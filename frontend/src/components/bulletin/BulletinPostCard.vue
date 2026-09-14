<script setup>
import { computed, ref } from "vue";
import { useAuthStore } from "../../stores/auth";
import { deleteBulletinPost, setBulletinPostResolved } from "../../api/bulletin";
import ConfirmDialog from "../common/ConfirmDialog.vue";
import ReportDialog from "../trust/ReportDialog.vue";
import { formatDateTime } from "../../utils/datetime";

const props = defineProps({
  post: {
    type: Object,
    required: true,
  },
});
const emit = defineEmits(["deleted", "changed"]);

const auth = useAuthStore();
const deleting = ref(false);
const updatingResolved = ref(false);
const errorMessage = ref("");
const showDeleteConfirm = ref(false);
const showReportDialog = ref(false);

const isAuthor = computed(() => auth.user?.id === props.post.authorId);
const canDelete = computed(() => isAuthor.value || auth.isAdmin);
const canReport = computed(() => auth.isAuthenticated && !isAuthor.value);
const canToggleResolved = computed(() => (isAuthor.value || auth.isAdmin) && props.post.type !== "GENERAL");

const typeBadge = computed(() => {
  if (props.post.type === "LOST") return { label: "🔴 Lost", cls: "badge-danger" };
  if (props.post.type === "FOUND") return { label: "🟢 Found", cls: "badge-success" };
  return null;
});

const resolvedLabel = computed(() =>
  props.post.type === "LOST" ? "Reunited!" : "Claimed!"
);

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

async function toggleResolved() {
  updatingResolved.value = true;
  errorMessage.value = "";
  try {
    await setBulletinPostResolved(props.post.id, !props.post.resolved);
    emit("changed");
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not update this post.";
  } finally {
    updatingResolved.value = false;
  }
}

function formatDate(value) {
  return formatDateTime(value);
}
</script>

<template>
  <div class="card" :class="{ 'bulletin-resolved': post.resolved }">
    <div class="row">
      <span class="row" style="width: auto; gap: var(--space-2)">
        <span v-if="typeBadge" class="badge" :class="typeBadge.cls">{{ typeBadge.label }}</span>
        <span v-if="post.resolved" class="badge badge-sold">✅ {{ resolvedLabel }}</span>
        <strong>{{ post.title }}</strong>
      </span>
      <span class="row" style="width: auto; gap: var(--space-2)">
        <button v-if="canToggleResolved" type="button" class="btn btn-outline" :disabled="updatingResolved" @click="toggleResolved">
          {{ post.resolved ? "Reopen" : `Mark as ${resolvedLabel}` }}
        </button>
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

<style scoped>
.bulletin-resolved {
  opacity: 0.6;
}
</style>
