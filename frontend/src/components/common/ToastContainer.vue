<script setup>
import { useToastStore } from "../../stores/toast";

const toastStore = useToastStore();
const ICONS = { success: "✅", error: "⚠️", info: "💬" };
</script>

<template>
  <Teleport to="body">
    <div class="toast-stack">
      <TransitionGroup name="toast">
        <div v-for="toast in toastStore.toasts" :key="toast.id" class="toast" :class="`toast-${toast.type}`" role="status">
          <span class="toast-icon" aria-hidden="true">{{ ICONS[toast.type] }}</span>
          <span class="toast-message">{{ toast.message }}</span>
          <button type="button" class="toast-close" aria-label="Dismiss" @click="toastStore.dismiss(toast.id)">✕</button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>
