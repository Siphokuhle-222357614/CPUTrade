import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/auth";

const routes = [
  {
    path: "/login",
    name: "login",
    component: () => import("../views/LoginRegisterView.vue"),
  },
  {
    path: "/",
    name: "marketplace",
    component: () => import("../views/MarketplaceView.vue"),
  },
  {
    path: "/products/new",
    name: "product-new",
    component: () => import("../views/ListingFormView.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/products/:id",
    name: "product-detail",
    component: () => import("../views/ProductDetailView.vue"),
    props: true,
  },
  {
    path: "/products/:id/edit",
    name: "product-edit",
    component: () => import("../views/ListingFormView.vue"),
    props: true,
    meta: { requiresAuth: true },
  },
  {
    path: "/admin",
    name: "admin",
    component: () => import("../views/AdminView.vue"),
    meta: { requiresAdmin: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// Route guards are a UX convenience only — every real authorization decision
// (ownership, admin-only, vendor-approval) is enforced server-side, since a
// client-side guard can trivially be bypassed by calling the API directly.
router.beforeEach((to) => {
  const auth = useAuthStore();

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: "login", query: { redirect: to.fullPath } };
  }

  if (to.meta.requiresAdmin && !auth.isAdmin) {
    return { name: "marketplace" };
  }

  return true;
});

export default router;
