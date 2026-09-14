<script setup>
import { onMounted, ref } from "vue";
import ProductGrid from "../components/marketplace/ProductGrid.vue";
import { listWishlist } from "../api/wishlist";
import { listSavedSearches, deleteSavedSearch } from "../api/savedSearches";
import { useToastStore } from "../stores/toast";

const toast = useToastStore();

const products = ref([]);
const loading = ref(true);
const errorMessage = ref("");

const savedSearches = ref([]);
const searchesLoading = ref(true);

const categoryLabels = {
  TEXTBOOKS: "Textbooks",
  ELECTRONICS: "Electronics",
  CLOTHING: "Clothing",
  SERVICES: "Services",
  OTHER: "Other",
};

// Builds a readable summary from whichever of the three optional criteria
// were actually set, e.g. "Electronics · "laptop" · under R3000".
function describeSearch(search) {
  const parts = [];
  if (search.category) parts.push(categoryLabels[search.category] || search.category);
  if (search.keyword) parts.push(`"${search.keyword}"`);
  if (search.maxPrice != null) parts.push(`under R${Number(search.maxPrice).toFixed(2).replace(/\.00$/, "")}`);
  return parts.join(" · ") || "Any new listing";
}

async function load() {
  loading.value = true;
  errorMessage.value = "";
  try {
    const { data } = await listWishlist();
    products.value = data;
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not load your wishlist.";
  } finally {
    loading.value = false;
  }
}

async function loadSavedSearches() {
  searchesLoading.value = true;
  try {
    const { data } = await listSavedSearches();
    savedSearches.value = data;
  } catch {
    // Non-fatal -- the wishlist section above is the main point of this page.
  } finally {
    searchesLoading.value = false;
  }
}

async function removeSavedSearch(id) {
  const previous = savedSearches.value;
  savedSearches.value = previous.filter((s) => s.id !== id);
  try {
    await deleteSavedSearch(id);
  } catch (err) {
    savedSearches.value = previous;
    toast.error(err.response?.data?.message || "Could not remove this alert.");
  }
}

onMounted(() => {
  load();
  loadSavedSearches();
});
</script>

<template>
  <div>
    <h1>❤️ My Wishlist</h1>
    <p class="field-hint" style="margin-bottom: var(--space-4)">
      Listings you're watching — we'll notify you about a price drop or if one sells.
    </p>

    <p v-if="loading">Loading…</p>
    <p v-else-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
    <ProductGrid
      v-else
      :products="products"
      empty-icon="🤍"
      empty-message="Nothing wishlisted yet — tap the heart on a listing to watch it."
    />

    <h2 style="margin-top: var(--space-6)">🔔 Saved Search Alerts</h2>
    <p class="field-hint" style="margin-bottom: var(--space-4)">
      Standing alerts from the marketplace's "Notify me about listings like this" button — we'll tell you the
      moment something matching gets posted.
    </p>

    <p v-if="searchesLoading">Loading…</p>
    <p v-else-if="!savedSearches.length" class="empty-state">
      <span class="empty-state-icon" aria-hidden="true">🔍</span>
      No saved alerts yet — set a filter on the marketplace and save it.
    </p>
    <div v-else class="table-list">
      <div v-for="search in savedSearches" :key="search.id" class="row">
        <span>{{ describeSearch(search) }}</span>
        <button type="button" class="btn btn-outline btn-sm" @click="removeSavedSearch(search.id)">Remove</button>
      </div>
    </div>
  </div>
</template>
