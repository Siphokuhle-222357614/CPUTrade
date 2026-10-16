import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { flushPromises, mount } from "@vue/test-utils";
import AccountSettingsView from "./AccountSettingsView.vue";

const { anonymizeAccount, authState } = vi.hoisted(() => ({
  anonymizeAccount: vi.fn(),
  authState: { logout: vi.fn() },
}));

vi.mock("../api/account", () => ({ anonymizeAccount }));
vi.mock("../stores/auth", () => ({ useAuthStore: () => authState }));

describe("account settings", () => {
  beforeEach(() => {
    anonymizeAccount.mockReset();
    authState.logout.mockReset();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("requires the current password and explicit DELETE confirmation", async () => {
    const wrapper = mount(AccountSettingsView, {
      global: { stubs: ["RouterLink"] },
    });

    expect(wrapper.get('button[type="submit"]').element.disabled).toBe(true);
    await wrapper.get("#account-current-password").setValue("correct-password");
    expect(wrapper.get('button[type="submit"]').element.disabled).toBe(true);
    await wrapper.get("#account-delete-confirmation").setValue("DELETE");
    expect(wrapper.get('button[type="submit"]').element.disabled).toBe(false);

    await wrapper.get("form").trigger("submit");
    await flushPromises();

    expect(anonymizeAccount).toHaveBeenCalledWith("correct-password");
    expect(authState.logout).toHaveBeenCalledOnce();
    expect(wrapper.text()).toContain("Your account has been anonymized");
  });

  it("keeps the user signed in and displays a password error", async () => {
    anonymizeAccount.mockRejectedValue({
      response: { data: { message: "Current password is incorrect" } },
    });
    const wrapper = mount(AccountSettingsView, {
      global: { stubs: ["RouterLink"] },
    });
    await wrapper.get("#account-current-password").setValue("wrong-password");
    await wrapper.get("#account-delete-confirmation").setValue("DELETE");

    await wrapper.get("form").trigger("submit");
    await flushPromises();

    expect(wrapper.text()).toContain("Current password is incorrect");
    expect(authState.logout).not.toHaveBeenCalled();
  });
});