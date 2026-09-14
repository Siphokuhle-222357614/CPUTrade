<script setup>
// A heart toggle for "watch this listing" -- shown on the marketplace grid
// (overlaid on the thumbnail) and on the detail page (inline, larger).
import { computed } from "vue";
import { useAuthStore } from "../../stores/auth";
import { useWishlistStore } from "../../stores/wishlist";

const props = defineProps({
  product: {
    type: Object,
    required: true,
  },
  size: {
    type: String,
    default: "sm", // "sm" (grid overlay) | "lg" (detail page)
  },
});

const auth = useAuthStore();
const wishlist = useWishlistStore();

const isOwner = computed(() => auth.user?.id === props.product.sellerId);
// Nothing to watch about a listing that's already gone.
const canWishlist = computed(() => auth.isAuthenticated && !isOwner.value && !props.product.sold);
const wishlisted = computed(() => wishlist.has(props.product.id));

function handleClick(event) {
  event.preventDefault(); // this button often sits inside a <router-link> card
  event.stopPropagation();
  wishlist.toggle(props.product);
}
</script>

<template>
  <button
    v-if="canWishlist"
    type="button"
    class="wishlist-btn"
    :class="[`wishlist-btn-${size}`, { active: wishlisted }]"
    :aria-pressed="wishlisted"
    :aria-label="wishlisted ? 'Remove from wishlist' : 'Add to wishlist'"
    :title="wishlisted ? 'Remove from wishlist' : 'Add to wishlist'"
    @click="handleClick"
  >
    {{ wishlisted ? "❤️" : "🤍" }}
  </button>
</template>

<style scoped>
.wishlist-btn {
  background: rgba(255, 255, 255, 0.9);
  border: none;
  border-radius: 50%;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  transition: transform var(--transition-base, 0.15s ease);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
}
.wishlist-btn:hover {
  transform: scale(1.12);
}
.wishlist-btn:active {
  transform: scale(0.92);
}
.wishlist-btn-sm {
  position: absolute;
  top: var(--space-2);
  right: var(--space-2);
  width: 32px;
  height: 32px;
  font-size: 1rem;
  z-index: 2;
}
.wishlist-btn-lg {
  width: 44px;
  height: 44px;
  font-size: 1.3rem;
}
</style>
