<script setup>
import SellerRatingBadge from "../product/SellerRatingBadge.vue";
import VerifiedSellerBadge from "../product/VerifiedSellerBadge.vue";
import WishlistButton from "../product/WishlistButton.vue";

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

const campusLabels = {
  BELLVILLE: "Bellville",
  DISTRICT_SIX: "District Six",
  GRANGER_BAY: "Granger Bay",
  MOWBRAY: "Mowbray",
  WELLINGTON: "Wellington",
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
      <span v-if="product.sold" class="badge badge-sold sold-ribbon">Sold</span>
      <WishlistButton :product="product" size="sm" />
    </div>
    <strong>{{ product.title }}</strong>
    <span v-if="product.campus" class="field-hint">📍 {{ campusLabels[product.campus] || product.campus }}</span>
    <div class="row">
      <span class="price">{{ formatPrice(product.price) }}</span>
      <span class="badge badge-condition">{{ conditionLabels[product.condition] || product.condition }}</span>
    </div>
    <div class="row">
      <span v-if="product.freecycle" class="badge badge-free">♻️ Freecycle</span>
      <span v-if="!product.sold && product.quantity > 1" class="badge badge-condition">×{{ product.quantity }}</span>
      <VerifiedSellerBadge :verified="product.sellerVerified" />
    </div>
    <SellerRatingBadge :average="product.sellerRatingAverage" :count="product.sellerRatingCount" />
  </router-link>
</template>
