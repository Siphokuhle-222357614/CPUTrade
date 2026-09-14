<script setup>
import { useRouter } from "vue-router";
import { useAuthStore } from "../../stores/auth";

const auth = useAuthStore();
const router = useRouter();

function handleLogout() {
  auth.logout();
  router.push({ name: "login" });
}
</script>

<template>
  <header class="navbar">
    <router-link to="/" class="brand">🛒 CPUTrade</router-link>
    <nav class="navbar-links">
      <router-link v-if="auth.isAuthenticated" to="/products/new" class="btn btn-accent">
        + Sell
      </router-link>
      <router-link v-if="auth.isAuthenticated" to="/chats">Chats</router-link>
      <router-link v-if="auth.isAdmin" to="/admin">Admin</router-link>

      <template v-if="auth.isAuthenticated">
        <span>{{ auth.user?.campusHandle }}</span>
        <button class="btn btn-outline" @click="handleLogout">Log out</button>
      </template>
      <router-link v-else to="/login" class="btn btn-outline">Log in</router-link>
    </nav>
  </header>
</template>
