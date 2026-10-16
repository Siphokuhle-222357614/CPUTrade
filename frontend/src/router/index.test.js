import { beforeEach, describe, expect, it } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import router from "./index";
import { useAuthStore } from "../stores/auth";

describe("router authentication guards", () => {
  beforeEach(async () => {
    localStorage.clear();
    setActivePinia(createPinia());
    await router.push("/");
    await router.isReady();
  });

  it("redirects unauthenticated users from protected routes to login", async () => {
    await router.push("/dashboard");

    expect(router.currentRoute.value.name).toBe("login");
    expect(router.currentRoute.value.query.redirect).toBe("/dashboard");
  });

  it("protects checkout and order history from signed-out users", async () => {
    await router.push("/checkout/17");

    expect(router.currentRoute.value.name).toBe("login");
    expect(router.currentRoute.value.query.redirect).toBe("/checkout/17");

    await router.push("/orders");

    expect(router.currentRoute.value.name).toBe("login");
    expect(router.currentRoute.value.query.redirect).toBe("/orders");
  });

  it("allows signed-in users to open account settings", async () => {
    const auth = useAuthStore();
    auth.token = "student-token";
    auth.user = { username: "student", role: "STUDENT" };

    await router.push("/account");

    expect(router.currentRoute.value.name).toBe("account-settings");
  });

  it("keeps non-admin users out of admin routes", async () => {
    const auth = useAuthStore();
    auth.token = "student-token";
    auth.user = { username: "student", role: "STUDENT" };

    await router.push("/admin");

    expect(router.currentRoute.value.name).toBe("marketplace");
  });

  it("redirects admins away from the seller dashboard", async () => {
    const auth = useAuthStore();
    auth.token = "admin-token";
    auth.user = { username: "admin", role: "ADMIN" };

    await router.push("/dashboard");

    expect(router.currentRoute.value.name).toBe("admin");
  });
});
