// A short, synthesized "message received" blip — no audio asset file to
// manage. Wrapped in try/catch throughout: browsers can refuse to start an
// AudioContext before the user has interacted with the page at all, and
// that must never break the chat UI, just silently skip the sound.
let sharedContext = null;

function getContext() {
  if (!sharedContext) {
    const Ctor = window.AudioContext || window.webkitAudioContext;
    if (!Ctor) return null;
    sharedContext = new Ctor();
  }
  return sharedContext;
}

export function playMessageSound() {
  try {
    const ctx = getContext();
    if (!ctx) return;

    const oscillator = ctx.createOscillator();
    const gain = ctx.createGain();
    oscillator.type = "sine";
    oscillator.frequency.setValueAtTime(880, ctx.currentTime);
    oscillator.frequency.exponentialRampToValueAtTime(660, ctx.currentTime + 0.12);
    gain.gain.setValueAtTime(0.15, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.18);

    oscillator.connect(gain);
    gain.connect(ctx.destination);
    oscillator.start();
    oscillator.stop(ctx.currentTime + 0.2);
  } catch (err) {
    // Autoplay policy or unsupported browser — silently skip the sound.
  }
}
