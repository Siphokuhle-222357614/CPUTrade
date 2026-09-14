import client from "./client";

export function listProducts({ category, keyword, minPrice, maxPrice } = {}) {
  const params = {};
  if (category) params.category = category;
  if (keyword) params.keyword = keyword;
  if (minPrice !== undefined && minPrice !== null && minPrice !== "") params.minPrice = minPrice;
  if (maxPrice !== undefined && maxPrice !== null && maxPrice !== "") params.maxPrice = maxPrice;
  return client.get("/products", { params });
}

export function getProduct(id) {
  return client.get(`/products/${id}`);
}

/** The current user's business dashboard — every listing they own, active or not. */
export function listMyProducts() {
  return client.get("/products/mine");
}

export function createProduct(payload) {
  return client.post("/products", payload);
}

export function updateProduct(id, payload) {
  return client.put(`/products/${id}`, payload);
}

export function deleteProduct(id) {
  return client.delete(`/products/${id}`);
}

export function getInterestedBuyers(id) {
  return client.get(`/products/${id}/interested-buyers`);
}

export function markSold(id, soldToUserId) {
  return client.patch(`/products/${id}/mark-sold`, soldToUserId ? { soldToUserId } : {});
}

export function markAvailable(id) {
  return client.patch(`/products/${id}/mark-available`);
}

export function addProductImages(id, images) {
  return client.post(`/products/${id}/images`, { images });
}

export function removeProductImage(id, url) {
  return client.delete(`/products/${id}/images`, { params: { url } });
}
