import client from "./client";

export function listSavedSearches() {
  return client.get("/saved-searches");
}

export function createSavedSearch(payload) {
  return client.post("/saved-searches", payload);
}

export function deleteSavedSearch(id) {
  return client.delete(`/saved-searches/${id}`);
}
