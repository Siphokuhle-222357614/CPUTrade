import client from "./client";

export function listBulletinPosts(type) {
  return client.get("/bulletin", { params: type ? { type } : {} });
}

export function createBulletinPost(payload) {
  return client.post("/bulletin", payload);
}

export function setBulletinPostResolved(id, resolved) {
  return client.patch(`/bulletin/${id}/resolved`, null, { params: { resolved } });
}

export function deleteBulletinPost(id) {
  return client.delete(`/bulletin/${id}`);
}
