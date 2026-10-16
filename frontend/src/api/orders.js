import client from "./client";

export function checkout(productId) {
  return client.post("/orders/checkout", { productId });
}

export function listMyOrders() {
  return client.get("/orders/mine");
}

export function confirmOrderReceived(orderId) {
  return client.patch(`/orders/${orderId}/confirm-received`);
}

export function cancelOrder(orderId) {
  return client.patch(`/orders/${orderId}/cancel`);
}

export function markOrderReadyForHandoff(orderId) {
  return client.patch(`/orders/${orderId}/ready-for-handoff`);
}
