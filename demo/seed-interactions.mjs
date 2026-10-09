// Drives the real API to create demo interactions (chats, wishlist, a sold + rated sale).
const API = 'http://localhost:8080/api';
const tokens = {};
async function call(user, method, path, body) {
  const res = await fetch(API + path, {
    method,
    headers: { 'Content-Type': 'application/json', ...(user ? { Authorization: `Bearer ${tokens[user]}` } : {}) },
    body: body ? JSON.stringify(body) : undefined,
  });
  const text = await res.text();
  let json; try { json = JSON.parse(text); } catch { json = text; }
  if (!res.ok) console.log('FAIL', method, path, res.status, text.slice(0, 200));
  return json;
}
for (const u of ['thabo_s', 'lerato_m', 'aisha_n', 'sizwe_d', 'campusprint']) {
  tokens[u] = (await call(null, 'POST', '/auth/login', { username: u, password: 'Password123!' })).token;
}
const products = await call(null, 'GET', '/products');
const pid = (t) => products.find((p) => p.title.startsWith(t))?.id;

// Wishlist
for (const [u, t] of [['sizwe_d', 'Scientific'], ['sizwe_d', 'Red Kanken'], ['aisha_n', 'Scientific'], ['thabo_s', 'Drawing'], ['thabo_s', 'City bicycle']]) {
  await call(u, 'POST', `/wishlist/${pid(t)}`);
}

async function chat(buyer, seller, title, lines) {
  const conv = await call(buyer, 'POST', `/products/${pid(title)}/conversations`);
  for (const [who, body] of lines) await call(who === 'b' ? buyer : seller, 'POST', `/conversations/${conv.id}/messages`, { body });
  return conv;
}

// Sale that goes all the way: chat -> sold -> buyer confirms -> rating
const earbuds = pid('JBL');
await chat('sizwe_d', 'lerato_m', 'JBL', [
  ['b', 'Hi Lerato, are the earbuds still available?'],
  ['s', 'Hi Sizwe! Yes they are. Battery still lasts the whole day.'],
  ['b', 'Great. Can we meet at the District Six library at 13:00 tomorrow?'],
  ['s', 'Perfect, see you at the library entrance.'],
]);
const sizwe = (await call('lerato_m', 'GET', `/products/${earbuds}/interested-buyers`)).find?.((b) => b.username === 'sizwe_d');
await call('lerato_m', 'PATCH', `/products/${earbuds}/mark-sold`, { soldToUserId: sizwe?.id ?? sizwe?.userId });
await call('sizwe_d', 'PATCH', `/products/${earbuds}/confirm-received`);
await call('sizwe_d', 'POST', `/products/${earbuds}/ratings`, { score: 5, comment: 'Exactly as described and Lerato was easy to meet. Smooth deal!' });

// Open conversations to show in the inbox
await chat('thabo_s', 'lerato_m', 'Red Kanken', [
  ['b', 'Hi! Does the backpack fit a 15 inch laptop?'],
  ['s', 'Yes, with room to spare. I can send more photos if you like.'],
]);
await chat('aisha_n', 'thabo_s', 'Scientific', [
  ['b', 'Hi Thabo, I saw you are open to a swap. Would a Programming 1 textbook work?'],
  ['s', 'Yes, that works for me. Which edition is it?'],
  ['b', 'Latest edition, 2025. Almost no highlighting.'],
]);
await chat('thabo_s', 'campusprint', 'Maths', [
  ['b', 'Do you have a slot on Thursday afternoon for Maths 1?'],
  ['s', 'Yes, 15:00 to 16:00 in Mowbray works. I will bring past papers.'],
]);
console.log('done');
