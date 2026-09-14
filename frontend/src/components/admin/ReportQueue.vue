<script setup>
import { onMounted, ref } from "vue";
import { dismissReport, listReports, reviewReport } from "../../api/reports";

const reports = ref([]);
const loading = ref(true);
const errorMessage = ref("");
const actingId = ref(null);

const reasonLabels = {
  SCAM: "🚨 Scam / fraud",
  INAPPROPRIATE: "Inappropriate content",
  SPAM: "Spam",
  HARASSMENT: "Harassment",
  OTHER: "Other",
};

async function load() {
  loading.value = true;
  errorMessage.value = "";
  try {
    const { data } = await listReports("PENDING");
    reports.value = data;
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not load reports.";
  } finally {
    loading.value = false;
  }
}

async function handleAction(id, action) {
  actingId.value = id;
  try {
    await action(id);
    reports.value = reports.value.filter((r) => r.id !== id);
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not update this report.";
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
    <h3>Reports Queue</h3>
    <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
    <p v-if="loading">Loading…</p>
    <p v-else-if="!reports.length" class="empty-state">No pending reports — nice and quiet.</p>

    <div v-else class="table-list">
      <div v-for="report in reports" :key="report.id" class="card" style="background: var(--color-bg)">
        <div class="row">
          <span class="badge badge-inactive">{{ reasonLabels[report.reason] || report.reason }}</span>
          <span class="field-hint">{{ formatDate(report.createdAt) }}</span>
        </div>
        <p style="margin: var(--space-2) 0">
          <strong>Reported:</strong>
          <router-link v-if="report.reportedProductId" :to="{ name: 'product-detail', params: { id: report.reportedProductId } }">
            {{ report.reportedProductTitle }}
          </router-link>
          <span v-else>—</span>
          (seller: {{ report.reportedUsername || "—" }})
        </p>
        <p v-if="report.details" class="field-hint">"{{ report.details }}"</p>
        <p class="field-hint">Filed by {{ report.reporterUsername }}</p>
        <div class="row" style="justify-content: flex-end; margin-top: var(--space-2)">
          <button
            type="button"
            class="btn btn-outline"
            :disabled="actingId === report.id"
            @click="handleAction(report.id, dismissReport)"
          >
            Dismiss
          </button>
          <button
            type="button"
            class="btn btn-primary"
            :disabled="actingId === report.id"
            @click="handleAction(report.id, reviewReport)"
          >
            Mark Reviewed
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
