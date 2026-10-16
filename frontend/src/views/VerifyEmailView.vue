<script setup>
import { ref } from "vue";
import { useRoute } from "vue-router";
import * as authApi from "../api/auth";

const route = useRoute();
const token = typeof route.query.token === "string" ? route.query.token : "";
const loading = ref(false);
const verified = ref(false);
const errorMessage = ref("");

async function handleVerify() {
  errorMessage.value = "";
  loading.value = true;
  try {
    await authApi.verifyEmail(token);
    verified.value = true;
  } catch (err) {
    errorMessage.value =
      err.response?.data?.message || "Unable to verify this email address. Request a new link and try again.";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <section class="card" style="max-width: 480px; margin: var(--space-6) auto">
    <h2>Verify your CPUT email</h2>
    <p v-if="verified" class="alert alert-info">
      Email verified. You can now sign in to CPUTrade.
    </p>
    <template v-else>
      <p v-if="!token" class="alert alert-error">
        This verification link is missing its token. Request a new link from the sign-in page.
      </p>
      <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
      <p v-if="token && !errorMessage">
        Confirm that you want to verify the email address used to create this account.
      </p>
      <button
        v-if="token"
        type="button"
        class="btn btn-primary"
        :disabled="loading"
        @click="handleVerify"
      >
        {{ loading ? "Verifying…" : "Verify email" }}
      </button>
    </template>
    <p style="margin-top: var(--space-4)">
      <RouterLink to="/login">Return to sign in</RouterLink>
    </p>
  </section>
</template>
