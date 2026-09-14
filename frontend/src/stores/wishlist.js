import { defineStore } from "pinia";
import * as wishlistApi from "../api/wishlist";
import { useToastStore } from "./toast";

// Holds just the ids (a Set, for O(1) "is this hearted?" checks across a
// whole grid) -- the full product objects only get fetched on the dedicated
// Wishlist page itself, not kept live here.
export const useWishlistStore = defineStore("wishlist", {
  state: () => ({
    ids: new Set(),
    loaded: false,
  }),

  actions: {
    async load() {
      try {
        const { data } = await wishlistApi.listWishlistIds();
        this.ids = new Set(data);
        this.loaded = true;
      } catch {
        // Not fatal -- hearts just won't show as filled until the next successful load.
      }
    },

    /** Called on logout so the next person to use this tab doesn't inherit someone else's hearts. */
    clear() {
      this.ids = new Set();
      this.loaded = false;
    },

    has(productId) {
      return this.ids.has(productId);
    },

    async toggle(product) {
      const toast = useToastStore();
      const wasWishlisted = this.ids.has(product.id);

      // Optimistic -- a heart that visibly fills/empties instantly reads as
      // far more responsive than waiting on a round trip for a one-bit toggle.
      if (wasWishlisted) {
        this.ids.delete(product.id);
      } else {
        this.ids.add(product.id);
      }

      try {
        if (wasWishlisted) {
          await wishlistApi.removeFromWishlist(product.id);
        } else {
          await wishlistApi.addToWishlist(product.id);
          toast.success(`Watching "${product.title}" — we'll let you know about a price drop or if it sells.`);
        }
      } catch (err) {
        // Roll back the optimistic change.
        if (wasWishlisted) {
          this.ids.add(product.id);
        } else {
          this.ids.delete(product.id);
        }
        toast.error(err.response?.data?.message || "Could not update your wishlist.");
      }
    },
  },
});
