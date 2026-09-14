<script setup>
import { reactive, ref } from "vue";
import { useRoute } from "vue-router";
import { submitAppeal } from "../api/appeals";

const route = useRoute();
const form = reactive({ username: route.query.username || "", message: "" });
const loading = ref(false);
const errorMessage = ref("");
const submitted = ref(false);

async function handleSubmit() {
  loading.value = true;
  errorMessage.value = "";
  try {
    await submitAppeal({ username: form.username, message: form.message });
    submitted.value = true;
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not submit your appeal.";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="card" style="max-width: 480px; margin: var(--space-6) auto">
    <h2>Appeal a Suspension</h2>
    <p class="field-hint">
      If your account was suspended and you believe it was a mistake, explain what happened below. An admin will
      review it.
    </p>

    <p v-if="submitted" class="alert alert-info">
      Your appeal has been submitted. An admin will review it — you'll be able to log in again if it's approved.
    </p>
    <template v-else>
      <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
      <form @submit.prevent="handleSubmit">
        <div class="field">
          <label for="appeal-username">Username</label>
          <input id="appeal-username" v-model="form.username" type="text" required />
        </div>
        <div class="field">
          <label for="appeal-message">Your appeal</label>
          <textarea id="appeal-message" v-model="form.message" rows="5" maxlength="2000" required></textarea>
        </div>
        <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
          {{ loading ? "Submitting…" : "Submit Appeal" }}
        </button>
      </form>
    </template>

    <p class="field-hint" style="margin-top: var(--space-4)">
      <router-link :to="{ name: 'login' }">← Back to sign in</router-link>
    </p>
  </div>
</template>
