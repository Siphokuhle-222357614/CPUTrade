<script setup>
// A small celebratory burst for a genuine milestone (e.g. marking a listing
// sold) — not a generic effect library, just a handful of emoji particles
// flying outward on randomized trajectories via CSS custom properties, so
// there's no animation dependency to add for one moment in the app.
import { ref, watch } from "vue";

const props = defineProps({
  // Increment this from the parent each time a burst should fire — a
  // counter (not a boolean) so firing twice in a row still re-triggers.
  trigger: {
    type: Number,
    default: 0,
  },
});

const EMOJI = ["🎉", "✨", "💫", "⭐"];
const PARTICLE_COUNT = 10;
const pieces = ref([]);
let clearTimer = null;

watch(
  () => props.trigger,
  (value) => {
    if (!value) return;
    pieces.value = Array.from({ length: PARTICLE_COUNT }, (_, i) => {
      const angle = (Math.PI * 2 * i) / PARTICLE_COUNT + Math.random() * 0.4;
      const distance = 36 + Math.random() * 36;
      return {
        id: `${value}-${i}`,
        emoji: EMOJI[Math.floor(Math.random() * EMOJI.length)],
        style: {
          "--tx": `${Math.cos(angle) * distance}px`,
          "--ty": `${Math.sin(angle) * distance}px`,
          "--rot": `${(Math.random() - 0.5) * 360}deg`,
          animationDelay: `${Math.random() * 70}ms`,
        },
      };
    });
    clearTimeout(clearTimer);
    clearTimer = setTimeout(() => (pieces.value = []), 900);
  }
);
</script>

<template>
  <span class="confetti-burst" aria-hidden="true">
    <span v-for="piece in pieces" :key="piece.id" class="confetti-piece" :style="piece.style">
      {{ piece.emoji }}
    </span>
  </span>
</template>
