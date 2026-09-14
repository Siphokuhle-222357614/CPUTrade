import client from "./client";

export function submitAppeal(payload) {
  return client.post("/appeals", payload);
}

export function listAppeals(status) {
  return client.get("/admin/appeals", { params: status ? { status } : {} });
}

export function approveAppeal(id) {
  return client.patch(`/admin/appeals/${id}/approve`);
}

export function rejectAppeal(id) {
  return client.patch(`/admin/appeals/${id}/reject`);
}
