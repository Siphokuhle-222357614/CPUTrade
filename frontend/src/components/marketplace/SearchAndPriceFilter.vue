<script setup>
const props = defineProps({
  keyword: { type: String, default: "" },
  minPrice: { type: [String, Number], default: "" },
  maxPrice: { type: [String, Number], default: "" },
});
const emit = defineEmits(["update:keyword", "update:minPrice", "update:maxPrice"]);
</script>

<template>
  <div class="row" style="align-items: flex-end; margin-bottom: var(--space-4)">
    <div class="field" style="flex: 2 1 200px; margin-bottom: 0">
      <label for="keyword">Search</label>
      <input
        id="keyword"
        :value="keyword"
        type="search"
        placeholder="e.g. Lenovo Legion laptop"
        @input="$emit('update:keyword', $event.target.value)"
      />
    </div>
    <div class="field" style="flex: 1 1 100px; margin-bottom: 0">
      <label for="minPrice">Min price (R)</label>
      <input
        id="minPrice"
        :value="minPrice"
        type="number"
        min="0"
        step="0.01"
        placeholder="0"
        @input="$emit('update:minPrice', $event.target.value)"
      />
    </div>
    <div class="field" style="flex: 1 1 100px; margin-bottom: 0">
      <label for="maxPrice">Max price (R)</label>
      <input
        id="maxPrice"
        :value="maxPrice"
        type="number"
        min="0"
        step="0.01"
        placeholder="Any"
        @input="$emit('update:maxPrice', $event.target.value)"
      />
    </div>
  </div>
</template>
