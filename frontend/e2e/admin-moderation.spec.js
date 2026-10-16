import { expect, test } from "@playwright/test";
import { getCheckoutListing, signIn } from "./checkout-support";

const apiUrl = "http://localhost:8080/api";

test("admin can review a report and the decision appears in action history", async ({ page }) => {
  test.setTimeout(60_000);
  const listing = await getCheckoutListing(page);
  const reportDetails = `E2E moderation report ${Date.now()}`;

  await signIn(page, "checkout_buyer");
  const token = await page.evaluate(() => localStorage.getItem("cputrade_token"));
  const deniedResponse = await page.request.get(`${apiUrl}/admin/history`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  expect(deniedResponse.status()).toBe(403);

  await page.goto(`/products/${listing.id}`);
  await page.getByRole("button", { name: /Report/ }).click();
  await expect(page.getByRole("dialog", { name: "Report listing" })).toBeVisible();
  await page.getByLabel("Details (optional)").fill(reportDetails);
  await page.getByRole("button", { name: "Submit Report" }).click();
  await expect(page.getByText("Thanks — an admin will review this. Your report is confidential.")).toBeVisible();

  await signIn(page, "e2e_admin");
  await page.goto("/admin");

  await expect(page.getByRole("heading", { name: "Reports Queue" })).toBeVisible();
  await expect(page.getByText(reportDetails)).toBeVisible();
  await page.getByRole("button", { name: /Mark report .* reviewed/ }).click();

  await expect(page.getByText(reportDetails)).toHaveCount(0);
  await page.reload();
  await expect(page.getByRole("heading", { name: "Recent Admin Actions" })).toBeVisible();
  await expect(page.getByText("Report reviewed", { exact: true })).toBeVisible();
  await expect(page.getByText(new RegExp(`acted on report “${listing.title}”`))).toBeVisible();
});
