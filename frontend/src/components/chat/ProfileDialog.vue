<script setup>
import { ref, watch } from "vue";
import Modal from "../common/Modal.vue";
import { getPublicProfile } from "../../api/profile";
import { formatDateTime } from "../../utils/datetime";

const props = defineProps({
  open: {
    type: Boolean,
    default: false,
  },
  userId: {
    type: [String, Number],
    default: null,
  },
});
const emit = defineEmits(["close"]);

const profile = ref(null);
const loading = ref(false);
const errorMessage = ref("");

watch(
  () => [props.open, props.userId],
  async ([isOpen, userId]) => {
    if (!isOpen || !userId) return;
    loading.value = true;
    errorMessage.value = "";
    profile.value = null;
    try {
      const { data } = await getPublicProfile(userId);
      profile.value = data;
    } catch (err) {
      errorMessage.value = err.response?.data?.message || "Could not load this profile.";
    } finally {
      loading.value = false;
    }
  }
);
</script>

<template>
  <Modal :open="open" :title="profile?.username || 'Profile'" @close="emit('close')">
    <p v-if="loading">Loading…</p>
    <p v-else-if="errorMessage" class="alert alert-error">{{ errorMessage }}</p>
    <template v-else-if="profile">
      <p class="row">
        <span
          class="badge"
          :class="profile.online ? 'badge-success' : 'badge-condition'"
        >
          {{ profile.online ? "🟢 Online" : "⚪ Offline" }}
        </span>
        <span class="badge badge-condition">{{ profile.role }}</span>
      </p>
      <p class="field-hint">{{ profile.campusHandle }}</p>
      <div class="table-list" style="margin-top: var(--space-3)">
        <div class="row">
          <span>Seller rating</span>
          <strong>
            {{ profile.ratingAverage != null ? `⭐ ${profile.ratingAverage.toFixed(1)} (${profile.ratingCount})` : "No ratings yet" }}
          </strong>
        </div>
        <div class="row">
          <span>Active listings</span>
          <strong>{{ profile.activeListingCount }}</strong>
        </div>
        <div class="row">
          <span>Member since</span>
          <strong>{{ formatDateTime(profile.memberSince) }}</strong>
        </div>
        <div v-if="!profile.online && profile.lastActiveAt" class="row">
          <span>Last active</span>
          <strong>{{ formatDateTime(profile.lastActiveAt) }}</strong>
        </div>
      </div>
    </template>
  </Modal>
</template>
