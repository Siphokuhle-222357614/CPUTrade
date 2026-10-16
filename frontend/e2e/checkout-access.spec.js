import { expect, test } from "@playwright/test";
import { getCheckoutListing, signIn } from "./checkout-support";

test("seller cannot order their own listing", async ({ page }) => {
  await signIn(page, "checkout_seller");
  const listing = await getCheckoutListing(page);

  await page.goto(`/checkout/${listing.id}`);

  await expect(page.getByText("You cannot order your own listing.")).toBeVisible();
  await expect(page.getByRole("button", { name: /Place order/ })).toBeDisabled();
});
