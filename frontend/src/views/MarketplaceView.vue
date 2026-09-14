<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import CategoryFilterPills from "../components/marketplace/CategoryFilterPills.vue";
import SearchAndPriceFilter from "../components/marketplace/SearchAndPriceFilter.vue";
import ProductGrid from "../components/marketplace/ProductGrid.vue";
import SkeletonCard from "../components/common/SkeletonCard.vue";
import { listProducts } from "../api/products";
import { loadCache, saveCache } from "../utils/offlineCache";
import { formatDayLabel } from "../utils/datetime";
import { useAuthStore } from "../stores/auth";

const CACHE_KEY = "marketplace_all";

const auth = useAuthStore();
const category = ref(null);
const keyword = ref("");
const minPrice = ref("");
const maxPrice = ref("");
const products = ref([]);
const loading = ref(true);
const errorMessage = ref("");
const offline = ref(false);

let debounceTimer = null;

async function load() {
  loading.value = true;
  errorMessage.value = "";
  const noFilters = !category.value && !keyword.value && !minPrice.value && !maxPrice.value;
  try {
    const { data } = await listProducts({
      category: category.value,
      keyword: keyword.value,
      minPrice: minPrice.value,
      maxPrice: maxPrice.value,
    });
    products.value = data;
    offline.value = false;
    // US7.1: only cache the unfiltered feed — that's the useful "last known
    // good" view to fall back to when a filtered request fails offline.
    if (noFilters) saveCache(CACHE_KEY, data);
    // The hero stats below should describe the whole marketplace, not
    // whatever's currently filtered — piggyback on an unfiltered fetch so
    // there's no separate endpoint to add just for a few numbers.
    if (noFilters) allListingsForStats.value = data;
  } catch (err) {
    const cached = noFilters ? loadCache(CACHE_KEY) : null;
    if (cached) {
      products.value = cached.data;
      offline.value = true;
    } else {
      errorMessage.value = "Could not load listings — is the backend running?";
    }
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

// --- Hero: a real, live snapshot of the marketplace, not decoration -------
const allListingsForStats = ref([]);

const greeting = computed(() => {
  const hour = new Date().getHours();
  const timeGreeting = hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";
  const name = auth.user?.username;
  return name ? `${timeGreeting}, ${name} 👋` : `${timeGreeting}, welcome to CPUTrade 👋`;
});

const heroStats = computed(() => {
  const listings = allListingsForStats.value;
  return {
    total: listings.length,
    free: listings.filter((p) => p.freecycle).length,
    categories: new Set(listings.map((p) => p.category)).size,
    today: listings.filter((p) => formatDayLabel(p.createdAt) === "Today").length,
  };
});
</script>

<template>
  <div class="hero-band">
    <div class="hero-band-content">
      <h1 class="hero-greeting">{{ greeting }}</h1>
      <p class="hero-subtitle">Textbooks, tech, clothes and more — from fellow CPUT students.</p>
      <div class="hero-stats">
        <div class="hero-stat">
          <strong>{{ heroStats.total }}</strong>
          <span>Live listings</span>
        </div>
        <div class="hero-stat">
          <strong>{{ heroStats.today }}</strong>
          <span>Posted today</span>
        </div>
        <div class="hero-stat">
          <strong>{{ heroStats.free }}</strong>
          <span>♻️ Free right now</span>
        </div>
        <div class="hero-stat">
          <strong>{{ heroStats.categories }}</strong>
          <span>Categories active</span>
        </div>
      </div>
    </div>
  </div>

  <CategoryFilterPills v-model="category" />
  <SearchAndPriceFilter v-model:keyword="keyword" v-model:min-price="minPrice" v-model:max-price="maxPrice" />
  <p v-if="offline" class="alert alert-info">
    You're offline — showing listings cached from your last visit. They may be out of date.
  </p>
  <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
  <div v-if="loading" class="product-grid">
    <SkeletonCard v-for="n in 8" :key="n" />
  </div>
  <ProductGrid v-else :products="products" />
</template>
