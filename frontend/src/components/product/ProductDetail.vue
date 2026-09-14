<script setup>
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../../stores/auth";
import { deleteProduct } from "../../api/products";

const props = defineProps({
  product: {
    type: Object,
    required: true,
  },
});
const emit = defineEmits(["deleted"]);

const auth = useAuthStore();
const router = useRouter();
const deleting = ref(false);
const errorMessage = ref("");

const isOwner = computed(() => auth.user?.id === props.product.sellerId);
const canManage = computed(() => isOwner.value || auth.isAdmin);

const conditionLabels = {
  NEW: "New",
  LIKE_NEW: "Like New",
  GOOD: "Good",
  FAIR: "Fair",
  POOR: "Poor",
};

function formatPrice(price) {
  const value = Number(price);
  return value === 0 ? "R0 · Free" : `R${value.toFixed(2).replace(/\.00$/, "")}`;
}

async function handleDelete() {
  if (!window.confirm("Delete this listing? This can't be undone.")) return;
  deleting.value = true;
  errorMessage.value = "";
  try {
    await deleteProduct(props.product.id);
    emit("deleted");
    router.push({ name: "marketplace" });
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not delete this listing.";
  } finally {
    deleting.value = false;
  }
}
</script>

<template>
  <div class="card" style="max-width: 640px; margin: 0 auto">
    <div class="thumb" style="aspect-ratio: 16/9; margin-bottom: var(--space-4)">
      <img
        v-if="product.imageBase64"
        :src="`data:image/jpeg;base64,${product.imageBase64}`"
        :alt="product.title"
      />
      <span v-else style="font-size: 48px">📦</span>
    </div>

    <span v-if="!product.active" class="badge badge-inactive" style="margin-bottom: var(--space-2)">
      Removed by admin
    </span>

    <div class="row">
      <h1 style="margin: 0">{{ formatPrice(product.price) }}</h1>
      <span class="badge badge-condition">{{ conditionLabels[product.condition] || product.condition }}</span>
    </div>

    <h3>{{ product.title }}</h3>
    <p style="white-space: pre-wrap">{{ product.description }}</p>
    <p class="field-hint">Sold by {{ product.sellerUsername }}</p>

    <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>

    <div v-if="canManage" class="row" style="margin-top: var(--space-4)">
      <router-link
        :to="{ name: 'product-edit', params: { id: product.id } }"
        class="btn btn-outline"
      >
        Edit
      </router-link>
      <button type="button" class="btn btn-danger" :disabled="deleting" @click="handleDelete">
        {{ deleting ? "Deleting…" : "Delete" }}
      </button>
    </div>
  </div>
</template>
