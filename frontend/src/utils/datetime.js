// The backend's clock is hardcoded to Africa/Johannesburg (GMT+2, no DST —
// see CpuTradeBackendApplication) and every timestamp it sends is that
// wall-clock value with no timezone suffix (e.g. "2026-09-14T16:45:40").
//
// We deliberately format that string directly instead of routing it through
// `new Date(value).toLocaleString()`. Per the JS spec, a date-time string
// with no timezone offset is parsed as the *viewer's own* local time — so a
// viewer whose device isn't set to SAST would see the timestamp silently
// shifted. Since this app is single-region by design (CPUT students in
// Johannesburg), every viewer should see the exact same wall-clock string
// the backend produced, so no Date/timezone math happens here at all.
const MONTHS = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

/** "14 Sep 2026, 16:45" — or "" if value is missing/unparseable. */
export function formatDateTime(value) {
  if (!value) return "";
  const match = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/.exec(value);
  if (!match) return value;
  const [, year, month, day, hour, minute] = match;
  return `${Number(day)} ${MONTHS[Number(month) - 1]} ${year}, ${hour}:${minute}`;
}

/**
 * Converts a <input type="datetime-local"> value ("2026-09-20T14:00", no
 * timezone) into the exact string the backend's LocalDateTime field expects
 * — appending seconds only. Never round-trip this through `new Date(...)
 * .toISOString()`: that converts through UTC and back, silently shifting
 * the admin's intended Johannesburg time by two hours.
 */
export function localInputToBackend(datetimeLocalValue) {
  if (!datetimeLocalValue) return null;
  return datetimeLocalValue.length === 16 ? `${datetimeLocalValue}:00` : datetimeLocalValue;
}
