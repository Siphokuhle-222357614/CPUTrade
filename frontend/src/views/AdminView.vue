<script setup>
import VendorRequestList from "../components/admin/VendorRequestList.vue";
import ListingModerationList from "../components/admin/ListingModerationList.vue";
import ReportQueue from "../components/admin/ReportQueue.vue";
import AppealQueue from "../components/admin/AppealQueue.vue";
import SuspendedUsersList from "../components/admin/SuspendedUsersList.vue";
</script>

<template>
  <h1>Admin</h1>
  <div style="display: flex; flex-direction: column; gap: var(--space-5)">
    <ReportQueue />
    <AppealQueue />
    <SuspendedUsersList />
    <VendorRequestList />
    <ListingModerationList />
  </div>
</template>
