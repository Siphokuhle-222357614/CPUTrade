<script setup>
import { ref, onMounted } from "vue";
import { getAllListings, deactivateListing } from "../../api/admin";
import ConfirmDialog from "../common/ConfirmDialog.vue";
import { useToastStore } from "../../stores/toast";

const toast = useToastStore();
const listings = ref([]);
const loading = ref(true);
const errorMessage = ref("");
const removingId = ref(null);
const pendingDeactivateId = ref(null);

async function load() {
  loading.value = true;
  try {
    const { data } = await getAllListings();
    listings.value = data;
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not load listings.";
  } finally {
    loading.value = false;
  }
}

async function handleDeactivate() {
  const id = pendingDeactivateId.value;
  pendingDeactivateId.value = null;
  removingId.value = id;
  try {
    const { data } = await deactivateListing(id);
    const index = listings.value.findIndex((p) => p.id === id);
    if (index !== -1) listings.value[index] = data;
    toast.info("Listing removed and seller notified.");
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not remove this listing.";
  } finally {
    removingId.value = null;
  }
}

onMounted(load);
</script>

<template>
  <div class="card">
    <h3>All Listings (Moderation)</h3>
    <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
    <p v-if="loading">Loading…</p>
    <p v-else-if="!listings.length" class="empty-state">No listings yet.</p>
    <div v-else class="table-list">
      <div v-for="product in listings" :key="product.id" class="row">
        <span>
          {{ product.title }} — R{{ product.price }} —
          <span v-if="product.active">Active</span>
          <span v-else class="badge badge-inactive">Removed</span>
        </span>
        <button
          v-if="product.active"
          type="button"
          class="btn btn-danger"
          :disabled="removingId === product.id"
          @click="pendingDeactivateId = product.id"
        >
          {{ removingId === product.id ? "Removing…" : "Remove" }}
        </button>
      </div>
    </div>

    <ConfirmDialog
      :open="pendingDeactivateId !== null"
      title="Remove this listing?"
      message="It's for fraud/abuse and the seller will be notified. The listing stays visible to admins for moderation records."
      confirm-label="Remove"
      danger
      @confirm="handleDeactivate"
      @cancel="pendingDeactivateId = null"
    />
  </div>
</template>
