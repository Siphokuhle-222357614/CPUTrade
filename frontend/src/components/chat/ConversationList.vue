<script setup>
import { onMounted, ref } from "vue";
import { useAuthStore } from "../../stores/auth";
import { myConversations } from "../../api/chat";

const auth = useAuthStore();
const conversations = ref([]);
const loading = ref(true);
const errorMessage = ref("");

async function load() {
  loading.value = true;
  try {
    const { data } = await myConversations();
    conversations.value = data;
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not load your chats.";
  } finally {
    loading.value = false;
  }
}

function otherParty(conversation) {
  return conversation.buyerId === auth.user?.id ? conversation.sellerUsername : conversation.buyerUsername;
}

onMounted(load);
</script>

<template>
  <h1>My Chats</h1>
  <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
  <p v-if="loading">Loading…</p>
  <p v-else-if="!conversations.length" class="empty-state">
    No conversations yet — message a seller from a product page to start one.
  </p>
  <div v-else class="table-list">
    <router-link
      v-for="conversation in conversations"
      :key="conversation.id"
      :to="{ name: 'chat', params: { id: conversation.id } }"
      class="card row"
      style="text-decoration: none; color: inherit"
    >
      <span>
        <strong>{{ conversation.productTitle }}</strong>
        — chatting with {{ otherParty(conversation) }}
      </span>
    </router-link>
  </div>
</template>
