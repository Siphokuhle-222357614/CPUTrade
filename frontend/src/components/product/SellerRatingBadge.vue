<script setup>
const props = defineProps({
  average: { type: Number, default: null },
  count: { type: Number, default: 0 },
});
</script>

<template>
  <span v-if="average !== null" class="field-hint" style="display: inline-flex; align-items: center; gap: 4px">
    ⭐ {{ average.toFixed(1) }} <span>({{ count }} {{ count === 1 ? "rating" : "ratings" }})</span>
  </span>
  <span v-else class="field-hint">No ratings yet</span>
</template>
