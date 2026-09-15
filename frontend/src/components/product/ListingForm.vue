<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import ImageUploadInput from "./ImageUploadInput.vue";
import PhotoManager from "./PhotoManager.vue";
import { createProduct, updateProduct } from "../../api/products";
import { useToastStore } from "../../stores/toast";

const toast = useToastStore();

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
  campus: props.initial?.campus || "BELLVILLE",
  quantity: props.initial?.quantity ?? 1,
  images: [], // create-only: new-upload Base64 strings — see ImageUploadInput
});

// Editing a listing manages its photos live (see PhotoManager) rather than
// through this form's Save button, so the gallery shown while editing needs
// its own reactive copy that updates as photos are added/removed.
const editImages = ref(props.initial?.imageUrls || []);

async function handleSubmit() {
  errorMessage.value = "";
  loading.value = true;
  try {
    let response;
    if (props.initial) {
      response = await updateProduct(props.initial.id, {
        title: form.title,
        description: form.description,
        price: Number(form.price),
        category: form.category,
        condition: form.condition,
        campus: form.campus,
        quantity: Number(form.quantity),
      });
    } else {
      response = await createProduct({
        title: form.title,
        description: form.description,
        price: Number(form.price),
        category: form.category,
        condition: form.condition,
        campus: form.campus,
        quantity: Number(form.quantity),
        images: form.images,
      });
    }

    toast.success(props.initial ? "Listing updated." : "Listing created! 🎉");
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

    <PhotoManager v-if="initial" :product-id="initial.id" :images="editImages" @updated="(p) => (editImages = p.imageUrls)" />

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
        <label for="quantity">Quantity available</label>
        <input id="quantity" v-model="form.quantity" type="number" min="1" step="1" required />
        <p class="field-hint">How many identical units you have — e.g. 10 of the same phone case.</p>
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

      <div class="field">
        <label for="campus">Campus</label>
        <select id="campus" v-model="form.campus">
          <option value="BELLVILLE">Bellville</option>
          <option value="DISTRICT_SIX">District Six</option>
          <option value="GRANGER_BAY">Granger Bay</option>
          <option value="MOWBRAY">Mowbray</option>
          <option value="WELLINGTON">Wellington</option>
        </select>
        <p class="field-hint">Where a buyer can meet you to collect it.</p>
      </div>

      <ImageUploadInput v-if="!initial" v-model="form.images" />

      <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
        {{ loading ? "Saving…" : initial ? "Save Changes" : "Create Listing" }}
      </button>
    </form>
  </div>
</template>
