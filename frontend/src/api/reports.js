import client from "./client";

export function submitReport(payload) {
  return client.post("/reports", payload);
}

export function listReports(status) {
  return client.get("/admin/reports", { params: status ? { status } : {} });
}

export function reviewReport(id) {
  return client.patch(`/admin/reports/${id}/review`);
}

export function dismissReport(id) {
  return client.patch(`/admin/reports/${id}/dismiss`);
}
