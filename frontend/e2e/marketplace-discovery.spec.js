import { expect, test } from "@playwright/test";

test("visitor can search and filter marketplace listings", async ({ page }) => {
  const listing = page.getByRole("link", { name: /E2E Checkout Item/ });

  await page.goto("/");
  await page.getByLabel("Search").fill("E2E Checkout Item");
  await expect(listing).toBeVisible();

  await page.getByLabel("Campus").selectOption("BELLVILLE");
  await page.getByRole("button", { name: "Electronics" }).click();
  await expect(listing).toBeVisible();

  await page.getByLabel("Max price (R)").fill("100");
  await expect(listing).toHaveCount(0);
  await expect(page.getByText("No listings here yet — be the first to post one!")).toBeVisible();

  await page.getByLabel("Max price (R)").fill("150");
  await expect(listing).toBeVisible();

  await page.getByRole("button", { name: /Open to Trade/ }).click();
  await expect(listing).toHaveCount(0);
});
