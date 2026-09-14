<script setup>
import { onMounted, ref, watch } from "vue";
import ProductDetail from "../components/product/ProductDetail.vue";
import { getProduct } from "../api/products";
import { loadCache, saveCache } from "../utils/offlineCache";

const props = defineProps({
  id: {
    type: [String, Number],
    required: true,
  },
});

const product = ref(null);
const loading = ref(true);
const errorMessage = ref("");
const offline = ref(false);

async function load() {
  loading.value = true;
  errorMessage.value = "";
  offline.value = false;
  const cacheKey = `product_${props.id}`;
  try {
    const { data } = await getProduct(props.id);
    product.value = data;
    saveCache(cacheKey, data); // US7.1: remember the last-viewed copy for offline viewing
  } catch (err) {
    const cached = loadCache(cacheKey);
    if (cached) {
      product.value = cached.data;
      offline.value = true;
    } else {
      errorMessage.value = err.response?.data?.message || "Listing not found.";
    }
  } finally {
    loading.value = false;
  }
}

watch(() => props.id, load);
onMounted(load);
</script>

<template>
  <p v-if="loading">Loading…</p>
  <p v-else-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
  <template v-else-if="product">
    <p v-if="offline" class="alert alert-info">
      You're offline — showing a cached copy of this listing from your last visit.
    </p>
    <ProductDetail :product="product" />
  </template>
</template>
