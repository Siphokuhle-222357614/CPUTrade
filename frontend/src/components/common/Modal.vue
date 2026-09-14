<script setup>
defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    default: "",
  },
});
const emit = defineEmits(["close"]);
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="modal-overlay" @click.self="emit('close')">
      <div class="modal-dialog" role="dialog" aria-modal="true" :aria-label="title">
        <div class="modal-header">
          <h3 style="margin: 0">{{ title }}</h3>
          <button type="button" class="modal-close" aria-label="Close" @click="emit('close')">✕</button>
        </div>
        <div class="modal-body">
          <slot />
        </div>
      </div>
    </div>
  </Teleport>
</template>
