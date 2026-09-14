<script setup>
import { computed, ref } from "vue";
import { useAuthStore } from "../../stores/auth";
import { deleteBulletinPost } from "../../api/bulletin";
import ConfirmDialog from "../common/ConfirmDialog.vue";
import ReportDialog from "../trust/ReportDialog.vue";

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
const showDeleteConfirm = ref(false);
const showReportDialog = ref(false);

const canDelete = computed(() => auth.user?.id === props.post.authorId || auth.isAdmin);
const canReport = computed(() => auth.isAuthenticated && auth.user?.id !== props.post.authorId);

async function handleDelete() {
  showDeleteConfirm.value = false;
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
      <span class="row" style="gap: var(--space-2)">
        <button v-if="canReport" type="button" class="btn btn-outline" @click="showReportDialog = true">
          🚩 Report
        </button>
        <button v-if="canDelete" type="button" class="btn btn-danger" :disabled="deleting" @click="showDeleteConfirm = true">
          {{ deleting ? "Removing…" : "Remove" }}
        </button>
      </span>
    </div>
    <p style="white-space: pre-wrap">{{ post.body }}</p>
    <p class="field-hint">Posted by {{ post.authorUsername }} · {{ formatDate(post.createdAt) }}</p>
    <p v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>

    <ConfirmDialog
      :open="showDeleteConfirm"
      title="Remove this post?"
      confirm-label="Remove"
      danger
      @confirm="handleDelete"
      @cancel="showDeleteConfirm = false"
    />
    <ReportDialog
      :open="showReportDialog"
      target-type="USER"
      :target-id="post.authorId"
      :target-label="`Post by ${post.authorUsername}: “${post.title}”`"
      @close="showReportDialog = false"
    />
  </div>
</template>
