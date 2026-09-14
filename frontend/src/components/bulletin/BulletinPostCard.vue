<script setup>
import { computed, ref } from "vue";
import { useAuthStore } from "../../stores/auth";
import { deleteBulletinPost } from "../../api/bulletin";

const props = defineProps({
  post: {
    type: Object,
    required: true,
  },
});
const emit = defineEmits(["deleted"]);

const auth = useAuthStore();
const deleting = ref(false);
const errorMessage = ref("");

const canDelete = computed(() => auth.user?.id === props.post.authorId || auth.isAdmin);

async function handleDelete() {
  if (!window.confirm("Remove this post?")) return;
  deleting.value = true;
  errorMessage.value = "";
  try {
    await deleteBulletinPost(props.post.id);
    emit("deleted");
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not remove this post.";
  } finally {
    deleting.value = false;
  }
}

function formatDate(value) {
  return new Date(value).toLocaleString();
}
</script>

<template>
  <div class="card">
    <div class="row">
      <strong>{{ post.title }}</strong>
      <button v-if="canDelete" type="button" class="btn btn-danger" :disabled="deleting" @click="handleDelete">
        {{ deleting ? "Removing…" : "Remove" }}
      </button>
    </div>
    <p style="white-space: pre-wrap">{{ post.body }}</p>
    <p class="field-hint">Posted by {{ post.authorUsername }} · {{ formatDate(post.createdAt) }}</p>
    <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
  </div>
</template>
