<script setup>
import { onMounted, ref } from "vue";
import { getPendingVendorRequests, approveVendor } from "../../api/admin";
import { useToastStore } from "../../stores/toast";

const toast = useToastStore();
const requests = ref([]);
const loading = ref(true);
const errorMessage = ref("");
const approvingId = ref(null);

async function load() {
  loading.value = true;
  try {
    const { data } = await getPendingVendorRequests();
    requests.value = data;
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not load vendor requests.";
  } finally {
    loading.value = false;
  }
}

async function handleApprove(userId) {
  approvingId.value = userId;
  const user = requests.value.find((u) => u.id === userId);
  try {
    await approveVendor(userId);
    requests.value = requests.value.filter((u) => u.id !== userId);
    toast.success(`${user?.username || "Vendor"} approved. ✅`);
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not approve this vendor.";
  } finally {
    approvingId.value = null;
  }
}

onMounted(load);
</script>

<template>
  <div class="card">
    <h3>Pending Vendor Requests</h3>
    <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
    <p v-if="loading">Loading…</p>
    <p v-else-if="!requests.length" class="empty-state">No pending vendor requests.</p>
    <div v-else class="table-list">
      <div v-for="user in requests" :key="user.id" class="row">
        <span>{{ user.username }} — {{ user.campusHandle }}</span>
        <button
          type="button"
          class="btn btn-accent"
          :disabled="approvingId === user.id"
          @click="handleApprove(user.id)"
        >
          {{ approvingId === user.id ? "Approving…" : "Approve" }}
        </button>
      </div>
    </div>
  </div>
</template>
