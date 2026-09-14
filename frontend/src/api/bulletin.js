import client from "./client";

export function listBulletinPosts() {
  return client.get("/bulletin");
}

export function createBulletinPost(payload) {
  return client.post("/bulletin", payload);
}

export function deleteBulletinPost(id) {
  return client.delete(`/bulletin/${id}`);
}
