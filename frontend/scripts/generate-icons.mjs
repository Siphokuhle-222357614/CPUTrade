// One-off generator: rasterizes scripts/icon-source.svg into every PWA icon
// size the various platforms ask for. Run with `node scripts/generate-icons.mjs`
// whenever the source SVG changes -- the PNGs it writes into public/icons/
// are committed, not built on the fly, so the app installs correctly even
// without Node/sharp available at deploy time.
import sharp from "sharp";
import { mkdirSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";

const __dirname = dirname(fileURLToPath(import.meta.url));
const source = join(__dirname, "icon-source.svg");
const outDir = join(__dirname, "..", "public", "icons");
mkdirSync(outDir, { recursive: true });

// Android/Chrome/desktop manifest icons, plus the two Apple/Windows sizes
// that must exist as real files referenced by <link>/<meta> tags in
// index.html (those two aren't read from the manifest by their platforms).
const targets = [
  { file: "icon-192.png", size: 192 },
  { file: "icon-512.png", size: 512 },
  { file: "icon-maskable-192.png", size: 192 },
  { file: "icon-maskable-512.png", size: 512 },
  { file: "apple-touch-icon.png", size: 180 },
  { file: "mstile-150.png", size: 150 },
];

for (const { file, size } of targets) {
  await sharp(source).resize(size, size).png().toFile(join(outDir, file));
  console.log(`wrote ${file} (${size}x${size})`);
}
