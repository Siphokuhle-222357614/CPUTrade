<script setup>
import { onMounted, ref, watch } from "vue";
import CategoryFilterPills from "../components/marketplace/CategoryFilterPills.vue";
import ProductGrid from "../components/marketplace/ProductGrid.vue";
import { listProducts } from "../api/products";

const category = ref(null);
const products = ref([]);
const loading = ref(true);
const errorMessage = ref("");

async function load() {
  loading.value = true;
  errorMessage.value = "";
  try {
    const { data } = await listProducts(category.value);
    products.value = data;
  } catch (err) {
    errorMessage.value = "Could not load listings — is the backend running?";
  } finally {
    loading.value = false;
  }
}

watch(category, load);
onMounted(load);
</script>

<template>
  <h1>Marketplace</h1>
  <CategoryFilterPills v-model="category" />
  <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
  <p v-if="loading">Loading…</p>
  <ProductGrid v-else :products="products" />
</template>
