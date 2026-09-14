<script setup>
import { watch } from "vue";
import { useRoute } from "vue-router";
import NavBar from "./components/layout/NavBar.vue";
import InstallPrompt from "./components/layout/InstallPrompt.vue";
import ToastContainer from "./components/common/ToastContainer.vue";
import { useAuthStore } from "./stores/auth";
import { useWishlistStore } from "./stores/wishlist";

const route = useRoute();
const auth = useAuthStore();
const wishlist = useWishlistStore();

// Wishlist state is per-user -- load it in on login, and drop it on logout so
// the next person to use this tab/browser doesn't inherit someone else's hearts.
watch(
  () => auth.isAuthenticated,
  (isAuthenticated) => {
    if (isAuthenticated) {
      wishlist.load();
    } else {
      wishlist.clear();
    }
  },
  { immediate: true }
);
</script>

<template>
  <ToastContainer />
  <InstallPrompt />
  <NavBar />
  <main class="page">
    <router-view v-slot="{ Component }">
      <transition name="page" mode="out-in">
        <!--
          <Transition> requires its slot to resolve to exactly one root
          element, but several views (e.g. AdminView) render an <h1> plus a
          sibling block as separate top-level nodes ("fragment" components) —
          without this wrapper div, Vue warns and silently skips animating
          them. The :key (on the route path, not the component) is what
          actually makes the transition fire on navigation at all: without
          a key change, Vue just patches this same div in place.
        -->
        <div :key="route.path">
          <component :is="Component" />
        </div>
      </transition>
    </router-view>
  </main>
</template>
