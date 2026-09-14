import client from "./client";

export function listNotifications() {
  return client.get("/notifications");
}

export function unreadNotificationCount() {
  return client.get("/notifications/unread-count");
}

export function markNotificationRead(id) {
  return client.patch(`/notifications/${id}/read`);
}

export function markAllNotificationsRead() {
  return client.patch("/notifications/read-all");
}
