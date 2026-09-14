<script setup>
import NavBar from "./components/layout/NavBar.vue";
import InstallPrompt from "./components/layout/InstallPrompt.vue";
</script>

<template>
  <InstallPrompt />
  <NavBar />
  <main class="page">
    <router-view />
  </main>
</template>
