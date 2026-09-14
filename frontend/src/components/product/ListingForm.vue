<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import ImageUploadInput from "./ImageUploadInput.vue";
import { createProduct, updateProduct } from "../../api/products";

const props = defineProps({
  // Pass an existing product to edit; omit to create a new one.
  initial: {
    type: Object,
    default: null,
  },
});

const router = useRouter();
const loading = ref(false);
const errorMessage = ref("");

const form = reactive({
  title: props.initial?.title || "",
  description: props.initial?.description || "",
  price: props.initial?.price ?? "",
  category: props.initial?.category || "TEXTBOOKS",
  condition: props.initial?.condition || "GOOD",
  imageBase64: props.initial?.imageBase64 || "",
});

async function handleSubmit() {
  errorMessage.value = "";
  loading.value = true;
  try {
    const payload = {
      title: form.title,
      description: form.description,
      price: Number(form.price),
      category: form.category,
      condition: form.condition,
      imageBase64: form.imageBase64 || null,
    };

    const response = props.initial
      ? await updateProduct(props.initial.id, payload)
      : await createProduct(payload);

    router.push({ name: "product-detail", params: { id: response.data.id } });
  } catch (err) {
    errorMessage.value =
      err.response?.data?.message || "Could not save the listing — please try again.";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="card" style="max-width: 520px; margin: 0 auto">
    <h2>{{ initial ? "Edit Listing" : "Create a Listing" }}</h2>

    <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>

    <form @submit.prevent="handleSubmit">
      <div class="field">
        <label for="title">Title</label>
        <input id="title" v-model="form.title" type="text" maxlength="150" required />
      </div>

      <div class="field">
        <label for="description">Description</label>
        <textarea id="description" v-model="form.description" rows="4" maxlength="5000"></textarea>
      </div>

      <div class="field">
        <label for="price">Price (R)</label>
        <input id="price" v-model="form.price" type="number" min="0" step="0.01" required />
        <p class="field-hint">Set to 0 to give the item away for free.</p>
      </div>

      <div class="field">
        <label for="category">Category</label>
        <select id="category" v-model="form.category">
          <option value="TEXTBOOKS">Textbooks</option>
          <option value="ELECTRONICS">Electronics</option>
          <option value="CLOTHING">Clothing</option>
          <option value="SERVICES">Services</option>
          <option value="OTHER">Other</option>
        </select>
      </div>

      <div class="field">
        <label for="condition">Condition</label>
        <select id="condition" v-model="form.condition">
          <option value="NEW">New</option>
          <option value="LIKE_NEW">Like New</option>
          <option value="GOOD">Good</option>
          <option value="FAIR">Fair</option>
          <option value="POOR">Poor</option>
        </select>
      </div>

      <ImageUploadInput v-model="form.imageBase64" />

      <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
        {{ loading ? "Saving…" : initial ? "Save Changes" : "Create Listing" }}
      </button>
    </form>
  </div>
</template>
