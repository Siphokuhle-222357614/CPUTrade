// US7.1: a lightweight offline cache so a shopper with a flaky campus
// connection can still browse the last listings/product they successfully
// loaded. This is deliberately simple — localStorage, not a service worker —
// consistent with this project's fast-path approach to Could-have stories.
// Every read/write is wrapped in try/catch: a private window, cleared site
// data, or a full quota must never break the page.

const PREFIX = "cputrade_cache_";

export function saveCache(key, data) {
  try {
    localStorage.setItem(PREFIX + key, JSON.stringify({ data, cachedAt: Date.now() }));
  } catch (err) {
    // storage unavailable/full — caching is a nice-to-have, not required
  }
}

/** Returns { data, cachedAt } or null if nothing cached (or storage is unavailable). */
export function loadCache(key) {
  try {
    const raw = localStorage.getItem(PREFIX + key);
    return raw ? JSON.parse(raw) : null;
  } catch (err) {
    return null;
  }
}
