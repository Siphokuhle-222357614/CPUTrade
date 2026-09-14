import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/auth";

const routes = [
  {
    path: "/login",
    name: "login",
    component: () => import("../views/LoginRegisterView.vue"),
  },
  {
    path: "/appeal",
    name: "appeal",
    component: () => import("../views/AppealView.vue"),
  },
  {
    path: "/",
    name: "marketplace",
    component: () => import("../views/MarketplaceView.vue"),
  },
  {
    path: "/dashboard",
    name: "dashboard",
    component: () => import("../views/DashboardView.vue"),
    // Admins moderate the platform, they don't sell on it — no business
    // dashboard for them (see the beforeEach guard below).
    meta: { requiresAuth: true, blockAdmin: true },
  },
  {
    path: "/products/new",
    name: "product-new",
    component: () => import("../views/ListingFormView.vue"),
    meta: { requiresAuth: true, blockAdmin: true },
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
  {
    path: "/wishlist",
    name: "wishlist",
    component: () => import("../views/WishlistView.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/chats",
    name: "conversations",
    component: () => import("../views/ConversationsView.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/chats/:id",
    name: "chat",
    component: () => import("../views/ConversationView.vue"),
    props: true,
    meta: { requiresAuth: true },
  },
  {
    path: "/board",
    name: "bulletin",
    component: () => import("../views/BulletinView.vue"),
  },
  {
    path: "/notifications",
    name: "notifications",
    component: () => import("../views/NotificationsView.vue"),
    meta: { requiresAuth: true },
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

  if (to.meta.blockAdmin && auth.isAdmin) {
    return { name: "admin" };
  }

  return true;
});

export default router;
