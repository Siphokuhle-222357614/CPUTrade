<script setup>
import { ref } from "vue";

// US2.1: JPEG/PNG only, decoded size <= 4MB per photo. This is a fast
// client-side pre-check for UX only — the server re-validates on the
// decoded bytes, since Base64 inflates size by ~33% and the client can't be
// trusted. Up to MAX_PHOTOS per listing, matching the server-side cap.
const MAX_BYTES = 4 * 1024 * 1024;
const MAX_PHOTOS = 6;
const ALLOWED_TYPES = ["image/jpeg", "image/png"];

const props = defineProps({
  // Array of new-upload Base64 strings (no data: prefix), in upload order.
  modelValue: {
    type: Array,
    default: () => [],
  },
});
const emit = defineEmits(["update:modelValue"]);

const error = ref("");

function stripPrefix(dataUrlOrBase64) {
  const commaIndex = dataUrlOrBase64.indexOf(",");
  return dataUrlOrBase64.startsWith("data:") && commaIndex >= 0
    ? dataUrlOrBase64.slice(commaIndex + 1)
    : dataUrlOrBase64;
}

function readAsBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(stripPrefix(reader.result));
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

async function handleFileChange(event) {
  error.value = "";
  const files = Array.from(event.target.files || []);
  event.target.value = ""; // let the same file be re-picked later if removed

  const room = MAX_PHOTOS - props.modelValue.length;
  if (files.length > room) {
    error.value = `You can add ${room} more photo${room === 1 ? "" : "s"} (${MAX_PHOTOS} max per listing).`;
    return;
  }

  const accepted = [];
  for (const file of files) {
    if (!ALLOWED_TYPES.includes(file.type)) {
      error.value = "Only JPEG or PNG images are allowed.";
      continue;
    }
    if (file.size > MAX_BYTES) {
      error.value = `"${file.name}" is ${(file.size / 1024).toFixed(0)}KB — the limit is 4MB.`;
      continue;
    }
    accepted.push(await readAsBase64(file));
  }

  if (accepted.length) {
    emit("update:modelValue", [...props.modelValue, ...accepted]);
  }
}

function removePhoto(index) {
  const next = [...props.modelValue];
  next.splice(index, 1);
  emit("update:modelValue", next);
}
</script>

<template>
  <div class="field">
    <label>Photos (JPEG/PNG, max 4MB each, up to {{ MAX_PHOTOS }})</label>
    <input
      type="file"
      accept="image/jpeg,image/png"
      multiple
      :disabled="modelValue.length >= MAX_PHOTOS"
      @change="handleFileChange"
    />
    <p v-if="error" class="field-error">{{ error }}</p>
    <p v-if="modelValue.length >= MAX_PHOTOS" class="field-hint">You've reached the {{ MAX_PHOTOS }}-photo limit.</p>

    <div v-if="modelValue.length" class="photo-preview-row">
      <div v-for="(photo, index) in modelValue" :key="index" class="photo-preview">
        <img :src="`data:image/jpeg;base64,${photo}`" :alt="`Photo ${index + 1}`" />
        <button type="button" class="photo-preview-remove" aria-label="Remove photo" @click="removePhoto(index)">
          ✕
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.photo-preview-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-top: var(--space-2);
}
.photo-preview {
  position: relative;
  width: 90px;
  height: 90px;
}
.photo-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: var(--radius-card);
  border: 1px solid var(--color-border);
}
.photo-preview-remove {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: none;
  background: var(--color-danger);
  color: #fff;
  font-size: 11px;
  line-height: 1;
  cursor: pointer;
}
</style>
