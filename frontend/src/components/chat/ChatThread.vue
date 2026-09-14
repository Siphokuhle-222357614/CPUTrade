<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import { useAuthStore } from "../../stores/auth";
import {
  deleteMessage,
  editMessage,
  getConversation,
  getMessages,
  getTyping,
  markConversationRead,
  pingTyping,
  sendMessage,
} from "../../api/chat";
import { blockUser, listBlockedUsers, unblockUser } from "../../api/blocks";
import { getPresence } from "../../api/profile";
import ReportDialog from "../trust/ReportDialog.vue";
import ConfirmDialog from "../common/ConfirmDialog.vue";
import ProfileDialog from "./ProfileDialog.vue";
import { dateKey, formatDayLabel, formatLastSeen, formatTime } from "../../utils/datetime";
import { playMessageSound } from "../../utils/sound";

const props = defineProps({
  conversationId: {
    type: [String, Number],
    required: true,
  },
});

const auth = useAuthStore();
const conversation = ref(null);
const messages = ref([]);
const loading = ref(true);
const sending = ref(false);
const errorMessage = ref("");
const isBlocked = ref(false);
const otherOnline = ref(false);
const otherLastActiveAt = ref(null);
const otherTyping = ref(false);

const showReportDialog = ref(false);
const showBlockConfirm = ref(false);
const showProfileDialog = ref(false);
const editingId = ref(null);
const editBody = ref("");
const deleteTargetId = ref(null);
const openMenuId = ref(null); // which message's ⋮ dropdown is open, if any

const form = reactive({ body: "", locationSuggestion: "" });

const locationLabels = {
  LIBRARY: "📍 Library — Ground Floor",
  IT_BUILDING: "💻 IT Building — Entrance",
  SRC_OFFICE: "🏛 SRC Office",
  MAIN_GATE: "🚧 Main Gate Security",
};

const otherUser = computed(() => {
  if (!conversation.value) return null;
  return conversation.value.buyerId === auth.user?.id
    ? { id: conversation.value.sellerId, username: conversation.value.sellerUsername }
    : { id: conversation.value.buyerId, username: conversation.value.buyerUsername };
});

/** Messages grouped with a day-divider row inserted whenever the calendar day changes. */
const timeline = computed(() => {
  const rows = [];
  let currentKey = null;
  for (const message of messages.value) {
    const key = dateKey(message.createdAt);
    if (key !== currentKey) {
      rows.push({ kind: "day", key, label: formatDayLabel(message.createdAt) });
      currentKey = key;
    }
    rows.push({ kind: "message", message });
  }
  return rows;
});

// WhatsApp convention: one check = sent only; two checks = delivered to
// their device but not opened; two checks in blue = they opened the chat.
function tickFor(message) {
  if (message.senderId !== auth.user?.id) return null;
  if (message.readAt) return { glyph: "✓✓", cssClass: "tick-seen" };
  if (message.deliveredAt) return { glyph: "✓✓", cssClass: "tick-delivered" };
  return { glyph: "✓", cssClass: "tick-sent" };
}

let messagesPollTimer = null;
let typingPollTimer = null;
let presencePollTimer = null;
let lastTypingPingAt = 0;

async function load() {
  loading.value = true;
  errorMessage.value = "";
  try {
    const [{ data: convo }, { data: msgs }, { data: blocked }] = await Promise.all([
      getConversation(props.conversationId),
      getMessages(props.conversationId),
      listBlockedUsers(),
    ]);
    conversation.value = convo;
    messages.value = msgs;
    const otherId = convo.buyerId === auth.user?.id ? convo.sellerId : convo.buyerId;
    isBlocked.value = blocked.some((u) => u.id === otherId);
    markConversationRead(props.conversationId).catch(() => {});
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not load this conversation.";
  } finally {
    loading.value = false;
  }
}

async function refreshMessages() {
  try {
    const { data } = await getMessages(props.conversationId);
    const previousIds = new Set(messages.value.map((m) => m.id));
    const newlyArrived = data.some((m) => !previousIds.has(m.id) && m.senderId !== auth.user?.id);
    messages.value = data;
    if (newlyArrived) {
      playMessageSound();
      markConversationRead(props.conversationId).catch(() => {});
    }
  } catch (err) {
    // A failed background poll shouldn't blank out an already-loaded thread.
  }
}

async function refreshTyping() {
  try {
    const { data } = await getTyping(props.conversationId);
    otherTyping.value = data.typing;
  } catch (err) {
    // ignore
  }
}

async function refreshPresence() {
  if (!otherUser.value) return;
  try {
    const { data } = await getPresence(otherUser.value.id);
    otherOnline.value = data.online;
    otherLastActiveAt.value = data.lastActiveAt;
  } catch (err) {
    // ignore
  }
}

function handleTypingInput() {
  const now = Date.now();
  if (now - lastTypingPingAt > 3000) {
    lastTypingPingAt = now;
    pingTyping(props.conversationId).catch(() => {});
  }
}

async function handleSend() {
  if (!form.body.trim()) return;
  sending.value = true;
  errorMessage.value = "";
  try {
    const { data } = await sendMessage(props.conversationId, {
      body: form.body,
      locationSuggestion: form.locationSuggestion || null,
    });
    messages.value.push(data);
    form.body = "";
    form.locationSuggestion = "";
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not send that message.";
  } finally {
    sending.value = false;
  }
}

function startEdit(message) {
  openMenuId.value = null;
  editingId.value = message.id;
  editBody.value = message.body;
}

function startDelete(messageId) {
  openMenuId.value = null;
  deleteTargetId.value = messageId;
}

function cancelEdit() {
  editingId.value = null;
  editBody.value = "";
}

async function submitEdit(message) {
  if (!editBody.value.trim()) return;
  try {
    const { data } = await editMessage(props.conversationId, message.id, editBody.value);
    const index = messages.value.findIndex((m) => m.id === message.id);
    if (index !== -1) messages.value[index] = data;
    cancelEdit();
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not edit this message.";
  }
}

async function confirmDelete() {
  const id = deleteTargetId.value;
  deleteTargetId.value = null;
  try {
    await deleteMessage(props.conversationId, id);
    const index = messages.value.findIndex((m) => m.id === id);
    if (index !== -1) {
      messages.value[index] = { ...messages.value[index], deleted: true, body: null };
    }
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not delete this message.";
  }
}

function toggleMenu(messageId) {
  openMenuId.value = openMenuId.value === messageId ? null : messageId;
}

function closeMenuOnOutsideClick(event) {
  if (!event.target.closest(".chat-message-menu-wrap")) {
    openMenuId.value = null;
  }
}

async function handleBlockConfirm() {
  showBlockConfirm.value = false;
  if (!otherUser.value) return;
  try {
    await blockUser(otherUser.value.id);
    isBlocked.value = true;
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not block this user.";
  }
}

async function handleUnblock() {
  if (!otherUser.value) return;
  try {
    await unblockUser(otherUser.value.id);
    isBlocked.value = false;
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not unblock this user.";
  }
}

onMounted(async () => {
  await load();
  refreshPresence();
  messagesPollTimer = setInterval(refreshMessages, 4000);
  typingPollTimer = setInterval(refreshTyping, 2500);
  presencePollTimer = setInterval(refreshPresence, 8000);
  document.addEventListener("click", closeMenuOnOutsideClick);
});

onBeforeUnmount(() => {
  clearInterval(messagesPollTimer);
  clearInterval(typingPollTimer);
  clearInterval(presencePollTimer);
  document.removeEventListener("click", closeMenuOnOutsideClick);
});
</script>

<template>
  <div class="card">
    <div v-if="otherUser" class="row" style="margin-bottom: var(--space-3)">
      <button type="button" class="chat-header-btn" @click="showProfileDialog = true">
        <strong>{{ otherUser.username }}</strong>
        <span v-if="otherOnline" class="field-hint" style="color: var(--color-success)">🟢 Online</span>
        <span v-else-if="otherLastActiveAt" class="field-hint">⚪ {{ formatLastSeen(otherLastActiveAt) }}</span>
        <span v-else class="field-hint">⚪ Offline</span>
      </button>
      <span class="row" style="gap: var(--space-2); width: auto">
        <button type="button" class="btn btn-outline" @click="showReportDialog = true">🚩 Report</button>
        <button v-if="!isBlocked" type="button" class="btn btn-outline" @click="showBlockConfirm = true">
          🚫 Block
        </button>
        <button v-else type="button" class="btn btn-outline" @click="handleUnblock">✅ Unblock</button>
      </span>
    </div>

    <p class="alert alert-info">🔒 Keep all communication in-app. Never share personal details.</p>
    <p v-if="isBlocked" class="alert alert-error">
      You've blocked this user — unblock them to send or receive messages here.
    </p>
    <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
    <p v-if="loading">Loading…</p>

    <div v-else style="display: flex; flex-direction: column; gap: var(--space-2); margin-bottom: var(--space-3)">
      <template v-for="row in timeline" :key="row.kind === 'day' ? `day-${row.key}` : `msg-${row.message.id}`">
        <div v-if="row.kind === 'day'" class="chat-day-divider">
          <span>{{ row.label }}</span>
        </div>

        <div
          v-else
          class="chat-message-wrapper"
          :style="{
            alignSelf: row.message.senderId === auth.user?.id ? 'flex-end' : 'flex-start',
            maxWidth: '80%',
          }"
        >
          <div
            class="card chat-bubble"
            :class="{ 'chat-bubble-own': row.message.senderId === auth.user?.id }"
          >
            <!-- Fixed (always-visible) chevron trigger — not a hover-only kebab. -->
            <div
              v-if="row.message.senderId === auth.user?.id && !row.message.deleted && editingId !== row.message.id"
              class="chat-message-menu-wrap"
            >
              <button type="button" class="chat-menu-trigger" aria-label="Message options" @click.stop="toggleMenu(row.message.id)">
                <svg width="12" height="12" viewBox="0 0 12 12" fill="none" aria-hidden="true">
                  <path d="M2.5 4.5L6 8L9.5 4.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
              </button>
              <div v-if="openMenuId === row.message.id" class="chat-message-menu">
                <button type="button" @click="startEdit(row.message)">✏️ Edit</button>
                <button type="button" @click="startDelete(row.message.id)">🗑️ Delete</button>
              </div>
            </div>

            <template v-if="editingId === row.message.id">
              <textarea v-model="editBody" rows="2" style="width: 100%; color: var(--color-text)"></textarea>
              <div class="row" style="justify-content: flex-end; margin-top: var(--space-1)">
                <button type="button" class="btn btn-outline" style="padding: 4px 10px" @click="cancelEdit">Cancel</button>
                <button type="button" class="btn btn-accent" style="padding: 4px 10px" @click="submitEdit(row.message)">Save</button>
              </div>
            </template>
            <template v-else-if="row.message.deleted">
              <p style="margin: 0; font-style: italic; opacity: 0.7">🚫 This message was deleted</p>
            </template>
            <template v-else>
              <p style="margin: 0">{{ row.message.body }}</p>
              <p v-if="row.message.locationSuggestion" class="badge badge-condition" style="margin-top: var(--space-1)">
                {{ locationLabels[row.message.locationSuggestion] }} (safe meetup suggested)
              </p>
            </template>

            <div class="row chat-bubble-meta">
              <span>
                {{ formatTime(row.message.createdAt) }}
                <span v-if="row.message.edited && !row.message.deleted"> · edited</span>
              </span>
              <span v-if="tickFor(row.message)" class="chat-tick" :class="tickFor(row.message).cssClass">
                {{ tickFor(row.message).glyph }}
              </span>
            </div>
          </div>
        </div>
      </template>

      <p v-if="!messages.length" class="empty-state">No messages yet — say hi!</p>
      <p v-if="otherTyping" class="field-hint" style="font-style: italic">{{ otherUser?.username }} is typing…</p>
    </div>

    <form v-if="!isBlocked" @submit.prevent="handleSend">
      <div class="field">
        <label for="body">Message</label>
        <input
          id="body"
          v-model="form.body"
          type="text"
          placeholder="Type a message…"
          maxlength="2000"
          required
          @input="handleTypingInput"
        />
      </div>
      <div class="field">
        <label for="location">Suggest a safe meetup spot (optional)</label>
        <select id="location" v-model="form.locationSuggestion">
          <option value="">— Select Safe CPUT Meetup Spot —</option>
          <option value="LIBRARY">Library — Ground Floor</option>
          <option value="IT_BUILDING">IT Building — Entrance</option>
          <option value="SRC_OFFICE">SRC Office</option>
          <option value="MAIN_GATE">Main Gate Security</option>
        </select>
      </div>
      <button type="submit" class="btn btn-primary" :disabled="sending">
        {{ sending ? "Sending…" : "Send" }}
      </button>
    </form>

    <ReportDialog
      v-if="otherUser"
      :open="showReportDialog"
      target-type="USER"
      :target-id="otherUser.id"
      :target-label="`Conversation about ${conversation?.productTitle}`"
      @close="showReportDialog = false"
    />
    <ConfirmDialog
      :open="showBlockConfirm"
      title="Block this user?"
      message="They won't be able to message you, and you won't be able to message them, until you unblock."
      confirm-label="Block"
      danger
      @confirm="handleBlockConfirm"
      @cancel="showBlockConfirm = false"
    />
    <ConfirmDialog
      :open="deleteTargetId !== null"
      title="Delete this message?"
      message="It'll show as removed in this chat for both of you."
      confirm-label="Delete"
      danger
      @confirm="confirmDelete"
      @cancel="deleteTargetId = null"
    />
    <ProfileDialog v-if="otherUser" :open="showProfileDialog" :user-id="otherUser.id" @close="showProfileDialog = false" />
  </div>
</template>

<style scoped>
.chat-header-btn {
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  text-align: left;
  display: flex;
  flex-direction: column;
  gap: 1px;
  color: var(--color-text);
}
.chat-header-btn:hover strong {
  text-decoration: underline;
}

.chat-day-divider {
  display: flex;
  justify-content: center;
  margin: var(--space-2) 0;
}
.chat-day-divider span {
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-pill);
  padding: 3px 12px;
  font-size: 11px;
  color: var(--color-text-muted);
  font-weight: 600;
}

.chat-message-wrapper {
  position: relative;
}
.chat-bubble {
  padding: var(--space-2) var(--space-3);
  position: relative;
}
.chat-bubble-own {
  background: var(--color-primary);
  color: #fff;
}
.chat-bubble:not(.chat-bubble-own) {
  background: var(--color-bg);
  color: var(--color-text);
}

.chat-bubble-meta {
  justify-content: flex-end;
  gap: 4px;
  font-size: 10px;
  opacity: 0.85;
  margin-top: 2px;
}

/* Read-receipt ticks — grey for sent/delivered, a clearly distinct blue once seen. */
.chat-tick {
  font-weight: 700;
  letter-spacing: -1.5px;
}
.tick-sent,
.tick-delivered {
  color: rgba(255, 255, 255, 0.65);
}
.tick-seen {
  color: #53bdeb;
}

/* A fixed (always-visible) chevron trigger, not a hover-only kebab. */
.chat-message-menu-wrap {
  position: absolute;
  top: 2px;
  right: 4px;
}
.chat-menu-trigger {
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.15);
  border: none;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  color: #fff;
  cursor: pointer;
  transition: background-color var(--transition-fast);
}
.chat-menu-trigger:hover {
  background: rgba(0, 0, 0, 0.3);
}
.chat-message-menu {
  position: absolute;
  top: 22px;
  right: 0;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-input);
  box-shadow: var(--shadow-card-hover);
  display: flex;
  flex-direction: column;
  min-width: 110px;
  overflow: hidden;
  z-index: 5;
}
.chat-message-menu button {
  background: none;
  border: none;
  text-align: left;
  padding: 8px 12px;
  font-size: 13px;
  color: var(--color-text);
  cursor: pointer;
}
.chat-message-menu button:hover {
  background: var(--color-bg);
}
</style>
