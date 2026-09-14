<script setup>
import { ref } from "vue";
import { addProductImages, removeProductImage } from "../../api/products";

// Live photo management for an existing listing — each add/remove is its
// own API call (unlike the rest of the edit form, which batches changes
// behind one "Save"), since photos are stored server-side as files rather
// than form state, and re-encoding the ones you're keeping just to satisfy
// a "full replace" save would be wasteful and easy to get wrong.
const MAX_BYTES = 500 * 1024;
const MAX_PHOTOS = 6;
const ALLOWED_TYPES = ["image/jpeg", "image/png"];

const props = defineProps({
  productId: {
    type: [String, Number],
    required: true,
  },
  images: {
    type: Array,
    required: true,
  },
});
const emit = defineEmits(["updated"]);

const error = ref("");
const busy = ref(false);

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
  event.target.value = "";
  if (!files.length) return;

  const room = MAX_PHOTOS - props.images.length;
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
      error.value = `"${file.name}" is ${(file.size / 1024).toFixed(0)}KB — the limit is 500KB.`;
      continue;
    }
    accepted.push(await readAsBase64(file));
  }
  if (!accepted.length) return;

  busy.value = true;
  try {
    const { data } = await addProductImages(props.productId, accepted);
    emit("updated", data);
  } catch (err) {
    error.value = err.response?.data?.message || "Could not add those photos.";
  } finally {
    busy.value = false;
  }
}

async function removePhoto(url) {
  error.value = "";
  busy.value = true;
  try {
    const { data } = await removeProductImage(props.productId, url);
    emit("updated", data);
  } catch (err) {
    error.value = err.response?.data?.message || "Could not remove that photo.";
  } finally {
    busy.value = false;
  }
}
</script>

<template>
  <div class="field">
    <label>Photos ({{ images.length }}/{{ MAX_PHOTOS }})</label>
    <p v-if="error" class="field-error">{{ error }}</p>

    <div v-if="images.length" class="photo-preview-row">
      <div v-for="url in images" :key="url" class="photo-preview">
        <img :src="url" alt="Listing photo" />
        <button type="button" class="photo-preview-remove" :disabled="busy" aria-label="Remove photo" @click="removePhoto(url)">
          ✕
        </button>
      </div>
    </div>
    <p v-else class="field-hint">No photos yet.</p>

    <input
      type="file"
      accept="image/jpeg,image/png"
      multiple
      :disabled="busy || images.length >= MAX_PHOTOS"
      style="margin-top: var(--space-2)"
      @change="handleFileChange"
    />
    <p v-if="images.length >= MAX_PHOTOS" class="field-hint">You've reached the {{ MAX_PHOTOS }}-photo limit.</p>
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
