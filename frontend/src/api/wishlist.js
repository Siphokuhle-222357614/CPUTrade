import client from "./client";

export function listWishlist() {
  return client.get("/wishlist");
}

/** Just the product ids — enough to mark hearts filled across a whole grid in one call. */
export function listWishlistIds() {
  return client.get("/wishlist/ids");
}

export function addToWishlist(productId) {
  return client.post(`/wishlist/${productId}`);
}

export function removeFromWishlist(productId) {
  return client.delete(`/wishlist/${productId}`);
}
