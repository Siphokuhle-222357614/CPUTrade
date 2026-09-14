<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../../stores/auth";
import { unreadNotificationCount } from "../../api/notifications";

const auth = useAuthStore();
const router = useRouter();
const unreadCount = ref(0);
const mobileMenuOpen = ref(false);
const userMenuOpen = ref(false);
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
  closeMenus();
  auth.logout();
  router.push({ name: "login" });
}

function closeMenus() {
  mobileMenuOpen.value = false;
  userMenuOpen.value = false;
}

function closeOnOutsideClick(event) {
  if (userMenuOpen.value && !event.target.closest(".navbar-user-menu")) {
    userMenuOpen.value = false;
  }
}

// US4.3: poll for new notifications every 20s rather than needing a websocket
// for this scale of app.
onMounted(() => {
  refreshUnreadCount();
  pollTimer = setInterval(refreshUnreadCount, 20000);
  document.addEventListener("click", closeOnOutsideClick);
});
onBeforeUnmount(() => {
  clearInterval(pollTimer);
  document.removeEventListener("click", closeOnOutsideClick);
});
watch(() => auth.isAuthenticated, refreshUnreadCount);
watch(
  () => router.currentRoute.value.name,
  (name) => {
    if (name === "notifications") refreshUnreadCount();
    // A route change is the clearest signal a menu should close — covers
    // both the mobile panel and the user dropdown in one place.
    closeMenus();
  }
);

const initials = computed(() => {
  const name = auth.user?.username || "?";
  return name.slice(0, 2).toUpperCase();
});
</script>

<template>
  <header class="navbar">
    <div class="navbar-inner">
      <router-link to="/" class="brand" @click="closeMenus">🛒 CPUTrade</router-link>

      <!-- Desktop nav: the frequent stuff stays one click away; everything
           else about "me" (dashboard, logout) lives behind the avatar so the
           bar itself doesn't turn into a wall of text links. -->
      <nav class="navbar-links navbar-links-desktop">
        <router-link to="/board">Board</router-link>
        <router-link v-if="auth.isAuthenticated && !auth.isAdmin" to="/wishlist">Wishlist</router-link>
        <router-link v-if="auth.isAuthenticated" to="/chats">Chats</router-link>
        <router-link v-if="auth.isAdmin" to="/admin">Admin</router-link>

        <router-link
          v-if="auth.isAuthenticated"
          to="/notifications"
          class="navbar-icon-link"
          aria-label="Notifications"
        >
          🔔
          <span v-if="unreadCount > 0" class="badge badge-free navbar-badge-pulse">{{ unreadCount }}</span>
        </router-link>

        <router-link v-if="auth.isAuthenticated && !auth.isAdmin" to="/products/new" class="btn btn-accent btn-sm">
          + Sell
        </router-link>

        <div v-if="auth.isAuthenticated" class="navbar-user-menu">
          <button
            type="button"
            class="navbar-avatar"
            :aria-expanded="userMenuOpen"
            aria-label="Account menu"
            @click.stop="userMenuOpen = !userMenuOpen"
          >
            {{ initials }}
          </button>
          <Transition name="dropdown">
            <div v-if="userMenuOpen" class="navbar-dropdown">
              <p class="navbar-dropdown-handle">{{ auth.user?.campusHandle }}</p>
              <router-link v-if="!auth.isAdmin" to="/dashboard" @click="closeMenus">📊 Dashboard</router-link>
              <button type="button" @click="handleLogout">🚪 Log out</button>
            </div>
          </Transition>
        </div>
        <router-link v-else to="/login" class="btn btn-outline btn-sm">Log in</router-link>
      </nav>

      <!-- Mobile: a single toggle instead of every link fighting for space
           and wrapping onto its own ragged row. -->
      <button
        type="button"
        class="navbar-burger"
        :class="{ open: mobileMenuOpen }"
        :aria-expanded="mobileMenuOpen"
        aria-label="Menu"
        @click="mobileMenuOpen = !mobileMenuOpen"
      >
        <span></span><span></span><span></span>
      </button>
    </div>

    <Transition name="mobile-menu">
      <nav v-if="mobileMenuOpen" class="navbar-mobile-panel">
        <router-link to="/board" @click="closeMenus">📋 Board</router-link>
        <router-link v-if="auth.isAuthenticated && !auth.isAdmin" to="/dashboard" @click="closeMenus">
          📊 Dashboard
        </router-link>
        <router-link v-if="auth.isAuthenticated && !auth.isAdmin" to="/wishlist" @click="closeMenus">
          ❤️ Wishlist
        </router-link>
        <router-link v-if="auth.isAuthenticated" to="/chats" @click="closeMenus">💬 Chats</router-link>
        <router-link v-if="auth.isAuthenticated" to="/notifications" @click="closeMenus">
          🔔 Notifications
          <span v-if="unreadCount > 0" class="badge badge-free">{{ unreadCount }}</span>
        </router-link>
        <router-link v-if="auth.isAdmin" to="/admin" @click="closeMenus">🛠️ Admin</router-link>
        <router-link
          v-if="auth.isAuthenticated && !auth.isAdmin"
          to="/products/new"
          class="btn btn-accent"
          @click="closeMenus"
        >
          + Sell
        </router-link>

        <div v-if="auth.isAuthenticated" class="navbar-mobile-account">
          <span>{{ auth.user?.campusHandle }}</span>
          <button type="button" class="btn btn-outline" @click="handleLogout">Log out</button>
        </div>
        <router-link v-else to="/login" class="btn btn-outline" @click="closeMenus">Log in</router-link>
      </nav>
    </Transition>
  </header>
</template>
