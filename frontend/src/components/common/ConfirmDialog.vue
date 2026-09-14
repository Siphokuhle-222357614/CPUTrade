<script setup>
import Modal from "./Modal.vue";

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    default: "Are you sure?",
  },
  message: {
    type: String,
    default: "",
  },
  confirmLabel: {
    type: String,
    default: "Confirm",
  },
  danger: {
    type: Boolean,
    default: false,
  },
});
const emit = defineEmits(["confirm", "cancel"]);
</script>

<template>
  <Modal :open="open" :title="title" @close="emit('cancel')">
    <p style="margin-top: 0">{{ message }}</p>
    <div class="row" style="justify-content: flex-end">
      <button type="button" class="btn btn-outline" @click="emit('cancel')">Cancel</button>
      <button type="button" :class="danger ? 'btn btn-danger' : 'btn btn-primary'" @click="emit('confirm')">
        {{ confirmLabel }}
      </button>
    </div>
  </Modal>
</template>
