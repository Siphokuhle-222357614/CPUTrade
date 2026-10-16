import { beforeEach, describe, expect, it, vi } from "vitest";
import { mount, flushPromises } from "@vue/test-utils";
import { createPinia } from "pinia";
import AuthForm from "./AuthForm.vue";

const { routeState, routerPush, login, register, resendVerification } = vi.hoisted(() => ({
  routeState: { query: {} },
  routerPush: vi.fn(),
  login: vi.fn(),
  register: vi.fn(),
  resendVerification: vi.fn(),
}));

vi.mock("vue-router", () => ({
  useRoute: () => routeState,
  useRouter: () => ({ push: routerPush }),
}));

vi.mock("../../api/auth", () => ({
  login,
  register,
  resendVerification,
}));

describe("auth form email verification flow", () => {
  beforeEach(() => {
    routeState.query = {};
    routerPush.mockReset();
    login.mockReset();
    register.mockReset();
    resendVerification.mockReset();
  });

  it("explains that students must verify before signing in and supports a resend", async () => {
    login.mockRejectedValue({
      response: {
        status: 403,
        data: { message: "Verify your CPUT email address before signing in." },
      },
    });
    resendVerification.mockResolvedValue({});
    const wrapper = mount(AuthForm, {
      global: {
        plugins: [createPinia()],
        stubs: { SuspendedAccountDialog: true },
      },
    });

    await wrapper.get("#username").setValue("student");
    await wrapper.get("#password").setValue("Password123!");
    await wrapper.get("form").trigger("submit.prevent");
    await flushPromises();

    expect(wrapper.text()).toContain("Verify your CPUT email address before signing in.");
    await wrapper.get("#verification-email").setValue("student@mycput.ac.za");
    await wrapper.findAll("form")[0].trigger("submit.prevent");
    await flushPromises();

    expect(resendVerification).toHaveBeenCalledWith("student@mycput.ac.za");
    expect(wrapper.text()).toContain("If that address belongs to an unverified student account");
  });

  it("asks newly registered students to verify their email before login", async () => {
    register.mockResolvedValue({});
    const wrapper = mount(AuthForm, {
      global: {
        plugins: [createPinia()],
        stubs: { SuspendedAccountDialog: true },
      },
    });

    await wrapper.get("button.pill:last-child").trigger("click");
    await wrapper.get("#username").setValue("student");
    await wrapper.get("#email").setValue("student@mycput.ac.za");
    await wrapper.get("#password").setValue("Password123!");
    await wrapper.get("form").trigger("submit.prevent");
    await flushPromises();

    expect(wrapper.text()).toContain("A verification link has been sent to your CPUT email");
    expect(wrapper.text()).not.toContain("You can log in now.");
  });
});
