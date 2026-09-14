<script setup>
import { reactive, ref } from "vue";
import { createBulletinPost } from "../../api/bulletin";

const emit = defineEmits(["posted"]);

const form = reactive({ title: "", body: "", type: "GENERAL" });
const loading = ref(false);
const errorMessage = ref("");

async function handleSubmit() {
  errorMessage.value = "";
  loading.value = true;
  try {
    await createBulletinPost({ title: form.title, body: form.body, type: form.type });
    form.title = "";
    form.body = "";
    form.type = "GENERAL";
    emit("posted");
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not post to the board.";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="card">
    <h4>Post a notice</h4>
    <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
    <form @submit.prevent="handleSubmit">
      <div class="field">
        <label for="bulletin-type">Type</label>
        <select id="bulletin-type" v-model="form.type">
          <option value="GENERAL">General notice</option>
          <option value="LOST">🔴 Lost something</option>
          <option value="FOUND">🟢 Found something</option>
        </select>
      </div>
      <div class="field">
        <label for="bulletin-title">Title</label>
        <input id="bulletin-title" v-model="form.title" type="text" maxlength="150" required />
      </div>
      <div class="field">
        <label for="bulletin-body">Details</label>
        <textarea id="bulletin-body" v-model="form.body" rows="3" maxlength="3000" required></textarea>
      </div>
      <button type="submit" class="btn btn-accent" :disabled="loading">
        {{ loading ? "Posting…" : "Post to Board" }}
      </button>
    </form>
  </div>
</template>
