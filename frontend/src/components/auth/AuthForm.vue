<script setup>
import { reactive, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useAuthStore } from "../../stores/auth";
import * as authApi from "../../api/auth";
import SuspendedAccountDialog from "./SuspendedAccountDialog.vue";

const auth = useAuthStore();
const router = useRouter();
const route = useRoute();

const mode = ref("login"); // "login" | "register"
const loading = ref(false);
const errorMessage = ref("");
const infoMessage = ref("");
const showSuspendedDialog = ref(false);
const suspendedMessage = ref("");
const showResendVerification = ref(false);
const verificationEmail = ref("");

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
  showResendVerification.value = false;
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
          : "Account created! A verification link has been sent to your CPUT email. Verify it before signing in; in local development, check the backend log.";
      mode.value = "login";
    } else {
      await auth.login({ username: form.username, password: form.password });
      router.push(route.query.redirect || { name: "marketplace" });
    }
  } catch (err) {
    const message = err.response?.data?.message || "Something went wrong — please try again.";
    const suspended = err.response?.status === 403 && message.toLowerCase().includes("suspended");
    const verificationRequired =
      err.response?.status === 403 && message.toLowerCase().includes("verify your cput email");
    if (verificationRequired) {
      showResendVerification.value = true;
      errorMessage.value = message;
    } else if (suspended) {
      // A suspension is important enough to interrupt with a popup rather
      // than blend into the usual inline error banner.
      suspendedMessage.value = message;
      showSuspendedDialog.value = true;
    } else {
      errorMessage.value = message;
    }
  } finally {
    loading.value = false;
  }
}

async function handleResendVerification() {
  resetMessages();
  loading.value = true;
  try {
    await authApi.resendVerification(verificationEmail.value);
    infoMessage.value =
      "If that address belongs to an unverified student account, a new link has been sent. Check your inbox or the local backend log.";
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Unable to request a verification link — please try again.";
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
    <form
      v-if="showResendVerification"
      class="field"
      @submit.prevent="handleResendVerification"
    >
      <label for="verification-email">Resend verification link</label>
      <input
        id="verification-email"
        v-model="verificationEmail"
        type="email"
        placeholder="yourname@mycput.ac.za"
        required
      />
      <button type="submit" class="btn btn-secondary" :disabled="loading">
        {{ loading ? "Please wait…" : "Resend link" }}
      </button>
    </form>

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
        <label for="role">I am a</label>
        <select id="role" v-model="form.role">
          <option value="STUDENT">Student</option>
          <option value="VENDOR">Vendor</option>
        </select>
        <p class="field-hint">Vendor accounts require admin approval before you can sell.</p>
      </div>

      <div class="field" v-if="mode === 'register'">
        <label for="email">Email address</label>
        <input
          id="email"
          v-model="form.email"
          type="email"
          :placeholder="form.role === 'STUDENT' ? 'yourname@mycput.ac.za' : 'your.business@email.com'"
          required
        />
        <p v-if="form.role === 'STUDENT'" class="field-hint">
          🎓 Students must register with an official CPUT email (@mycput.ac.za or @cput.ac.za) — this keeps the
          marketplace CPUT-only.
        </p>
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

      <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
        {{ loading ? "Please wait…" : mode === "login" ? "Sign In" : "Create Account" }}
      </button>
    </form>

    <SuspendedAccountDialog
      :open="showSuspendedDialog"
      :message="suspendedMessage"
      :username="form.username"
      @close="showSuspendedDialog = false"
    />
  </div>
</template>
