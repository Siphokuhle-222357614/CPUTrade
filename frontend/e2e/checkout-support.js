import { expect } from "@playwright/test";

const apiUrl = "http://localhost:8080/api";

export async function getCheckoutListing(page) {
  const response = await page.request.get(`${apiUrl}/products?keyword=E2E%20Checkout%20Item`);
  expect(response.ok()).toBeTruthy();
  const listings = await response.json();
  const listing = listings.find((product) => product.title === "E2E Checkout Item");
  expect(listing).toBeTruthy();
  return listing;
}

export async function signIn(page, username) {
  const response = await page.request.post(`${apiUrl}/auth/login`, {
    data: { username, password: "TestPassword123!" },
  });
  const loginResult = await response.json();
  expect(
    response.ok(),
    `Login failed (${response.status()}): ${JSON.stringify(loginResult)}`,
  ).toBeTruthy();
  const { token, user } = loginResult;

  await page.goto("/");
  await page.evaluate(({ authToken, currentUser }) => {
    localStorage.setItem("cputrade_token", authToken);
    localStorage.setItem("cputrade_user", JSON.stringify(currentUser));
  }, { authToken: token, currentUser: user });
}

export async function placeOrder(page, listing) {
  await page.goto(`/products/${listing.id}`);
  await page.getByRole("button", { name: /Buy this item/ }).click();
  await expect(page).toHaveURL(/\/checkout\/\d+$/);
  await page.getByRole("button", { name: /Place order/ }).click();
  await expect(page.getByRole("heading", { name: "Order placed" })).toBeVisible();
}
