<script setup>
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../../stores/auth";
import { deleteProduct, markAvailable } from "../../api/products";
import { startConversation } from "../../api/chat";
import SellerRatingBadge from "./SellerRatingBadge.vue";
import VerifiedSellerBadge from "./VerifiedSellerBadge.vue";
import WishlistButton from "./WishlistButton.vue";
import RatingForm from "./RatingForm.vue";
import MarkSoldDialog from "./MarkSoldDialog.vue";
import ConfirmDialog from "../common/ConfirmDialog.vue";
import ReportDialog from "../trust/ReportDialog.vue";
import ConfettiBurst from "../common/ConfettiBurst.vue";
import { useToastStore } from "../../stores/toast";

const toast = useToastStore();

const props = defineProps({
  product: {
    type: Object,
    required: true,
  },
});
const emit = defineEmits(["deleted", "updated"]);

const auth = useAuthStore();
const router = useRouter();
const deleting = ref(false);
const messaging = ref(false);
const unmarkingSold = ref(false);
const errorMessage = ref("");
const showDeleteConfirm = ref(false);
const showReportDialog = ref(false);
const showMarkSoldDialog = ref(false);
const activePhotoIndex = ref(0);
const soldBurstKey = ref(0);

const isOwner = computed(() => auth.user?.id === props.product.sellerId);
const canManage = computed(() => isOwner.value || auth.isAdmin);
// Once it's sold there's nothing left to buy, so starting a new chat about it is a dead end.
const canMessageSeller = computed(() => auth.isAuthenticated && !isOwner.value && !props.product.sold);
// Rating is tied to an actual completed trade, not just "anyone who looked at the listing".
const canRateSeller = computed(() => auth.isAuthenticated && !isOwner.value && props.product.sold);
const canReport = computed(() => auth.isAuthenticated && !isOwner.value);
const photos = computed(() => (props.product.imageUrls?.length ? props.product.imageUrls : []));

async function handleMessageSeller() {
  messaging.value = true;
  errorMessage.value = "";
  try {
    const { data } = await startConversation(props.product.id);
    router.push({ name: "chat", params: { id: data.id } });
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not start a conversation.";
  } finally {
    messaging.value = false;
  }
}

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
  return value === 0 ? "R0 · Free" : `R${value.toFixed(2).replace(/\.00$/, "")}`;
}

// A student sharing a listing into a res/campus WhatsApp group is the
// single most likely way this app spreads organically -- wa.me works from
// any device (installed app, WhatsApp Web, or the "open in browser"
// fallback) with no API key or backend involved.
function shareToWhatsApp() {
  const price = formatPrice(props.product.price);
  const text = `Check this out on CPUTrade: ${props.product.title} (${price})\n${window.location.href}`;
  window.open(`https://wa.me/?text=${encodeURIComponent(text)}`, "_blank", "noopener");
}

async function handleDelete() {
  showDeleteConfirm.value = false;
  deleting.value = true;
  errorMessage.value = "";
  try {
    await deleteProduct(props.product.id);
    toast.info("Listing deleted.");
    emit("deleted");
    router.push({ name: "marketplace" });
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not delete this listing.";
  } finally {
    deleting.value = false;
  }
}

function handleMarkedSold(updated) {
  showMarkSoldDialog.value = false;
  soldBurstKey.value++;
  toast.success(
    updated.sold
      ? "Marked as sold — nice work! 🎉"
      : `Nice, one sold! ${updated.quantity} left in stock. 🎉`
  );
  emit("updated", updated);
}

async function handleMarkAvailable() {
  unmarkingSold.value = true;
  errorMessage.value = "";
  try {
    const { data } = await markAvailable(props.product.id);
    toast.info("Back on the marketplace.");
    emit("updated", data);
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not update this listing.";
  } finally {
    unmarkingSold.value = false;
  }
}
</script>

<template>
  <div class="card" style="max-width: 640px; margin: 0 auto">
    <div class="thumb" style="aspect-ratio: 16/9; margin-bottom: var(--space-2)">
      <img v-if="photos.length" :src="photos[activePhotoIndex]" :alt="product.title" />
      <span v-else style="font-size: 48px">📦</span>
    </div>
    <div v-if="photos.length > 1" class="row" style="gap: var(--space-2); margin-bottom: var(--space-4)">
      <button
        v-for="(photo, index) in photos"
        :key="photo"
        type="button"
        class="thumb-pick"
        :class="{ active: index === activePhotoIndex }"
        @click="activePhotoIndex = index"
      >
        <img :src="photo" :alt="`${product.title} photo ${index + 1}`" />
      </button>
    </div>

    <div class="row" style="gap: var(--space-2); position: relative">
      <span v-if="product.sold" class="badge badge-sold">✅ Sold</span>
      <span v-else-if="!product.active" class="badge badge-inactive">Removed by admin</span>
      <ConfettiBurst :trigger="soldBurstKey" />
    </div>

    <div class="row">
      <h1 style="margin: 0">{{ formatPrice(product.price) }}</h1>
      <span class="badge badge-condition">{{ conditionLabels[product.condition] || product.condition }}</span>
      <WishlistButton :product="product" size="lg" />
    </div>
    <span v-if="product.freecycle" class="badge badge-free" style="margin-top: var(--space-2)">♻️ Freecycle</span>
    <span v-if="product.campus" class="badge badge-condition" style="margin-top: var(--space-2)">
      📍 {{ campusLabels[product.campus] || product.campus }}
    </span>
    <span v-if="product.openToSwap" class="badge badge-free" style="margin-top: var(--space-2)">🔄 Open to Trade</span>

    <h3>{{ product.title }}</h3>
    <p style="white-space: pre-wrap">{{ product.description }}</p>
    <p v-if="product.openToSwap && product.swapPreferences" class="field-hint">
      🔄 Looking for: {{ product.swapPreferences }}
    </p>
    <p class="field-hint">
      Sold by {{ product.sellerUsername }} ·
      <SellerRatingBadge :average="product.sellerRatingAverage" :count="product.sellerRatingCount" />
      <VerifiedSellerBadge :verified="product.sellerVerified" />
      · {{ product.viewCount }} view{{ product.viewCount === 1 ? "" : "s" }}
      <template v-if="product.watcherCount > 0"> · 👀 {{ product.watcherCount }} watching</template>
    </p>
    <p v-if="!product.sold && product.quantity > 1" class="field-hint">{{ product.quantity }} available</p>
    <p v-if="product.sold" class="field-hint">
      Sold{{ product.soldToUsername ? ` to ${product.soldToUsername}` : "" }}
    </p>

    <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>

    <div class="row" style="margin-top: var(--space-4)">
      <button v-if="canMessageSeller" type="button" class="btn btn-primary" :disabled="messaging" @click="handleMessageSeller">
        {{ messaging ? "Starting chat…" : "💬 Message Seller" }}
      </button>
      <button v-if="canReport" type="button" class="btn btn-outline" @click="showReportDialog = true">
        🚩 Report
      </button>
      <button type="button" class="btn btn-outline" @click="shareToWhatsApp">
        📤 Share to WhatsApp
      </button>

      <template v-if="canManage">
        <router-link :to="{ name: 'product-edit', params: { id: product.id } }" class="btn btn-outline">
          Edit
        </router-link>
        <button v-if="!product.sold" type="button" class="btn btn-accent" @click="showMarkSoldDialog = true">
          {{ product.quantity > 1 ? "Sell One" : "Mark as Sold" }}
        </button>
        <button v-else type="button" class="btn btn-outline" :disabled="unmarkingSold" @click="handleMarkAvailable">
          {{ unmarkingSold ? "Updating…" : "Mark as Available Again" }}
        </button>
        <button type="button" class="btn btn-danger" :disabled="deleting" @click="showDeleteConfirm = true">
          {{ deleting ? "Deleting…" : "Delete" }}
        </button>
      </template>
    </div>

    <RatingForm v-if="canRateSeller" :product-id="product.id" />

    <ConfirmDialog
      :open="showDeleteConfirm"
      title="Delete this listing?"
      message="This can't be undone."
      confirm-label="Delete"
      danger
      @confirm="handleDelete"
      @cancel="showDeleteConfirm = false"
    />
    <ReportDialog
      :open="showReportDialog"
      target-type="PRODUCT"
      :target-id="product.id"
      :target-label="product.title"
      @close="showReportDialog = false"
    />
    <MarkSoldDialog
      :open="showMarkSoldDialog"
      :product-id="product.id"
      :quantity="product.quantity"
      @close="showMarkSoldDialog = false"
      @sold="handleMarkedSold"
    />
  </div>
</template>

<style scoped>
.thumb-pick {
  width: 56px;
  height: 56px;
  padding: 0;
  border: 2px solid transparent;
  border-radius: var(--radius-card);
  background: none;
  cursor: pointer;
  overflow: hidden;
}
.thumb-pick.active {
  border-color: var(--color-primary-light);
}
.thumb-pick img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
