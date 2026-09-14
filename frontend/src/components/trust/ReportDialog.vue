<script setup>
import { reactive, ref, watch } from "vue";
import Modal from "../common/Modal.vue";
import { submitReport } from "../../api/reports";

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  // "PRODUCT" or "USER"
  targetType: {
    type: String,
    required: true,
  },
  targetId: {
    type: [String, Number],
    required: true,
  },
  targetLabel: {
    type: String,
    default: "",
  },
});
const emit = defineEmits(["close", "submitted"]);

const form = reactive({ reason: "SCAM", details: "" });
const loading = ref(false);
const errorMessage = ref("");
const submitted = ref(false);

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      form.reason = "SCAM";
      form.details = "";
      errorMessage.value = "";
      submitted.value = false;
    }
  }
);

async function handleSubmit() {
  loading.value = true;
  errorMessage.value = "";
  try {
    await submitReport({
      targetType: props.targetType,
      targetId: props.targetId,
      reason: form.reason,
      details: form.details || null,
    });
    submitted.value = true;
    emit("submitted");
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not submit this report.";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <Modal :open="open" :title="`Report ${targetType === 'PRODUCT' ? 'listing' : 'user'}`" @close="emit('close')">
    <p v-if="targetLabel" class="field-hint" style="margin-top: 0">{{ targetLabel }}</p>

    <p v-if="submitted" class="alert alert-info">
      Thanks — an admin will review this. Your report is confidential.
    </p>
    <template v-else>
      <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
      <form @submit.prevent="handleSubmit">
        <div class="field">
          <label for="report-reason">Reason</label>
          <select id="report-reason" v-model="form.reason">
            <option value="SCAM">Scam / fraud</option>
            <option value="INAPPROPRIATE">Inappropriate content</option>
            <option value="SPAM">Spam</option>
            <option value="HARASSMENT">Harassment</option>
            <option value="OTHER">Other</option>
          </select>
        </div>
        <div class="field">
          <label for="report-details">Details (optional)</label>
          <textarea id="report-details" v-model="form.details" rows="3" maxlength="1000"></textarea>
        </div>
        <div class="row" style="justify-content: flex-end">
          <button type="button" class="btn btn-outline" @click="emit('close')">Cancel</button>
          <button type="submit" class="btn btn-danger" :disabled="loading">
            {{ loading ? "Submitting…" : "Submit Report" }}
          </button>
        </div>
      </form>
    </template>
  </Modal>
</template>
