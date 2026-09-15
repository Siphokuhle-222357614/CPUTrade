<script setup>
// A small rotating "someone just bought this" strip in the hero band --
// pure social proof that the marketplace is actually alive, not decoration
// for its own sake. Renders nothing until there's at least one real sale to
// show, and nothing at all if the request fails (never worth an error state
// for something this decorative).
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { getRecentlySold } from "../../api/products";

const sales = ref([]);
const index = ref(0);
let rotateTimer = null;

function formatPrice(price) {
  const value = Number(price);
  return value === 0 ? "free" : `R${value.toFixed(2).replace(/\.00$/, "")}`;
}

const current = computed(() => sales.value[index.value] || null);

onMounted(async () => {
  try {
    const { data } = await getRecentlySold();
    sales.value = data;
  } catch {
    sales.value = [];
  }
  if (sales.value.length > 1) {
    rotateTimer = setInterval(() => {
      index.value = (index.value + 1) % sales.value.length;
    }, 4500);
  }
});
onBeforeUnmount(() => clearInterval(rotateTimer));
</script>

<template>
  <Transition name="fade" mode="out-in">
    <router-link
      v-if="current"
      :key="current.id"
      :to="{ name: 'product-detail', params: { id: current.id } }"
      class="activity-ticker"
    >
      🔥 Just sold: "{{ current.title }}" for {{ formatPrice(current.price) }}
    </router-link>
  </Transition>
</template>
