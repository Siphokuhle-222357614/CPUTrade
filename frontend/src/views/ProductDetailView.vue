<script setup>
import { onMounted, ref, watch } from "vue";
import ProductDetail from "../components/product/ProductDetail.vue";
import { getProduct } from "../api/products";

const props = defineProps({
  id: {
    type: [String, Number],
    required: true,
  },
});

const product = ref(null);
const loading = ref(true);
const errorMessage = ref("");

async function load() {
  loading.value = true;
  errorMessage.value = "";
  try {
    const { data } = await getProduct(props.id);
    product.value = data;
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Listing not found.";
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
  <ProductDetail v-else-if="product" :product="product" />
</template>
