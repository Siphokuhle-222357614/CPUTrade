<script setup>
import { onMounted, reactive, ref } from "vue";
import { useAuthStore } from "../../stores/auth";
import { getMessages, sendMessage } from "../../api/chat";

const props = defineProps({
  conversationId: {
    type: [String, Number],
    required: true,
  },
});

const auth = useAuthStore();
const messages = ref([]);
const loading = ref(true);
const sending = ref(false);
const errorMessage = ref("");

const form = reactive({ body: "", locationSuggestion: "" });

const locationLabels = {
  LIBRARY: "📍 Library — Ground Floor",
  IT_BUILDING: "💻 IT Building — Entrance",
  SRC_OFFICE: "🏛 SRC Office",
  MAIN_GATE: "🚧 Main Gate Security",
};

async function load() {
  loading.value = true;
  errorMessage.value = "";
  try {
    const { data } = await getMessages(props.conversationId);
    messages.value = data;
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

onMounted(load);
</script>

<template>
  <div class="card">
    <p class="alert alert-info">🔒 Keep all communication in-app. Never share personal details.</p>
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

    <form @submit.prevent="handleSend">
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
  </div>
</template>
