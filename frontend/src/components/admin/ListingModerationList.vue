<script setup>
import { onMounted, ref } from "vue";
import { getAllListings, deactivateListing } from "../../api/admin";

const listings = ref([]);
const loading = ref(true);
const errorMessage = ref("");
const removingId = ref(null);

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

async function handleDeactivate(id) {
  if (!window.confirm("Remove this listing for fraud/abuse? The seller will be notified.")) return;
  removingId.value = id;
  try {
    const { data } = await deactivateListing(id);
    const index = listings.value.findIndex((p) => p.id === id);
    if (index !== -1) listings.value[index] = data;
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
          @click="handleDeactivate(product.id)"
        >
          {{ removingId === product.id ? "Removing…" : "Remove" }}
        </button>
      </div>
    </div>
  </div>
</template>
