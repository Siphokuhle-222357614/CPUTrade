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

/** "16:45" — just the clock time, for a chat message bubble. */
export function formatTime(value) {
  const match = /T(\d{2}):(\d{2})/.exec(value || "");
  return match ? `${match[1]}:${match[2]}` : "";
}

/** The "2026-09-14" portion, used to group messages by day. */
export function dateKey(value) {
  return (value || "").slice(0, 10);
}

// "now" is a real absolute instant (unlike a zone-less timestamp string),
// so converting it into Johannesburg's civil date via Intl is genuinely
// timezone-safe — this is not the same ambiguous parsing this file
// otherwise avoids. en-CA formats as YYYY-MM-DD.
const JOBURG_DAY_FORMATTER = new Intl.DateTimeFormat("en-CA", {
  timeZone: "Africa/Johannesburg",
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
});

function johannesburgTodayKey(daysAgo = 0) {
  const instant = new Date(Date.now() - daysAgo * 86400000);
  return JOBURG_DAY_FORMATTER.format(instant);
}

/** "Today" / "Yesterday" / "14 September 2026" — a day divider label, chat-app style. */
export function formatDayLabel(value) {
  const key = dateKey(value);
  if (!key) return "";

  if (key === johannesburgTodayKey(0)) return "Today";
  if (key === johannesburgTodayKey(1)) return "Yesterday";

  const FULL_MONTHS = [
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
  ];
  const [year, month, day] = key.split("-");
  return `${Number(day)} ${FULL_MONTHS[Number(month) - 1]} ${year}`;
}
