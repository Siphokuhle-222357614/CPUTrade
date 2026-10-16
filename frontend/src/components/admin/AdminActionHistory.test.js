import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { flushPromises, mount } from "@vue/test-utils";
import AdminActionHistory from "./AdminActionHistory.vue";

const { getAdminActionHistory } = vi.hoisted(() => ({
  getAdminActionHistory: vi.fn(),
}));

vi.mock("../../api/admin", () => ({ getAdminActionHistory }));

describe("admin action history", () => {
  beforeEach(() => {
    getAdminActionHistory.mockReset();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("renders actor and target snapshots from the protected history API", async () => {
    getAdminActionHistory.mockResolvedValue({
      data: [{
        id: 10,
        action: "USER_SUSPENDED",
        actorUsername: "admin",
        targetType: "USER",
        targetId: 22,
        targetLabel: "student",
        details: "Account suspended.",
        createdAt: "2026-06-18T11:30:00",
      }],
    });
    const wrapper = mount(AdminActionHistory);
    expect(wrapper.get('[role="status"]').text()).toContain("Loading action history");
    await flushPromises();

    expect(wrapper.get("h2").text()).toBe("Recent Admin Actions");
    expect(wrapper.text()).toContain("Account suspended");
    expect(wrapper.text()).toContain("admin");
    expect(wrapper.text()).toContain("student");
    expect(wrapper.text()).toContain("18 Jun 2026, 11:30");
    expect(wrapper.find("time").attributes("datetime")).toBe("2026-06-18T11:30:00");
  });

  it("announces history load errors to assistive technology", async () => {
    getAdminActionHistory.mockRejectedValue({
      response: { data: { message: "History unavailable." } },
    });
    const wrapper = mount(AdminActionHistory);
    await flushPromises();

    expect(wrapper.get('[role="alert"]').text()).toBe("History unavailable.");
  });
});
