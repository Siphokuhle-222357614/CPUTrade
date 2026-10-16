import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import { flushPromises, mount } from "@vue/test-utils";
import OrdersView from "./OrdersView.vue";
import { useAuthStore } from "../stores/auth";

const { listMyOrders, confirmOrderReceived, cancelOrder, markOrderReadyForHandoff } = vi.hoisted(() => ({
  listMyOrders: vi.fn(),
  confirmOrderReceived: vi.fn(),
  cancelOrder: vi.fn(),
  markOrderReadyForHandoff: vi.fn(),
}));

vi.mock("../api/orders", () => ({
  listMyOrders,
  confirmOrderReceived,
  cancelOrder,
  markOrderReadyForHandoff,
}));

const placedOrder = {
  id: 42,
  productId: 17,
  buyerId: 5,
  sellerId: 8,
  buyerUsername: "buyer",
  sellerUsername: "seller",
  listingTitle: "Scientific calculator",
  totalAmount: 125,
  quantity: 1,
  status: "PLACED",
  paymentStatus: "SIMULATED",
  createdAt: "2026-05-20T12:00:00",
  buyerRated: false,
};

describe("order history", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    useAuthStore().user = { id: 5, username: "buyer", role: "STUDENT" };
    listMyOrders.mockReset();
    confirmOrderReceived.mockReset();
    cancelOrder.mockReset();
    markOrderReadyForHandoff.mockReset();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("lets the buyer confirm delivery and then rate the seller", async () => {
    listMyOrders.mockResolvedValue({ data: [structuredClone(placedOrder)] });
    confirmOrderReceived.mockResolvedValue({
      data: { ...placedOrder, status: "RECEIVED", receivedAt: "2026-05-20T13:00:00" },
    });
    const wrapper = mount(OrdersView, {
      global: { stubs: ["RouterLink", "RatingForm"] },
    });
    await flushPromises();

    expect(wrapper.text()).toContain("Scientific calculator");
    expect(wrapper.text()).toContain("Payment: simulated");
    await wrapper.get("button").trigger("click");
    await flushPromises();

    expect(confirmOrderReceived).toHaveBeenCalledWith(42);
    expect(wrapper.text()).toContain("Received");
    expect(wrapper.findComponent({ name: "RatingForm" }).exists()).toBe(true);
  });

  it("does not show buyer receipt actions to sellers", async () => {
    useAuthStore().user = { id: 8, username: "seller", role: "VENDOR" };
    listMyOrders.mockResolvedValue({ data: [placedOrder] });
    const wrapper = mount(OrdersView, {
      global: { stubs: ["RouterLink", "RatingForm"] },
    });
    await flushPromises();

    expect(wrapper.text()).toContain("Buyer: buyer");
    expect(wrapper.text()).not.toContain("Confirm I received this");
    expect(wrapper.text()).not.toContain("Cancel order");
    expect(wrapper.text()).toContain("Mark ready for hand-off");
  });

  it("lets the buyer cancel a placed order after confirmation", async () => {
    vi.spyOn(window, "confirm").mockReturnValue(true);
    listMyOrders.mockResolvedValue({ data: [structuredClone(placedOrder)] });
    cancelOrder.mockResolvedValue({
      data: { ...placedOrder, status: "CANCELLED", cancelledAt: "2026-05-20T13:00:00" },
    });
    const wrapper = mount(OrdersView, {
      global: { stubs: ["RouterLink", "RatingForm"] },
    });
    await flushPromises();

    const cancelButton = wrapper.findAll("button").find((button) => button.text().includes("Cancel order"));
    await cancelButton.trigger("click");
    await flushPromises();

    expect(cancelOrder).toHaveBeenCalledWith(42);
    expect(wrapper.text()).toContain("Cancelled");
    expect(wrapper.text()).not.toContain("Confirm I received this");
    expect(wrapper.text()).not.toContain("Cancel order");
  });

  it("lets the seller mark a placed order ready for hand-off", async () => {
    useAuthStore().user = { id: 8, username: "seller", role: "VENDOR" };
    listMyOrders.mockResolvedValue({ data: [structuredClone(placedOrder)] });
    markOrderReadyForHandoff.mockResolvedValue({
      data: { ...placedOrder, status: "READY_FOR_HANDOFF" },
    });
    const wrapper = mount(OrdersView, {
      global: { stubs: ["RouterLink", "RatingForm"] },
    });
    await flushPromises();

    const readyButton = wrapper.findAll("button").find((button) => button.text().includes("Mark ready"));
    await readyButton.trigger("click");
    await flushPromises();

    expect(markOrderReadyForHandoff).toHaveBeenCalledWith(42);
    expect(wrapper.text()).toContain("Ready for hand-off");
    expect(wrapper.text()).not.toContain("Mark ready for hand-off");
  });
});
