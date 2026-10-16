import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import { flushPromises, mount } from "@vue/test-utils";
import CheckoutView from "./CheckoutView.vue";
import { useAuthStore } from "../stores/auth";

const { getProduct, checkout } = vi.hoisted(() => ({
  getProduct: vi.fn(),
  checkout: vi.fn(),
}));

vi.mock("../api/products", () => ({ getProduct }));
vi.mock("../api/orders", () => ({ checkout }));

const listing = {
  id: 17,
  title: "Scientific calculator",
  sellerUsername: "seller",
  price: 125,
  quantity: 2,
  active: true,
  sold: false,
};

describe("simulated checkout", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    useAuthStore().user = { id: 5, role: "STUDENT" };
    getProduct.mockReset();
    checkout.mockReset();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("places one order without collecting card details", async () => {
    getProduct.mockResolvedValue({ data: listing });
    checkout.mockResolvedValue({ data: { id: 42, paymentStatus: "SIMULATED" } });
    const wrapper = mount(CheckoutView, {
      props: { id: "17" },
      global: { stubs: ["RouterLink"] },
    });
    await flushPromises();

    expect(wrapper.text()).toContain("No money will be charged");
    expect(wrapper.text()).toContain("Scientific calculator");
    expect(wrapper.find("input").exists()).toBe(false);
    await wrapper.get("button").trigger("click");
    await flushPromises();

    expect(checkout).toHaveBeenCalledWith("17");
    expect(wrapper.text()).toContain("Order #42 is recorded");
    expect(wrapper.text()).toContain("no charge was made");
  });

  it("shows checkout errors returned when availability changes", async () => {
    getProduct.mockResolvedValue({ data: listing });
    checkout.mockRejectedValue({
      response: { data: { message: "This listing is no longer available" } },
    });
    const wrapper = mount(CheckoutView, {
      props: { id: 17 },
      global: { stubs: ["RouterLink"] },
    });
    await flushPromises();
    await wrapper.get("button").trigger("click");
    await flushPromises();

    expect(wrapper.text()).toContain("This listing is no longer available");
  });

  it("does not allow ordering a listing with no stock", async () => {
    getProduct.mockResolvedValue({ data: { ...listing, quantity: 0 } });
    const wrapper = mount(CheckoutView, {
      props: { id: 17 },
      global: { stubs: ["RouterLink"] },
    });
    await flushPromises();

    expect(wrapper.text()).toContain("This listing is no longer available to order");
    expect(wrapper.find("button").element.disabled).toBe(true);
    expect(checkout).not.toHaveBeenCalled();
  });
});
