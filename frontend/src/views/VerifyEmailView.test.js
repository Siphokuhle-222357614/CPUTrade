import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { mount } from "@vue/test-utils";
import { flushPromises } from "@vue/test-utils";
import VerifyEmailView from "./VerifyEmailView.vue";

const { routeState, verifyEmail } = vi.hoisted(() => ({
  routeState: { query: {} },
  verifyEmail: vi.fn(),
}));

vi.mock("vue-router", async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useRoute: () => routeState,
  };
});

vi.mock("../api/auth", () => ({
  verifyEmail,
}));

describe("email verification view", () => {
  beforeEach(() => {
    routeState.query = { token: "valid-token" };
    verifyEmail.mockReset();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("verifies only after the user confirms the action", async () => {
    verifyEmail.mockResolvedValue({});
    const wrapper = mount(VerifyEmailView, {
      global: { stubs: ["RouterLink"] },
    });

    expect(verifyEmail).not.toHaveBeenCalled();
    await wrapper.get("button").trigger("click");
    await flushPromises();

    expect(verifyEmail).toHaveBeenCalledWith("valid-token");
    expect(wrapper.text()).toContain("Email verified. You can now sign in");
  });

  it("shows the API error when a link is expired or invalid", async () => {
    verifyEmail.mockRejectedValue({
      response: { data: { message: "This verification link is invalid or expired." } },
    });
    const wrapper = mount(VerifyEmailView, {
      global: { stubs: ["RouterLink"] },
    });

    await wrapper.get("button").trigger("click");
    await flushPromises();

    expect(wrapper.text()).toContain("This verification link is invalid or expired.");
  });

  it("does not submit when the link has no token", async () => {
    routeState.query = {};
    const wrapper = mount(VerifyEmailView, {
      global: { stubs: ["RouterLink"] },
    });

    expect(wrapper.text()).toContain("This verification link is missing its token.");
    expect(wrapper.find("button").exists()).toBe(false);
    expect(verifyEmail).not.toHaveBeenCalled();
  });
});
