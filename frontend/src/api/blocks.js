import client from "./client";

export function blockUser(userId) {
  return client.post(`/users/${userId}/block`);
}

export function unblockUser(userId) {
  return client.delete(`/users/${userId}/block`);
}

export function listBlockedUsers() {
  return client.get("/users/blocked");
}
