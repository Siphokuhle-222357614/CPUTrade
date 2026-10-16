import { beforeEach, describe, expect, it, vi } from "vitest";

describe("offline listing cache", () => {
  beforeEach(() => {
    localStorage.clear();
    vi.resetModules();
  });

  it("saves and reads listing data using the current cache version", async () => {
    const { loadCache, saveCache } = await import("./offlineCache");
    const listings = [{ id: 12, title: "Current listing" }];

    saveCache("marketplace_all", listings);

    expect(loadCache("marketplace_all").data).toEqual(listings);
    expect(localStorage.getItem("cputrade_cache_v2_marketplace_all")).not.toBeNull();
  });

  it("removes stale offline listing snapshots from the previous cache version", async () => {
    localStorage.setItem(
      "cputrade_cache_marketplace_all",
      JSON.stringify({ data: [{ id: 1, title: "Deleted test listing" }] }),
    );
    localStorage.setItem("unrelated-setting", "keep");

    const { loadCache } = await import("./offlineCache");

    expect(localStorage.getItem("cputrade_cache_marketplace_all")).toBeNull();
    expect(loadCache("marketplace_all")).toBeNull();
    expect(localStorage.getItem("unrelated-setting")).toBe("keep");
  });
});
