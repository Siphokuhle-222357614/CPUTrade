import { expect, test } from "@playwright/test";
import { getCheckoutListing, placeOrder, signIn } from "./checkout-support";

const apiUrl = "http://localhost:8080/api";

test("buyer can cancel an order and the listing stock is restored", async ({ page }) => {
  await signIn(page, "checkout_buyer");
  const listing = await getCheckoutListing(page);
  const availableStock = listing.quantity;

  await placeOrder(page, listing);
  await page.getByRole("link", { name: "View my orders" }).click();

  const order = page.locator("article").filter({ hasText: listing.title }).first();
  await page.once("dialog", async (dialog) => {
    expect(dialog.message()).toBe("Cancel this order? The listing's stock will be returned.");
    await dialog.accept();
  });
  await order.getByRole("button", { name: /Cancel order/ }).click();

  await expect(order.getByText("Cancelled")).toBeVisible();
  const listingResponse = await page.request.get(`${apiUrl}/products/${listing.id}`);
  expect(listingResponse.ok()).toBeTruthy();
  expect((await listingResponse.json()).quantity).toBe(availableStock);
});

test("seller marks an order ready and the buyer confirms receipt", async ({ page }) => {
  await signIn(page, "checkout_buyer");
  const listing = await getCheckoutListing(page);
  await placeOrder(page, listing);

  await signIn(page, "checkout_seller");
  await page.goto("/orders");
  const sellerOrder = page.locator("article").filter({ hasText: listing.title }).first();
  await sellerOrder.getByRole("button", { name: /Mark order \d+ ready for hand-off/ }).click();
  await expect(sellerOrder.getByText("Ready for hand-off", { exact: true })).toBeVisible();

  await signIn(page, "checkout_buyer");
  await page.goto("/orders");
  const buyerOrder = page.locator("article").filter({ hasText: listing.title }).first();
  await expect(buyerOrder.getByText("Ready for hand-off", { exact: true })).toBeVisible();
  await buyerOrder.getByRole("button", { name: /Confirm receipt for order/ }).click();
  await expect(buyerOrder.getByText("Received")).toBeVisible();
});
