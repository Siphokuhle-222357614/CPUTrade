<script setup>
import { computed, onMounted, ref } from "vue";
import { useAuthStore } from "../stores/auth";
import { listMyProducts } from "../api/products";
import { myConversations } from "../api/chat";

const auth = useAuthStore();
const listings = ref([]);
const conversations = ref([]);
const loading = ref(true);
const errorMessage = ref("");

const conditionLabels = {
  NEW: "New",
  LIKE_NEW: "Like New",
  GOOD: "Good",
  FAIR: "Fair",
  POOR: "Poor",
};

const isPendingVendor = computed(() => auth.user?.role === "VENDOR" && !auth.user?.vendorApproved);

const myConversationsAsSeller = computed(() =>
  conversations.value.filter((c) => c.sellerId === auth.user?.id)
);

const stats = computed(() => {
  const active = listings.value.filter((p) => p.active);
  const totalViews = listings.value.reduce((sum, p) => sum + (p.viewCount || 0), 0);
  const rated = listings.value.find((p) => p.sellerRatingCount > 0);
  return {
    total: listings.value.length,
    active: active.length,
    totalViews,
    ratingAverage: rated?.sellerRatingAverage ?? null,
    ratingCount: rated?.sellerRatingCount ?? 0,
    unreadChats: myConversationsAsSeller.value.length,
  };
});

function formatPrice(price) {
  const value = Number(price);
  return value === 0 ? "Free" : `R${value.toFixed(2).replace(/\.00$/, "")}`;
}

function otherPartyUsername(conversation) {
  return conversation.buyerUsername;
}

async function load() {
  loading.value = true;
  errorMessage.value = "";
  try {
    const [{ data: myListings }, { data: myChats }] = await Promise.all([
      listMyProducts(),
      myConversations(),
    ]);
    listings.value = myListings;
    conversations.value = myChats;
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not load your dashboard.";
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <h1>My Business Dashboard</h1>
  <p class="field-hint">Everything about what you're selling — listings, views, ratings, and buyer messages.</p>

  <p v-if="isPendingVendor" class="alert alert-info" style="margin-top: var(--space-4)">
    ⏳ Your vendor account is pending admin approval. You can see this dashboard, but can't create listings or make
    changes yet.
  </p>
  <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
  <p v-if="loading">Loading…</p>

  <template v-else>
    <!-- Stat cards -->
    <div class="product-grid" style="margin-top: var(--space-4)">
      <div class="card">
        <p class="field-hint" style="margin: 0">Listings</p>
        <h2 style="margin: 0">{{ stats.total }}</h2>
        <p class="field-hint" style="margin: 0">{{ stats.active }} active</p>
      </div>
      <div class="card">
        <p class="field-hint" style="margin: 0">Total Views</p>
        <h2 style="margin: 0">{{ stats.totalViews }}</h2>
      </div>
      <div class="card">
        <p class="field-hint" style="margin: 0">Seller Rating</p>
        <h2 style="margin: 0">{{ stats.ratingAverage != null ? stats.ratingAverage.toFixed(1) : "—" }}</h2>
        <p class="field-hint" style="margin: 0">{{ stats.ratingCount }} rating{{ stats.ratingCount === 1 ? "" : "s" }}</p>
      </div>
      <div class="card">
        <p class="field-hint" style="margin: 0">Buyer Chats</p>
        <h2 style="margin: 0">{{ stats.unreadChats }}</h2>
      </div>
    </div>

    <!-- My Listings -->
    <h3 style="margin-top: var(--space-6)">My Listings</h3>
    <router-link v-if="!isPendingVendor" to="/products/new" class="btn btn-accent" style="margin-bottom: var(--space-4)">
      + New Listing
    </router-link>
    <p v-if="!listings.length" class="empty-state">You haven't listed anything yet.</p>
    <div v-else class="table-list">
      <router-link
        v-for="product in listings"
        :key="product.id"
        :to="{ name: 'product-detail', params: { id: product.id } }"
        class="card row"
        style="text-decoration: none; color: inherit"
      >
        <span>
          <strong>{{ product.title }}</strong> — {{ formatPrice(product.price) }} —
          {{ conditionLabels[product.condition] || product.condition }}
        </span>
        <span class="row" style="width: auto; gap: var(--space-2)">
          <span class="field-hint">{{ product.viewCount }} view{{ product.viewCount === 1 ? "" : "s" }}</span>
          <span v-if="product.active" class="badge badge-success">Active</span>
          <span v-else class="badge badge-inactive">Removed</span>
        </span>
      </router-link>
    </div>

    <!-- Messages about my business -->
    <h3 style="margin-top: var(--space-6)">Messages From Buyers</h3>
    <p v-if="!myConversationsAsSeller.length" class="empty-state">No one has messaged you about a listing yet.</p>
    <div v-else class="table-list">
      <router-link
        v-for="conversation in myConversationsAsSeller"
        :key="conversation.id"
        :to="{ name: 'chat', params: { id: conversation.id } }"
        class="card row"
        style="text-decoration: none; color: inherit"
      >
        <span>
          <strong>{{ conversation.productTitle }}</strong> — {{ otherPartyUsername(conversation) }}
        </span>
      </router-link>
    </div>
  </template>
</template>
