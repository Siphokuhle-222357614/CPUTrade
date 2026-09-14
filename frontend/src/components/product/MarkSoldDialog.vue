<script setup>
import { ref, watch } from "vue";
import Modal from "../common/Modal.vue";
import { getInterestedBuyers, markSold } from "../../api/products";

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  productId: {
    type: [String, Number],
    required: true,
  },
});
const emit = defineEmits(["close", "sold"]);

const buyers = ref([]);
const selectedBuyerId = ref(null);
const loading = ref(false);
const submitting = ref(false);
const errorMessage = ref("");

watch(
  () => props.open,
  async (isOpen) => {
    if (!isOpen) return;
    errorMessage.value = "";
    selectedBuyerId.value = null;
    loading.value = true;
    try {
      const { data } = await getInterestedBuyers(props.productId);
      buyers.value = data;
    } catch (err) {
      errorMessage.value = err.response?.data?.message || "Could not load who messaged you about this listing.";
    } finally {
      loading.value = false;
    }
  }
);

async function confirm() {
  submitting.value = true;
  errorMessage.value = "";
  try {
    const { data } = await markSold(props.productId, selectedBuyerId.value);
    emit("sold", data);
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not mark this listing as sold.";
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <Modal :open="open" title="Mark as Sold" @close="emit('close')">
    <p class="field-hint">
      This hides the listing from marketplace search and lets the buyer leave you a rating. You can undo this later.
    </p>

    <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
    <p v-if="loading">Loading…</p>

    <template v-else>
      <div class="field">
        <label>Who did you sell it to? (optional)</label>
        <div class="table-list">
          <label class="row" style="cursor: pointer">
            <input v-model="selectedBuyerId" type="radio" :value="null" />
            <span>Prefer not to say</span>
          </label>
          <label v-for="buyer in buyers" :key="buyer.id" class="row" style="cursor: pointer">
            <input v-model="selectedBuyerId" type="radio" :value="buyer.id" />
            <span>{{ buyer.username }}</span>
          </label>
        </div>
        <p v-if="!buyers.length" class="field-hint">No one has messaged you about this listing yet.</p>
      </div>

      <div class="row" style="margin-top: var(--space-4)">
        <button type="button" class="btn btn-primary" :disabled="submitting" @click="confirm">
          {{ submitting ? "Saving…" : "Confirm Sold" }}
        </button>
        <button type="button" class="btn btn-outline" @click="emit('close')">Cancel</button>
      </div>
    </template>
  </Modal>
</template>
