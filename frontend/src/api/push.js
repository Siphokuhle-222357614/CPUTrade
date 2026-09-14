import client from "./client";

export function getPushPublicKey() {
  return client.get("/push/public-key");
}

/** payload is a raw PushSubscription.toJSON() object: { endpoint, keys: { p256dh, auth } }. */
export function subscribePush(subscription) {
  return client.post("/push/subscribe", subscription);
}

export function unsubscribePush(endpoint) {
  return client.delete("/push/subscribe", { params: { endpoint } });
}
