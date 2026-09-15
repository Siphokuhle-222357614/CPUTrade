import { createApp } from "vue";
import { createPinia } from "pinia";
import "./styles/tokens.css";
import "./styles/base.css";
import App from "./App.vue";
import router from "./router";

const app = createApp(App);

app.use(createPinia());
app.use(router);

app.mount("#app");

// Fade out and remove the boot splash (see index.html) now that the app has
// actually rendered — rAF twice so the first real paint has happened before
// it starts fading, instead of racing it.
const splash = document.getElementById("app-splash");
if (splash) {
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      splash.classList.add("splash-hide");
      setTimeout(() => splash.remove(), 400);
    });
  });
}
