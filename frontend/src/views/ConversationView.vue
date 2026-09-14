<script setup>
import ChatThread from "../components/chat/ChatThread.vue";

defineProps({
  id: {
    type: [String, Number],
    required: true,
  },
});
</script>

<template>
  <h1>Chat</h1>
  <ChatThread :conversation-id="id" />
</template>
