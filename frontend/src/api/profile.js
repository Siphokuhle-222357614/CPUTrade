import client from "./client";

export function getPublicProfile(userId) {
  return client.get(`/users/${userId}/profile`);
}

export function getPresence(userId) {
  return client.get(`/users/${userId}/presence`);
}
