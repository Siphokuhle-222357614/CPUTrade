import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import * as authApi from "../api/auth";
import { useAuthStore } from "./auth";

vi.mock("../api/auth", () => ({
  login: vi.fn(),
  register: vi.fn(),
}));

describe("auth store", () => {
  beforeEach(() => {
    localStorage.clear();
    setActivePinia(createPinia());
  });

  it("restores the saved token and user from local storage", () => {
    localStorage.setItem("cputrade_token", "saved-token");
    localStorage.setItem(
      "cputrade_user",
      JSON.stringify({ username: "student", role: "STUDENT" }),
    );

    const auth = useAuthStore();

    expect(auth.isAuthenticated).toBe(true);
    expect(auth.user.username).toBe("student");
    expect(auth.isAdmin).toBe(false);
  });

  it("persists the user and token after a successful login", async () => {
    const user = { username: "admin", role: "ADMIN" };
    authApi.login.mockResolvedValue({
      data: { token: "fresh-token", user },
    });

    const auth = useAuthStore();
    await auth.login({ username: "admin", password: "secret" });

    expect(auth.user).toEqual(user);
    expect(auth.isAuthenticated).toBe(true);
    expect(auth.isAdmin).toBe(true);
    expect(localStorage.getItem("cputrade_token")).toBe("fresh-token");
    expect(JSON.parse(localStorage.getItem("cputrade_user"))).toEqual(user);
  });

  it("does not persist authentication when login fails", async () => {
    authApi.login.mockRejectedValue(new Error("Invalid credentials"));

    const auth = useAuthStore();

    await expect(auth.login({ username: "student", password: "wrong" }))
      .rejects.toThrow("Invalid credentials");
    expect(auth.isAuthenticated).toBe(false);
    expect(localStorage.getItem("cputrade_token")).toBeNull();
  });

  it("registers without automatically authenticating the user", async () => {
    const response = { message: "Registration successful" };
    authApi.register.mockResolvedValue({ data: response });

    const auth = useAuthStore();
    await expect(auth.register({ username: "student" })).resolves.toEqual(response);

    expect(auth.isAuthenticated).toBe(false);
    expect(localStorage.getItem("cputrade_token")).toBeNull();
  });

  it("clears persisted authentication on logout", async () => {
    authApi.login.mockResolvedValue({
      data: { token: "fresh-token", user: { username: "student", role: "STUDENT" } },
    });
    const auth = useAuthStore();
    await auth.login({ username: "student", password: "secret" });

    auth.logout();

    expect(auth.isAuthenticated).toBe(false);
    expect(auth.user).toBeNull();
    expect(localStorage.getItem("cputrade_token")).toBeNull();
    expect(localStorage.getItem("cputrade_user")).toBeNull();
  });
});
