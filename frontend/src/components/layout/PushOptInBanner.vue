<script setup>
// A one-line, dismissible nudge to turn on real push notifications -- shown
// only to a logged-in student, on a supported browser, who hasn't already
// decided (granted, denied, or dismissed this banner before).
import { computed, onMounted, ref } from "vue";
import { useAuthStore } from "../../stores/auth";
import { usePushStore } from "../../stores/push";
import { useToastStore } from "../../stores/toast";

const DISMISSED_KEY = "cputrade_push_prompt_dismissed";

const auth = useAuthStore();
const push = usePushStore();
const toast = useToastStore();
const dismissed = ref(false);
const enabling = ref(false);

function readDismissed() {
  try {
    return localStorage.getItem(DISMISSED_KEY) === "1";
  } catch {
    return false;
  }
}

function dismiss() {
  dismissed.value = true;
  try {
    localStorage.setItem(DISMISSED_KEY, "1");
  } catch {
    // Worst case the banner reappears next visit -- not worth failing over.
  }
}

async function handleEnable() {
  enabling.value = true;
  const ok = await push.enable();
  enabling.value = false;
  if (ok) {
    toast.success("Push notifications are on — you'll hear about messages and deals even with the app closed.");
  } else if (push.permission === "denied") {
    toast.error("Notifications are blocked for this site in your browser settings.");
    dismiss();
  }
}

onMounted(() => {
  dismissed.value = readDismissed();
  push.checkSubscription();
});

const visible = computed(
  () =>
    auth.isAuthenticated &&
    push.supported &&
    push.checked &&
    !push.subscribed &&
    push.permission === "default" &&
    !dismissed.value
);
</script>

<template>
  <div v-if="visible" class="push-banner">
    <span class="push-banner-icon" aria-hidden="true">🔔</span>
    <div class="push-banner-body">
      <strong>Never miss a message or a deal</strong>
      <p>Turn on notifications to hear about chats and wishlist price drops instantly.</p>
    </div>
    <button type="button" class="btn btn-primary btn-sm" :disabled="enabling" @click="handleEnable">
      {{ enabling ? "Enabling…" : "Enable" }}
    </button>
    <button type="button" class="push-banner-dismiss" aria-label="Dismiss" @click="dismiss">✕</button>
  </div>
</template>

<style scoped>
.push-banner {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) var(--space-4);
  background: var(--color-primary-bg, #eef2f9);
  border-bottom: 1px solid var(--color-primary-light, var(--color-primary));
}
.push-banner-icon {
  font-size: 1.4rem;
  flex-shrink: 0;
}
.push-banner-body {
  flex: 1;
  min-width: 0;
}
.push-banner-body strong {
  display: block;
  color: var(--color-primary);
  font-size: 0.9rem;
}
.push-banner-body p {
  margin: 0;
  font-size: 0.8rem;
  color: var(--color-text);
}
.push-banner-dismiss {
  background: none;
  border: none;
  color: var(--color-text);
  cursor: pointer;
  font-size: 1rem;
  padding: var(--space-1);
  flex-shrink: 0;
}

@media (max-width: 480px) {
  .push-banner-body p {
    display: none;
  }
}
</style>
