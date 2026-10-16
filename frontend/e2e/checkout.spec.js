import { expect, test } from "@playwright/test";
import { getCheckoutListing, placeOrder, signIn } from "./checkout-support";

test("buyer places an order with simulated payment and sees it in history", async ({ page }) => {
  await signIn(page, "checkout_buyer");
  const listing = await getCheckoutListing(page);

  await placeOrder(page, listing);
  await expect(page.getByText("No money will be charged")).toBeVisible();
  await expect(page.locator('input, [autocomplete*="cc-"], [name*="card" i]')).toHaveCount(0);

  await page.getByRole("link", { name: "View my orders" }).click();
  const order = page.locator("article").filter({ hasText: listing.title }).first();
  await expect(order).toBeVisible();
  await expect(order.getByText("Payment: simulated — no charge was made.")).toBeVisible();
});
