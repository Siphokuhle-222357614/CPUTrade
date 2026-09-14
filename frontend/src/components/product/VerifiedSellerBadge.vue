<script setup>
defineProps({
  verified: {
    type: Boolean,
    default: false,
  },
});
</script>

<template>
  <span v-if="verified" class="badge badge-verified" title="3+ completed sales on CPUTrade">✅ Verified Seller</span>
</template>
