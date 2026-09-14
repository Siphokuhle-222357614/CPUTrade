<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from "vue";
import CategoryFilterPills from "../components/marketplace/CategoryFilterPills.vue";
import SearchAndPriceFilter from "../components/marketplace/SearchAndPriceFilter.vue";
import ProductGrid from "../components/marketplace/ProductGrid.vue";
import { listProducts } from "../api/products";
import { loadCache, saveCache } from "../utils/offlineCache";

const CACHE_KEY = "marketplace_all";

const category = ref(null);
const keyword = ref("");
const minPrice = ref("");
const maxPrice = ref("");
const products = ref([]);
const loading = ref(true);
const errorMessage = ref("");
const offline = ref(false);

let debounceTimer = null;

async function load() {
  loading.value = true;
  errorMessage.value = "";
  const noFilters = !category.value && !keyword.value && !minPrice.value && !maxPrice.value;
  try {
    const { data } = await listProducts({
      category: category.value,
      keyword: keyword.value,
      minPrice: minPrice.value,
      maxPrice: maxPrice.value,
    });
    products.value = data;
    offline.value = false;
    // US7.1: only cache the unfiltered feed — that's the useful "last known
    // good" view to fall back to when a filtered request fails offline.
    if (noFilters) saveCache(CACHE_KEY, data);
  } catch (err) {
    const cached = noFilters ? loadCache(CACHE_KEY) : null;
    if (cached) {
      products.value = cached.data;
      offline.value = true;
    } else {
      errorMessage.value = "Could not load listings — is the backend running?";
    }
  } finally {
    loading.value = false;
  }
}

function debouncedLoad() {
  clearTimeout(debounceTimer);
  debounceTimer = setTimeout(load, 300);
}

// Category changes reload immediately; free-text/price fields debounce so we
// don't hammer the API on every keystroke.
watch(category, load);
watch([keyword, minPrice, maxPrice], debouncedLoad);
onMounted(load);
onBeforeUnmount(() => clearTimeout(debounceTimer));
</script>

<template>
  <h1>Marketplace</h1>
  <CategoryFilterPills v-model="category" />
  <SearchAndPriceFilter v-model:keyword="keyword" v-model:min-price="minPrice" v-model:max-price="maxPrice" />
  <p v-if="offline" class="alert alert-info">
    You're offline — showing listings cached from your last visit. They may be out of date.
  </p>
  <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
  <p v-if="loading">Loading…</p>
  <ProductGrid v-else :products="products" />
</template>
