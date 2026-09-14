<script setup>
import ProductCard from "./ProductCard.vue";

defineProps({
  products: {
    type: Array,
    required: true,
  },
  emptyIcon: {
    type: String,
    default: "🔍",
  },
  emptyMessage: {
    type: String,
    default: "No listings here yet — be the first to post one!",
  },
});
</script>

<template>
  <div v-if="products.length" class="product-grid">
    <ProductCard
      v-for="(product, index) in products"
      :key="product.id"
      :product="product"
      :style="{ animationDelay: `${Math.min(index, 12) * 35}ms` }"
    />
  </div>
  <p v-else class="empty-state">
    <span class="empty-state-icon" aria-hidden="true">{{ emptyIcon }}</span>
    {{ emptyMessage }}
  </p>
</template>
