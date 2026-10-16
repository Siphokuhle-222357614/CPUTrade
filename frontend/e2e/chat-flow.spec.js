import { expect, test } from "@playwright/test";
import { getCheckoutListing, signIn } from "./checkout-support";

test("buyer can message a seller and the seller can read the persisted message", async ({ page }) => {
  const listing = await getCheckoutListing(page);
  const message = `E2E chat message ${Date.now()}`;

  await signIn(page, "checkout_buyer");
  await page.goto(`/products/${listing.id}`);
  await page.getByRole("button", { name: /Message Seller/ }).click();
  await expect(page).toHaveURL(/\/chats\/\d+$/);

  await page.getByLabel("Message", { exact: true }).fill(message);
  await page.getByLabel("Suggest a safe meetup spot (optional)").selectOption("LIBRARY");
  await page.getByRole("button", { name: "Send", exact: true }).click();
  await expect(page.getByText(message, { exact: true })).toBeVisible();
  await expect(page.getByText("📍 Library — Ground Floor (safe meetup suggested)", { exact: true })).toBeVisible();

  await signIn(page, "checkout_seller");
  await page.goto("/chats");
  await page.getByRole("link", { name: new RegExp(listing.title) }).click();
  await expect(page.getByText(message, { exact: true })).toBeVisible();
  await expect(page.getByText("📍 Library — Ground Floor (safe meetup suggested)", { exact: true })).toBeVisible();
});
