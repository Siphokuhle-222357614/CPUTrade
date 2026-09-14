<script setup>
import { onMounted, ref } from "vue";
import ListingForm from "../components/product/ListingForm.vue";
import { getProduct } from "../api/products";

const props = defineProps({
  id: {
    type: [String, Number],
    default: null,
  },
});

const initial = ref(null);
const loading = ref(!!props.id);
const errorMessage = ref("");

async function load() {
  if (!props.id) return;
  loading.value = true;
  try {
    const { data } = await getProduct(props.id);
    initial.value = data;
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not load this listing.";
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <p v-if="loading">Loading…</p>
  <p v-else-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
  <ListingForm v-else :initial="initial" />
</template>
