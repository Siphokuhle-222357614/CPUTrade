<script setup>
import { onMounted, ref } from "vue";
import ProductGrid from "../components/marketplace/ProductGrid.vue";
import { listWishlist } from "../api/wishlist";

const products = ref([]);
const loading = ref(true);
const errorMessage = ref("");

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

onMounted(load);
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
  </div>
</template>
