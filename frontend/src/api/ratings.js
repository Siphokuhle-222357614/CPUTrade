import client from "./client";

export function rateSeller(productId, { score, comment }) {
  return client.post(`/products/${productId}/ratings`, { score, comment });
}
