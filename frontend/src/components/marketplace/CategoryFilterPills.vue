<script setup>
defineProps({
  modelValue: {
    type: String,
    default: null,
  },
});
defineEmits(["update:modelValue"]);

const categories = [
  { value: null, label: "All" },
  { value: "TEXTBOOKS", label: "Textbooks" },
  { value: "ELECTRONICS", label: "Electronics" },
  { value: "CLOTHING", label: "Clothing" },
  { value: "SERVICES", label: "Services" },
  { value: "OTHER", label: "Other" },
];
</script>

<template>
  <div class="pill-row">
    <button
      v-for="cat in categories"
      :key="cat.label"
      type="button"
      class="pill"
      :class="{ active: modelValue === cat.value }"
      @click="$emit('update:modelValue', cat.value)"
    >
      {{ cat.label }}
    </button>
  </div>
</template>
