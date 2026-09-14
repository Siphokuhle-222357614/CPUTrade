<script setup>
import ProductCard from "./ProductCard.vue";

defineProps({
  products: {
    type: Array,
    required: true,
  },
});
</script>

<template>
  <div v-if="products.length" class="product-grid">
    <ProductCard v-for="product in products" :key="product.id" :product="product" />
  </div>
  <p v-else class="empty-state">No listings here yet — be the first to post one!</p>
</template>
