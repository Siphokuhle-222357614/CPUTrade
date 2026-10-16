import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia } from "pinia";
import { flushPromises, mount } from "@vue/test-utils";
import MarketplaceView from "./MarketplaceView.vue";
import ProductGrid from "../components/marketplace/ProductGrid.vue";
import { saveCache } from "../utils/offlineCache";

const { listProducts } = vi.hoisted(() => ({
  listProducts: vi.fn(),
}));

vi.mock("../api/products", () => ({
  listProducts,
}));
vi.mock("../api/savedSearches", () => ({
  createSavedSearch: vi.fn(),
}));

describe("marketplace listing freshness", () => {
  beforeEach(() => {
    localStorage.clear();
    listProducts.mockReset();
    saveCache("marketplace_all", [{ id: 1, title: "Deleted test listing" }]);
  });

  function mountMarketplace() {
    return mount(MarketplaceView, {
      global: {
        plugins: [createPinia()],
        stubs: [
          "CategoryFilterPills",
          "SearchAndPriceFilter",
          "ProductGrid",
          "RecentActivityTicker",
          "SkeletonCard",
        ],
      },
    });
  }

  it("uses an empty live response instead of previously cached listings", async () => {
    listProducts.mockResolvedValue({ data: [] });
    const wrapper = mountMarketplace();
    await flushPromises();

    expect(wrapper.findComponent(ProductGrid).props("products")).toEqual([]);
    expect(wrapper.text()).not.toContain("Deleted test listing");
  });

  it("does not display cached listings when the backend returns an error", async () => {
    listProducts.mockRejectedValue({ response: { status: 500 } });
    const wrapper = mountMarketplace();
    await flushPromises();

    expect(wrapper.findComponent(ProductGrid).props("products")).toEqual([]);
    expect(wrapper.text()).not.toContain("Deleted test listing");
    expect(wrapper.text()).toContain("Could not load listings");
  });
});
