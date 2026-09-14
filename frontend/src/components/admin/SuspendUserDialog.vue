<script setup>
import { reactive, ref, watch } from "vue";
import Modal from "../common/Modal.vue";
import { suspendUser } from "../../api/admin";
import { localInputToBackend } from "../../utils/datetime";
import { useToastStore } from "../../stores/toast";

const toast = useToastStore();

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  userId: {
    type: [String, Number],
    default: null,
  },
  username: {
    type: String,
    default: "",
  },
});
const emit = defineEmits(["close", "suspended"]);

const form = reactive({ reason: "", suspendedUntil: "" });
const loading = ref(false);
const errorMessage = ref("");

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      form.reason = "";
      form.suspendedUntil = "";
      errorMessage.value = "";
    }
  }
);

async function handleSubmit() {
  loading.value = true;
  errorMessage.value = "";
  try {
    await suspendUser(props.userId, {
      reason: form.reason || null,
      // Sent as the literal wall-clock value the admin typed — never
      // round-tripped through new Date(...).toISOString(), which converts
      // through UTC and silently shifts it by two hours (this app's clock
      // is always Africa/Johannesburg; see utils/datetime.js).
      suspendedUntil: localInputToBackend(form.suspendedUntil),
    });
    toast.info(`${props.username} suspended.`);
    emit("suspended");
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not suspend this account.";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <Modal :open="open" :title="`Suspend ${username}`" @close="emit('close')">
    <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
    <form @submit.prevent="handleSubmit">
      <div class="field">
        <label for="suspend-reason">Reason</label>
        <textarea id="suspend-reason" v-model="form.reason" rows="3" maxlength="500" placeholder="Why is this account being suspended?"></textarea>
      </div>
      <div class="field">
        <label for="suspend-until">Auto-lift at (optional)</label>
        <input id="suspend-until" v-model="form.suspendedUntil" type="datetime-local" />
        <p class="field-hint">Leave blank for an indefinite suspension — the user can still appeal at any time.</p>
      </div>
      <div class="row" style="justify-content: flex-end">
        <button type="button" class="btn btn-outline" @click="emit('close')">Cancel</button>
        <button type="submit" class="btn btn-danger" :disabled="loading">
          {{ loading ? "Suspending…" : "Suspend Account" }}
        </button>
      </div>
    </form>
  </Modal>
</template>
