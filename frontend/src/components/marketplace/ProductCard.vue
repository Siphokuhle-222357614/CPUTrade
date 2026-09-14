<script setup>
import SellerRatingBadge from "../product/SellerRatingBadge.vue";

const props = defineProps({
  product: {
    type: Object,
    required: true,
  },
});

const conditionLabels = {
  NEW: "New",
  LIKE_NEW: "Like New",
  GOOD: "Good",
  FAIR: "Fair",
  POOR: "Poor",
};

function formatPrice(price) {
  const value = Number(price);
  return value === 0 ? "R0" : `R${value.toFixed(2).replace(/\.00$/, "")}`;
}
</script>

<template>
  <router-link :to="{ name: 'product-detail', params: { id: product.id } }" class="card product-card">
    <div class="thumb">
      <img v-if="product.imageUrl" :src="product.imageUrl" :alt="product.title" />
      <span v-else style="font-size: 32px">📦</span>
    </div>
    <strong>{{ product.title }}</strong>
    <div class="row">
      <span class="price">{{ formatPrice(product.price) }}</span>
      <span class="badge badge-condition">{{ conditionLabels[product.condition] || product.condition }}</span>
    </div>
    <span v-if="product.freecycle" class="badge badge-free">♻️ Freecycle</span>
    <SellerRatingBadge :average="product.sellerRatingAverage" :count="product.sellerRatingCount" />
  </router-link>
</template>
