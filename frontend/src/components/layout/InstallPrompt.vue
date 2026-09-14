<script setup>
// Cross-platform "install this app" banner. There is no single browser API
// that covers every device, so this component branches into three cases:
//
// 1. Android / desktop Chrome, Edge, Samsung Internet, etc. all fire a real
//    `beforeinstallprompt` event we can hook a button up to directly.
// 2. iOS/iPadOS Safari never fires that event (Apple's PWA support is
//    manifest-driven only) -- the only way "in" is the Share sheet, so we
//    show instructions instead of a button.
// 3. macOS Safari (Sonoma and later) also never fires it, but exposes
//    "Add to Dock" from the File menu -- again, instructions only.
//
// If the app is already installed/running standalone, or the browser is one
// that supports none of the above (e.g. desktop Firefox), nothing renders.
import { computed, onBeforeUnmount, onMounted, ref } from "vue";

const DISMISSED_KEY = "cputrade_install_prompt_dismissed";

const deferredPrompt = ref(null);
const platform = ref(null); // "installable" | "ios" | "mac-safari" | null
const dismissed = ref(false);

function readDismissed() {
  try {
    return localStorage.getItem(DISMISSED_KEY) === "1";
  } catch {
    // Private-mode/storage-blocked browsers: fall back to showing the
    // banner every visit rather than crashing.
    return false;
  }
}

function isStandalone() {
  return (
    window.matchMedia?.("(display-mode: standalone)").matches ||
    // iOS's own non-standard flag -- not covered by the media query above.
    window.navigator.standalone === true
  );
}

function detectPlatform() {
  if (isStandalone()) return null;

  const ua = window.navigator.userAgent;
  const isIos = /iPad|iPhone|iPod/.test(ua) || (ua.includes("Macintosh") && navigator.maxTouchPoints > 1);
  if (isIos) return "ios";

  const isSafari = /^((?!chrome|android).)*safari/i.test(ua);
  if (isSafari) return "mac-safari";

  return null; // Chromium browsers report themselves via beforeinstallprompt instead.
}

function handleBeforeInstallPrompt(event) {
  event.preventDefault();
  deferredPrompt.value = event;
  platform.value = "installable";
}

function handleAppInstalled() {
  deferredPrompt.value = null;
  platform.value = null;
}

async function install() {
  if (!deferredPrompt.value) return;
  deferredPrompt.value.prompt();
  await deferredPrompt.value.userChoice;
  deferredPrompt.value = null;
  platform.value = null;
}

function dismiss() {
  dismissed.value = true;
  try {
    localStorage.setItem(DISMISSED_KEY, "1");
  } catch {
    // Nothing to do -- worst case the banner reappears next visit.
  }
}

onMounted(() => {
  dismissed.value = readDismissed();
  window.addEventListener("beforeinstallprompt", handleBeforeInstallPrompt);
  window.addEventListener("appinstalled", handleAppInstalled);
  // Chromium hasn't fired its event yet at mount time -- fall back to the
  // manual-instructions platforms so Safari users see something immediately.
  if (!platform.value) platform.value = detectPlatform();
});

onBeforeUnmount(() => {
  window.removeEventListener("beforeinstallprompt", handleBeforeInstallPrompt);
  window.removeEventListener("appinstalled", handleAppInstalled);
});

const visible = computed(() => !dismissed.value && !!platform.value);
</script>

<template>
  <div v-if="visible" class="install-banner">
    <span class="install-banner-icon" aria-hidden="true">📲</span>

    <div class="install-banner-body">
      <template v-if="platform === 'installable'">
        <strong>Install CPUTrade</strong>
        <p>Add it to your home screen for one-tap access and chat alerts.</p>
      </template>
      <template v-else-if="platform === 'ios'">
        <strong>Install CPUTrade</strong>
        <p>Tap the Share icon <span aria-hidden="true">⬆️</span>, then "Add to Home Screen".</p>
      </template>
      <template v-else-if="platform === 'mac-safari'">
        <strong>Install CPUTrade</strong>
        <p>Open the File menu and choose "Add to Dock".</p>
      </template>
    </div>

    <button v-if="platform === 'installable'" type="button" class="btn btn-primary btn-sm" @click="install">
      Install
    </button>
    <button type="button" class="install-banner-dismiss" aria-label="Dismiss" @click="dismiss">✕</button>
  </div>
</template>

<style scoped>
.install-banner {
  position: sticky;
  top: 0;
  z-index: 40;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) var(--space-4);
  background: var(--color-accent-bg);
  border-bottom: 1px solid var(--color-accent);
}
.install-banner-icon {
  font-size: 1.4rem;
  flex-shrink: 0;
}
.install-banner-body {
  flex: 1;
  min-width: 0;
}
.install-banner-body strong {
  display: block;
  color: var(--color-primary);
  font-size: 0.9rem;
}
.install-banner-body p {
  margin: 0;
  font-size: 0.8rem;
  color: var(--color-text);
}
.btn-sm {
  padding: var(--space-1) var(--space-3);
  font-size: 0.85rem;
  flex-shrink: 0;
}
.install-banner-dismiss {
  background: none;
  border: none;
  color: var(--color-text);
  cursor: pointer;
  font-size: 1rem;
  padding: var(--space-1);
  flex-shrink: 0;
}

@media (max-width: 480px) {
  .install-banner-body p {
    display: none; /* keep the bar to one line on narrow phones */
  }
}
</style>
