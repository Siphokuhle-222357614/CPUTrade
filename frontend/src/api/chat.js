import client from "./client";

export function startConversation(productId) {
  return client.post(`/products/${productId}/conversations`);
}

export function myConversations() {
  return client.get("/conversations");
}

export function getConversation(conversationId) {
  return client.get(`/conversations/${conversationId}`);
}

export function getMessages(conversationId) {
  return client.get(`/conversations/${conversationId}/messages`);
}

export function sendMessage(conversationId, { body, locationSuggestion }) {
  return client.post(`/conversations/${conversationId}/messages`, { body, locationSuggestion });
}
