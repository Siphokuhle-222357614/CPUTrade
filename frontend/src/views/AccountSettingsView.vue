<script setup>
import { computed, ref } from "vue";
import { useAuthStore } from "../stores/auth";
import { anonymizeAccount } from "../api/account";

const auth = useAuthStore();
const password = ref("");
const confirmation = ref("");
const deleting = ref(false);
const deleted = ref(false);
const errorMessage = ref("");

const canDelete = computed(
  () => password.value.length > 0 && confirmation.value === "DELETE" && !deleting.value
);

async function handleDelete() {
  if (!canDelete.value) return;

  errorMessage.value = "";
  deleting.value = true;
  try {
    await anonymizeAccount(password.value);
    auth.logout();
    password.value = "";
    confirmation.value = "";
    deleted.value = true;
  } catch (error) {
    errorMessage.value =
      error.response?.data?.message ??
      "We could not confirm the deletion result. Check your connection before retrying.";
  } finally {
    deleting.value = false;
  }
}
</script>

<template>
  <section class="card account-settings" aria-labelledby="account-settings-title">
    <h1 id="account-settings-title">Account privacy and deletion</h1>

    <div v-if="deleted" role="status" class="alert alert-info">
      Your account has been anonymized and you have been signed out.
      <RouterLink to="/login">Continue to sign in</RouterLink>
    </div>

    <template v-else>
      <p>
        Deleting your account is permanent. We replace your username, email address, campus handle, and
        password, and remove your private chat, saved searches, wishlist, notifications, push subscription,
        and bulletin posts. Your active listings are deactivated and their descriptions and photos are removed.
      </p>
      <p>
        To preserve transaction and moderation history, sale records, rating scores, reports, and appeals
        remain linked to a generic “Deleted account” label. Comments you wrote on ratings are removed.
        These records are retained indefinitely in this school-project prototype; this policy must be
        reviewed before any real-world use.
      </p>

      <form @submit.prevent="handleDelete">
        <label for="account-current-password">Current password</label>
        <input
          id="account-current-password"
          v-model="password"
          type="password"
          autocomplete="current-password"
          required
        />

        <label for="account-delete-confirmation">Type DELETE to confirm</label>
        <input
          id="account-delete-confirmation"
          v-model="confirmation"
          type="text"
          autocomplete="off"
          required
        />

        <p v-if="errorMessage" role="alert" class="alert alert-error">{{ errorMessage }}</p>

        <button type="submit" class="btn btn-danger" :disabled="!canDelete">
          {{ deleting ? "Deleting account…" : "Permanently delete my account" }}
        </button>
      </form>
    </template>
  </section>
</template>

<style scoped>
.account-settings {
  max-width: 680px;
  margin: var(--space-6) auto;
}

.account-settings form {
  display: grid;
  gap: var(--space-3);
  margin-top: var(--space-5);
}

.account-settings input {
  width: 100%;
  padding: var(--space-3);
  border: 1px solid var(--color-border, #cbd5e1);
  border-radius: var(--radius-md, 8px);
  font: inherit;
}
</style>