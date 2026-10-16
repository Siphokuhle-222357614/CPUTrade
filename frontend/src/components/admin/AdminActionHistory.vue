<script setup>
import { onMounted, ref } from "vue";
import { getAdminActionHistory } from "../../api/admin";
import { formatDateTime } from "../../utils/datetime";

const actions = ref([]);
const loading = ref(true);
const errorMessage = ref("");

const actionLabels = {
  VENDOR_APPROVED: "Vendor approved",
  LISTING_DEACTIVATED: "Listing deactivated",
  USER_SUSPENDED: "Account suspended",
  USER_REACTIVATED: "Account reactivated",
  REPORT_REVIEWED: "Report reviewed",
  REPORT_DISMISSED: "Report dismissed",
  APPEAL_APPROVED: "Appeal approved",
  APPEAL_REJECTED: "Appeal rejected",
};

async function load() {
  loading.value = true;
  errorMessage.value = "";
  try {
    const { data } = await getAdminActionHistory();
    actions.value = data;
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not load admin action history.";
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <section class="card" aria-labelledby="admin-action-history-title">
    <h2 id="admin-action-history-title">Recent Admin Actions</h2>
    <p class="field-hint">The latest 100 moderation and account actions, with actor and target snapshots.</p>
    <p v-if="errorMessage" class="alert alert-error" role="alert">{{ errorMessage }}</p>
    <p v-if="loading" role="status" aria-live="polite">Loading action history…</p>
    <p v-else-if="!actions.length" class="empty-state">No admin actions recorded yet.</p>

    <ol v-else class="table-list">
      <li v-for="entry in actions" :key="entry.id" class="card">
        <div class="row" style="justify-content: space-between">
          <strong>{{ actionLabels[entry.action] || entry.action }}</strong>
          <time class="field-hint" :datetime="entry.createdAt">{{ formatDateTime(entry.createdAt) }}</time>
        </div>
        <p>
          <strong>{{ entry.actorUsername }}</strong>
          acted on {{ entry.targetType.toLowerCase() }} “{{ entry.targetLabel }}” (#{{ entry.targetId }}).
        </p>
        <p class="field-hint">{{ entry.details }}</p>
      </li>
    </ol>
  </section>
</template>
