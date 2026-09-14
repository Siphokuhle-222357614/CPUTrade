<script setup>
import { onMounted, ref } from "vue";
import { useAuthStore } from "../stores/auth";
import { listBulletinPosts } from "../api/bulletin";
import BulletinPostForm from "../components/bulletin/BulletinPostForm.vue";
import BulletinPostCard from "../components/bulletin/BulletinPostCard.vue";

const auth = useAuthStore();
const posts = ref([]);
const loading = ref(true);
const errorMessage = ref("");

async function load() {
  loading.value = true;
  errorMessage.value = "";
  try {
    const { data } = await listBulletinPosts();
    posts.value = data;
  } catch (err) {
    errorMessage.value = "Could not load the board — is the backend running?";
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <h1>Campus Bulletin Board</h1>
  <p class="field-hint">Study groups, lost & found, requests — anything that isn't a for-sale listing.</p>

  <div style="display: flex; flex-direction: column; gap: var(--space-4); margin-top: var(--space-4)">
    <BulletinPostForm v-if="auth.isAuthenticated" @posted="load" />
    <p v-else class="alert alert-info">Log in to post a notice.</p>

    <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
    <p v-if="loading">Loading…</p>
    <p v-else-if="!posts.length" class="empty-state">Nothing posted yet — be the first.</p>
    <BulletinPostCard v-for="post in posts" :key="post.id" :post="post" @deleted="load" />
  </div>
</template>
