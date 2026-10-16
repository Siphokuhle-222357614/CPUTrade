import client from "./client";

export function anonymizeAccount(password) {
  return client.delete("/account", { data: { password } });
}