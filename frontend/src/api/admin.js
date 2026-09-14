import client from "./client";

export function getPendingVendorRequests() {
  return client.get("/admin/vendor-requests");
}

export function approveVendor(userId) {
  return client.post(`/admin/vendor-requests/${userId}/approve`);
}

export function getAllListings() {
  return client.get("/admin/listings");
}

export function deactivateListing(id) {
  return client.patch(`/admin/listings/${id}/deactivate`);
}
