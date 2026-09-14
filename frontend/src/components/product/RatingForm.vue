<script setup>
import { reactive, ref } from "vue";
import { rateSeller } from "../../api/ratings";

const props = defineProps({
  productId: {
    type: [String, Number],
    required: true,
  },
});
const emit = defineEmits(["rated"]);

const form = reactive({ score: 5, comment: "" });
const loading = ref(false);
const errorMessage = ref("");
const submitted = ref(false);

async function handleSubmit() {
  errorMessage.value = "";
  loading.value = true;
  try {
    await rateSeller(props.productId, { score: Number(form.score), comment: form.comment });
    submitted.value = true;
    emit("rated");
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not submit your rating.";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="card" style="margin-top: var(--space-4)">
    <h4>Rate this seller</h4>
    <p v-if="submitted" class="alert alert-info">Thanks for your feedback!</p>
    <template v-else>
      <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
      <form @submit.prevent="handleSubmit">
        <div class="field">
          <label for="score">Score</label>
          <select id="score" v-model="form.score">
            <option value="5">5 — Excellent</option>
            <option value="4">4 — Good</option>
            <option value="3">3 — Okay</option>
            <option value="2">2 — Poor</option>
            <option value="1">1 — Bad</option>
          </select>
        </div>
        <div class="field">
          <label for="comment">Comment (optional)</label>
          <textarea id="comment" v-model="form.comment" rows="2" maxlength="1000"></textarea>
        </div>
        <button type="submit" class="btn btn-accent" :disabled="loading">
          {{ loading ? "Submitting…" : "Submit Rating" }}
        </button>
      </form>
    </template>
  </div>
</template>
