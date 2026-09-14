<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from "vue";
import CategoryFilterPills from "../components/marketplace/CategoryFilterPills.vue";
import SearchAndPriceFilter from "../components/marketplace/SearchAndPriceFilter.vue";
import ProductGrid from "../components/marketplace/ProductGrid.vue";
import { listProducts } from "../api/products";

const category = ref(null);
const keyword = ref("");
const minPrice = ref("");
const maxPrice = ref("");
const products = ref([]);
const loading = ref(true);
const errorMessage = ref("");

let debounceTimer = null;

async function load() {
  loading.value = true;
  errorMessage.value = "";
  try {
    const { data } = await listProducts({
      category: category.value,
      keyword: keyword.value,
      minPrice: minPrice.value,
      maxPrice: maxPrice.value,
    });
    products.value = data;
  } catch (err) {
    errorMessage.value = "Could not load listings — is the backend running?";
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
  <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
  <p v-if="loading">Loading…</p>
  <ProductGrid v-else :products="products" />
</template>
