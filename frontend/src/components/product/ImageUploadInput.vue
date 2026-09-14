<script setup>
import { ref } from "vue";

// US2.1: JPEG/PNG only, decoded size <= 500KB. This is a fast client-side
// pre-check for UX only — the server re-validates on the decoded bytes,
// since Base64 inflates size by ~33% and the client can't be trusted.
const MAX_BYTES = 500 * 1024;
const ALLOWED_TYPES = ["image/jpeg", "image/png"];

const props = defineProps({
  modelValue: {
    type: String,
    default: "",
  },
});
const emit = defineEmits(["update:modelValue"]);

const error = ref("");
const previewBase64 = ref(props.modelValue ? stripPrefix(props.modelValue) : "");

function stripPrefix(dataUrlOrBase64) {
  const commaIndex = dataUrlOrBase64.indexOf(",");
  return dataUrlOrBase64.startsWith("data:") && commaIndex >= 0
    ? dataUrlOrBase64.slice(commaIndex + 1)
    : dataUrlOrBase64;
}

function handleFileChange(event) {
  error.value = "";
  const file = event.target.files?.[0];
  if (!file) return;

  if (!ALLOWED_TYPES.includes(file.type)) {
    error.value = "Only JPEG or PNG images are allowed.";
    event.target.value = "";
    return;
  }
  if (file.size > MAX_BYTES) {
    error.value = `Image is ${(file.size / 1024).toFixed(0)}KB — the limit is 500KB.`;
    event.target.value = "";
    return;
  }

  const reader = new FileReader();
  reader.onload = () => {
    const base64 = stripPrefix(reader.result);
    previewBase64.value = base64;
    emit("update:modelValue", base64);
  };
  reader.readAsDataURL(file);
}

function clearImage() {
  previewBase64.value = "";
  emit("update:modelValue", "");
}
</script>

<template>
  <div class="field">
    <label>Photo (JPEG/PNG, max 500KB)</label>
    <input type="file" accept="image/jpeg,image/png" @change="handleFileChange" />
    <p v-if="error" class="field-error">{{ error }}</p>
    <div v-if="previewBase64" style="margin-top: var(--space-2)">
      <img
        :src="`data:image/jpeg;base64,${previewBase64}`"
        alt="Preview"
        style="max-width: 160px; border-radius: var(--radius-card)"
      />
      <button type="button" class="btn btn-outline" style="margin-top: var(--space-2)" @click="clearImage">
        Remove photo
      </button>
    </div>
  </div>
</template>
