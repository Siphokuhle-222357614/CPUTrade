<script setup>
import VendorRequestList from "../components/admin/VendorRequestList.vue";
import ListingModerationList from "../components/admin/ListingModerationList.vue";
import ReportQueue from "../components/admin/ReportQueue.vue";
</script>

<template>
  <h1>Admin</h1>
  <div style="display: flex; flex-direction: column; gap: var(--space-5)">
    <ReportQueue />
    <VendorRequestList />
    <ListingModerationList />
  </div>
</template>
