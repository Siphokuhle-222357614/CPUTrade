<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../../stores/auth";
import { unreadNotificationCount } from "../../api/notifications";

const auth = useAuthStore();
const router = useRouter();
const unreadCount = ref(0);
let pollTimer = null;

async function refreshUnreadCount() {
  if (!auth.isAuthenticated) {
    unreadCount.value = 0;
    return;
  }
  try {
    const { data } = await unreadNotificationCount();
    unreadCount.value = data.unreadCount;
  } catch (err) {
    // non-fatal — the badge just won't update this cycle
  }
}

function handleLogout() {
  auth.logout();
  router.push({ name: "login" });
}

// US4.3: poll for new notifications every 20s rather than needing a websocket
// for this scale of app.
onMounted(() => {
  refreshUnreadCount();
  pollTimer = setInterval(refreshUnreadCount, 20000);
});
onBeforeUnmount(() => clearInterval(pollTimer));
watch(() => auth.isAuthenticated, refreshUnreadCount);
watch(() => router.currentRoute.value.name, (name) => {
  if (name === "notifications") refreshUnreadCount();
});
</script>

<template>
  <header class="navbar">
    <router-link to="/" class="brand">🛒 CPUTrade</router-link>
    <nav class="navbar-links">
      <router-link to="/board">Board</router-link>
      <!-- Admins moderate the platform, they don't sell on it — no business dashboard for them. -->
      <router-link v-if="auth.isAuthenticated && !auth.isAdmin" to="/dashboard">Dashboard</router-link>
      <router-link v-if="auth.isAuthenticated && !auth.isAdmin" to="/products/new" class="btn btn-accent">
        + Sell
      </router-link>
      <router-link v-if="auth.isAuthenticated" to="/chats">Chats</router-link>
      <router-link v-if="auth.isAuthenticated" to="/notifications" style="position: relative">
        🔔<span v-if="unreadCount > 0" class="badge badge-free" style="margin-left: 4px">{{ unreadCount }}</span>
      </router-link>
      <router-link v-if="auth.isAdmin" to="/admin">Admin</router-link>

      <template v-if="auth.isAuthenticated">
        <span>{{ auth.user?.campusHandle }}</span>
        <button class="btn btn-outline" @click="handleLogout">Log out</button>
      </template>
      <router-link v-else to="/login" class="btn btn-outline">Log in</router-link>
    </nav>
  </header>
</template>
