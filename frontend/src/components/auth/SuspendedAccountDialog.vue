<script setup>
import { useRouter } from "vue-router";
import Modal from "../common/Modal.vue";

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  message: {
    type: String,
    default: "",
  },
  username: {
    type: String,
    default: "",
  },
});
const emit = defineEmits(["close"]);

const router = useRouter();

function goToAppeal() {
  emit("close");
  router.push({ name: "appeal", query: { username: props.username } });
}
</script>

<template>
  <Modal :open="open" title="🚫 Account Suspended" @close="emit('close')">
    <p style="margin-top: 0">{{ message }}</p>
    <p class="field-hint">
      If you believe this is a mistake, you can submit an appeal — an admin will review it, and your account is
      reactivated automatically if it's approved.
    </p>
    <div class="row" style="justify-content: flex-end">
      <button type="button" class="btn btn-outline" @click="emit('close')">Close</button>
      <button type="button" class="btn btn-primary" @click="goToAppeal">Submit an Appeal</button>
    </div>
  </Modal>
</template>
