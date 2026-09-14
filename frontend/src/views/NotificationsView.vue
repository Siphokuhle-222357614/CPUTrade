<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { listNotifications, markAllNotificationsRead, markNotificationRead } from "../api/notifications";

const router = useRouter();
const notifications = ref([]);
const loading = ref(true);
const errorMessage = ref("");

async function load() {
  loading.value = true;
  errorMessage.value = "";
  try {
    const { data } = await listNotifications();
    notifications.value = data;
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not load notifications.";
  } finally {
    loading.value = false;
  }
}

async function handleOpen(notification) {
  if (!notification.read) {
    try {
      await markNotificationRead(notification.id);
      notification.read = true;
    } catch (err) {
      // non-fatal — still navigate even if marking read failed
    }
  }
  if (notification.link) {
    router.push(notification.link);
  }
}

async function handleMarkAllRead() {
  try {
    await markAllNotificationsRead();
    notifications.value.forEach((n) => (n.read = true));
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not mark all as read.";
  }
}

function formatDate(value) {
  return new Date(value).toLocaleString();
}

const typeIcons = {
  VENDOR_APPROVED: "✅",
  LISTING_REMOVED: "🚫",
  NEW_MESSAGE: "💬",
};

onMounted(load);
</script>

<template>
  <div class="row">
    <h1>Notifications</h1>
    <button
      v-if="notifications.some((n) => !n.read)"
      type="button"
      class="btn btn-outline"
      @click="handleMarkAllRead"
    >
      Mark all read
    </button>
  </div>

  <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
  <p v-if="loading">Loading…</p>
  <p v-else-if="!notifications.length" class="empty-state">No notifications yet.</p>

  <div v-else class="table-list">
    <button
      v-for="notification in notifications"
      :key="notification.id"
      type="button"
      class="card row"
      :class="{ 'notification-unread': !notification.read }"
      style="text-align: left; width: 100%; cursor: pointer"
      @click="handleOpen(notification)"
    >
      <span>{{ typeIcons[notification.type] || "🔔" }}</span>
      <span style="flex: 1">
        {{ notification.message }}
        <span class="field-hint" style="display: block">{{ formatDate(notification.createdAt) }}</span>
      </span>
      <span v-if="!notification.read" class="badge badge-free">New</span>
    </button>
  </div>
</template>

<style scoped>
.notification-unread {
  border-left: 3px solid var(--color-accent);
}
</style>
