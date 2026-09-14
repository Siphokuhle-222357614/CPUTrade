<script setup>
import { onMounted, ref } from "vue";
import { getSuspendedUsers, reactivateUser } from "../../api/admin";

const users = ref([]);
const loading = ref(true);
const errorMessage = ref("");
const actingId = ref(null);

async function load() {
  loading.value = true;
  errorMessage.value = "";
  try {
    const { data } = await getSuspendedUsers();
    users.value = data;
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not load suspended accounts.";
  } finally {
    loading.value = false;
  }
}

async function handleReactivate(id) {
  actingId.value = id;
  try {
    await reactivateUser(id);
    users.value = users.value.filter((u) => u.id !== id);
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not reactivate this account.";
  } finally {
    actingId.value = null;
  }
}

function formatDate(value) {
  return value ? new Date(value).toLocaleString() : null;
}

onMounted(load);
</script>

<template>
  <div class="card">
    <h3>Suspended Accounts</h3>
    <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
    <p v-if="loading">Loading…</p>
    <p v-else-if="!users.length" class="empty-state">No suspended accounts.</p>

    <div v-else class="table-list">
      <div v-for="user in users" :key="user.id" class="row">
        <span>
          <strong>{{ user.username }}</strong>
          <span v-if="user.suspensionReason"> — "{{ user.suspensionReason }}"</span>
          <span v-if="user.suspendedUntil" class="field-hint">
            (auto-lifts {{ formatDate(user.suspendedUntil) }})
          </span>
        </span>
        <button
          type="button"
          class="btn btn-primary"
          :disabled="actingId === user.id"
          @click="handleReactivate(user.id)"
        >
          {{ actingId === user.id ? "Reactivating…" : "Reactivate" }}
        </button>
      </div>
    </div>
  </div>
</template>
