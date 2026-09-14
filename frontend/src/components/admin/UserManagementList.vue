<script setup>
import { onMounted, ref } from "vue";
import { getAllUsers, reactivateUser } from "../../api/admin";
import SuspendUserDialog from "./SuspendUserDialog.vue";

const users = ref([]);
const loading = ref(true);
const errorMessage = ref("");
const actingId = ref(null);
const suspendTarget = ref(null); // { userId, username } | null

async function load() {
  loading.value = true;
  errorMessage.value = "";
  try {
    const { data } = await getAllUsers();
    users.value = data;
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not load users.";
  } finally {
    loading.value = false;
  }
}

async function handleReactivate(id) {
  actingId.value = id;
  try {
    const { data } = await reactivateUser(id);
    const index = users.value.findIndex((u) => u.id === id);
    if (index !== -1) users.value[index] = data;
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not reactivate this account.";
  } finally {
    actingId.value = null;
  }
}

function handleSuspended() {
  suspendTarget.value = null;
  load(); // simplest way to reflect the now-suspended status in the list
}

function formatDate(value) {
  return value ? new Date(value).toLocaleString() : null;
}

onMounted(load);
</script>

<template>
  <div class="card">
    <h3>Manage Students &amp; Vendors</h3>
    <p class="field-hint" style="margin-top: 0">
      Suspend an account directly for a protocol violation — a suspended user can appeal, and approving their appeal
      reactivates them automatically.
    </p>
    <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
    <p v-if="loading">Loading…</p>
    <p v-else-if="!users.length" class="empty-state">No students or vendors yet.</p>

    <div v-else class="table-list">
      <div v-for="user in users" :key="user.id" class="row">
        <span>
          <strong>{{ user.username }}</strong>
          <span class="badge badge-condition" style="margin-left: var(--space-2)">{{ user.role }}</span>
          <span v-if="user.role === 'VENDOR' && !user.vendorApproved" class="badge badge-warning">Pending Approval</span>
          <span v-if="user.accountStatus === 'SUSPENDED'" class="badge badge-danger">Suspended</span>
          <span v-if="user.suspensionReason" class="field-hint" style="display: block">"{{ user.suspensionReason }}"</span>
          <span v-if="user.suspendedUntil" class="field-hint" style="display: block">
            Auto-lifts {{ formatDate(user.suspendedUntil) }}
          </span>
        </span>
        <button
          v-if="user.accountStatus === 'SUSPENDED'"
          type="button"
          class="btn btn-primary"
          :disabled="actingId === user.id"
          @click="handleReactivate(user.id)"
        >
          {{ actingId === user.id ? "Reactivating…" : "Reactivate" }}
        </button>
        <button
          v-else
          type="button"
          class="btn btn-danger"
          @click="suspendTarget = { userId: user.id, username: user.username }"
        >
          🚫 Suspend
        </button>
      </div>
    </div>

    <SuspendUserDialog
      :open="suspendTarget !== null"
      :user-id="suspendTarget?.userId"
      :username="suspendTarget?.username"
      @close="suspendTarget = null"
      @suspended="handleSuspended"
    />
  </div>
</template>
