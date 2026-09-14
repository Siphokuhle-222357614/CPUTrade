import client from "./client";

export function register({ username, email, password, role }) {
  return client.post("/auth/register", { username, email, password, role });
}

export function login({ username, password }) {
  return client.post("/auth/login", { username, password });
}
