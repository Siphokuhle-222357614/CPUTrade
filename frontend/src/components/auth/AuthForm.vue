<script setup>
import { reactive, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useAuthStore } from "../../stores/auth";

const auth = useAuthStore();
const router = useRouter();
const route = useRoute();

const mode = ref("login"); // "login" | "register"
const loading = ref(false);
const errorMessage = ref("");
const infoMessage = ref("");

const form = reactive({
  username: "",
  email: "",
  password: "",
  role: "STUDENT",
});

function resetMessages() {
  errorMessage.value = "";
  infoMessage.value = "";
}

async function handleSubmit() {
  resetMessages();
  loading.value = true;
  try {
    if (mode.value === "register") {
      await auth.register({
        username: form.username,
        email: form.email,
        password: form.password,
        role: form.role,
      });
      infoMessage.value =
        form.role === "VENDOR"
          ? "Account created! A vendor account needs admin approval before you can sell — you can log in now."
          : "Account created! You can log in now.";
      mode.value = "login";
    } else {
      await auth.login({ username: form.username, password: form.password });
      router.push(route.query.redirect || { name: "marketplace" });
    }
  } catch (err) {
    errorMessage.value =
      err.response?.data?.message || "Something went wrong — please try again.";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="card" style="max-width: 420px; margin: var(--space-6) auto">
    <h2>{{ mode === "login" ? "Sign in" : "Create an account" }}</h2>

    <div class="pill-row" style="margin-bottom: var(--space-4)">
      <button
        type="button"
        class="pill"
        :class="{ active: mode === 'login' }"
        @click="mode = 'login'; resetMessages()"
      >
        Sign In
      </button>
      <button
        type="button"
        class="pill"
        :class="{ active: mode === 'register' }"
        @click="mode = 'register'; resetMessages()"
      >
        Register
      </button>
    </div>

    <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
    <p v-if="infoMessage" class="alert alert-info">{{ infoMessage }}</p>

    <form @submit.prevent="handleSubmit">
      <div class="field">
        <label for="username">Username</label>
        <input
          id="username"
          v-model="form.username"
          type="text"
          placeholder="3-20 alphanumeric characters"
          required
          minlength="3"
          maxlength="20"
        />
      </div>

      <div class="field" v-if="mode === 'register'">
        <label for="email">Email address</label>
        <input id="email" v-model="form.email" type="email" placeholder="your.real@email.com" required />
      </div>

      <div class="field">
        <label for="password">Password</label>
        <input
          id="password"
          v-model="form.password"
          type="password"
          placeholder="At least 8 characters"
          required
          minlength="8"
        />
      </div>

      <div class="field" v-if="mode === 'register'">
        <label for="role">Role</label>
        <select id="role" v-model="form.role">
          <option value="STUDENT">Student</option>
          <option value="VENDOR">Vendor</option>
        </select>
        <p class="field-hint">Vendor accounts require admin approval before you can sell.</p>
      </div>

      <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
        {{ loading ? "Please wait…" : mode === "login" ? "Sign In" : "Create Account" }}
      </button>
    </form>
  </div>
</template>
