<script setup>
import { computed, onMounted, ref } from "vue";
import { getProduct } from "../api/products";
import { checkout } from "../api/orders";
import { useAuthStore } from "../stores/auth";

const props = defineProps({
  id: {
    type: [String, Number],
    required: true,
  },
});

const auth = useAuthStore();
const product = ref(null);
const order = ref(null);
const loading = ref(true);
const placingOrder = ref(false);
const errorMessage = ref("");
const available = computed(
  () => product.value?.active && !product.value?.sold && Number(product.value?.quantity) > 0
);
const ownListing = computed(
  () => product.value && Number(product.value.sellerId) === Number(auth.user?.id)
);
const canOrder = computed(() => available.value && !ownListing.value && !auth.isAdmin);

function formatPrice(price) {
  const value = Number(price);
  return value === 0 ? "R0 · Free" : `R${value.toFixed(2).replace(/\.00$/, "")}`;
}

async function loadProduct() {
  loading.value = true;
  errorMessage.value = "";
  try {
    const { data } = await getProduct(props.id);
    product.value = data;
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not load this listing.";
  } finally {
    loading.value = false;
  }
}

async function placeOrder() {
  placingOrder.value = true;
  errorMessage.value = "";
  try {
    const { data } = await checkout(props.id);
    order.value = data;
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not place this order.";
  } finally {
    placingOrder.value = false;
  }
}

onMounted(loadProduct);
</script>

<template>
  <section class="card" style="max-width: 640px; margin: 0 auto">
    <h1>Checkout</h1>
    <p class="field-hint">
      This is a local payment simulation. No money will be charged, and no card or banking details are collected.
    </p>

    <p v-if="loading" role="status" aria-live="polite">Loading listing…</p>
    <p v-else-if="errorMessage && !product" class="alert alert-error" role="alert">{{ errorMessage }}</p>
    <template v-else-if="product">
      <div class="row" style="justify-content: space-between">
        <div>
          <h2>{{ product.title }}</h2>
          <p class="field-hint">Sold by {{ product.sellerUsername }}</p>
        </div>
        <strong>{{ formatPrice(product.price) }}</strong>
      </div>
      <p>Quantity: 1</p>
      <p class="row" style="justify-content: space-between">
        <strong>Total (simulated)</strong>
        <strong>{{ formatPrice(product.price) }}</strong>
      </p>

      <p v-if="!available" class="alert alert-info">
        This listing is no longer available to order.
      </p>
      <p v-else-if="ownListing" class="alert alert-info">
        You cannot order your own listing.
      </p>
      <p v-if="errorMessage" class="alert alert-error" role="alert">{{ errorMessage }}</p>
      <div v-if="!order" class="row">
        <button
          type="button"
          class="btn btn-accent"
          :aria-busy="placingOrder"
          :disabled="placingOrder || !canOrder"
          @click="placeOrder"
        >
          {{ placingOrder ? "Placing order…" : "Place order · simulated payment" }}
        </button>
        <router-link :to="{ name: 'product-detail', params: { id: product.id } }" class="btn btn-outline">
          Back to listing
        </router-link>
      </div>

      <div v-else class="alert alert-info" role="status">
        <h2>Order placed</h2>
        <p>Order #{{ order.id }} is recorded. Payment is simulated; no charge was made.</p>
        <div class="row">
          <router-link :to="{ name: 'orders' }" class="btn btn-primary">View my orders</router-link>
          <router-link :to="{ name: 'product-detail', params: { id: product.id } }" class="btn btn-outline">
            Return to listing
          </router-link>
        </div>
      </div>
    </template>
  </section>
</template>
