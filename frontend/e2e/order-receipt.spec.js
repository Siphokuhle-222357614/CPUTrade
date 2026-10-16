import { expect, test } from "@playwright/test";
import { getCheckoutListing, placeOrder, signIn } from "./checkout-support";

test("buyer confirms receipt and can rate the seller", async ({ page }) => {
  await signIn(page, "checkout_buyer");
  const listing = await getCheckoutListing(page);
  await placeOrder(page, listing);

  await page.getByRole("link", { name: "View my orders" }).click();
  const order = page.locator("article").filter({ hasText: listing.title }).first();
  await order.getByRole("button", { name: /Confirm receipt for order/ }).click();

  await expect(order.getByText("Received")).toBeVisible();
  await expect(order.getByText("Rate this seller")).toBeVisible();
});
