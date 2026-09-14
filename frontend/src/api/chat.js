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

export function editMessage(conversationId, messageId, body) {
  return client.put(`/conversations/${conversationId}/messages/${messageId}`, { body });
}

export function deleteMessage(conversationId, messageId) {
  return client.delete(`/conversations/${conversationId}/messages/${messageId}`);
}

/** Marks every message from the other participant as read (seen tick) — call when the thread is opened. */
export function markConversationRead(conversationId) {
  return client.post(`/conversations/${conversationId}/read`);
}

export function pingTyping(conversationId) {
  return client.post(`/conversations/${conversationId}/typing`);
}

export function getTyping(conversationId) {
  return client.get(`/conversations/${conversationId}/typing`);
}
