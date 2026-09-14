<script setup>
import { onMounted, ref } from "vue";
import { approveAppeal, listAppeals, rejectAppeal } from "../../api/appeals";

const appeals = ref([]);
const loading = ref(true);
const errorMessage = ref("");
const actingId = ref(null);

async function load() {
  loading.value = true;
  errorMessage.value = "";
  try {
    const { data } = await listAppeals("PENDING");
    appeals.value = data;
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not load appeals.";
  } finally {
    loading.value = false;
  }
}

async function handleAction(id, action) {
  actingId.value = id;
  try {
    await action(id);
    appeals.value = appeals.value.filter((a) => a.id !== id);
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not update this appeal.";
  } finally {
    actingId.value = null;
  }
}

function formatDate(value) {
  return new Date(value).toLocaleString();
}

onMounted(load);
</script>

<template>
  <div class="card">
    <h3>Suspension Appeals</h3>
    <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
    <p v-if="loading">Loading…</p>
    <p v-else-if="!appeals.length" class="empty-state">No pending appeals.</p>

    <div v-else class="table-list">
      <div v-for="appeal in appeals" :key="appeal.id" class="card" style="background: var(--color-bg)">
        <div class="row">
          <strong>{{ appeal.username }}</strong>
          <span class="field-hint">{{ formatDate(appeal.createdAt) }}</span>
        </div>
        <p style="margin: var(--space-2) 0">"{{ appeal.message }}"</p>
        <div class="row" style="justify-content: flex-end">
          <button
            type="button"
            class="btn btn-outline"
            :disabled="actingId === appeal.id"
            @click="handleAction(appeal.id, rejectAppeal)"
          >
            Reject
          </button>
          <button
            type="button"
            class="btn btn-accent"
            :disabled="actingId === appeal.id"
            @click="handleAction(appeal.id, approveAppeal)"
          >
            Approve &amp; Reactivate
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
