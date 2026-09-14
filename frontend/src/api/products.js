import client from "./client";

export function listProducts(category) {
  return client.get("/products", { params: category ? { category } : {} });
}

export function getProduct(id) {
  return client.get(`/products/${id}`);
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
