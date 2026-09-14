<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useAuthStore } from "../../stores/auth";
import { getConversation, getMessages, sendMessage } from "../../api/chat";
import { blockUser, listBlockedUsers, unblockUser } from "../../api/blocks";
import ReportDialog from "../trust/ReportDialog.vue";
import ConfirmDialog from "../common/ConfirmDialog.vue";

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
const showReportDialog = ref(false);
const showBlockConfirm = ref(false);

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
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not load this conversation.";
  } finally {
    loading.value = false;
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

onMounted(load);
</script>

<template>
  <div class="card">
    <div v-if="otherUser" class="row" style="margin-bottom: var(--space-3)">
      <strong>Chatting with {{ otherUser.username }}</strong>
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

    <div v-else style="display: flex; flex-direction: column; gap: var(--space-3); margin-bottom: var(--space-4)">
      <div
        v-for="message in messages"
        :key="message.id"
        :style="{
          alignSelf: message.senderId === auth.user?.id ? 'flex-end' : 'flex-start',
          maxWidth: '80%',
        }"
      >
        <div
          class="card"
          :style="{
            background: message.senderId === auth.user?.id ? 'var(--color-primary)' : 'var(--color-bg)',
            color: message.senderId === auth.user?.id ? '#fff' : 'var(--color-text)',
            padding: 'var(--space-2) var(--space-3)',
          }"
        >
          <p style="margin: 0">{{ message.body }}</p>
          <p v-if="message.locationSuggestion" class="badge badge-condition" style="margin-top: var(--space-1)">
            {{ locationLabels[message.locationSuggestion] }} (safe meetup suggested)
          </p>
        </div>
        <p class="field-hint" style="margin: 2px 4px">{{ message.senderUsername }}</p>
      </div>
      <p v-if="!messages.length" class="empty-state">No messages yet — say hi!</p>
    </div>

    <form v-if="!isBlocked" @submit.prevent="handleSend">
      <div class="field">
        <label for="body">Message</label>
        <input id="body" v-model="form.body" type="text" placeholder="Type a message…" maxlength="2000" required />
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
  </div>
</template>
