<script setup>
import { onMounted, ref } from "vue";
import {
  listMyOrders,
  confirmOrderReceived,
  cancelOrder as cancelOrderRequest,
  markOrderReadyForHandoff,
} from "../api/orders";
import { useAuthStore } from "../stores/auth";
import RatingForm from "../components/product/RatingForm.vue";

const auth = useAuthStore();
const orders = ref([]);
const loading = ref(true);
const errorMessage = ref("");
const confirmingOrderId = ref(null);
const cancellingOrderId = ref(null);
const preparingOrderId = ref(null);
const actionError = ref("");

function isBuyer(order) {
  return Number(order.buyerId) === Number(auth.user?.id);
}

function formatPrice(price) {
  const value = Number(price);
  return value === 0 ? "R0 · Free" : `R${value.toFixed(2).replace(/\.00$/, "")}`;
}

function formatDate(value) {
  return new Date(value).toLocaleString("en-ZA");
}

async function loadOrders() {
  loading.value = true;
  errorMessage.value = "";
  try {
    const { data } = await listMyOrders();
    orders.value = data;
  } catch (err) {
    errorMessage.value = err.response?.data?.message || "Could not load your orders.";
  } finally {
    loading.value = false;
  }
}

async function confirmReceipt(order) {
  confirmingOrderId.value = order.id;
  actionError.value = "";
  try {
    const { data } = await confirmOrderReceived(order.id);
    Object.assign(order, data);
  } catch (err) {
    actionError.value = err.response?.data?.message || "Could not confirm receipt.";
  } finally {
    confirmingOrderId.value = null;
  }
}

async function cancelOrder(order) {
  if (!window.confirm("Cancel this order? The listing's stock will be returned.")) return;

  cancellingOrderId.value = order.id;
  actionError.value = "";
  try {
    const { data } = await cancelOrderRequest(order.id);
    Object.assign(order, data);
  } catch (err) {
    actionError.value = err.response?.data?.message || "Could not cancel this order.";
  } finally {
    cancellingOrderId.value = null;
  }
}

async function markReadyForHandoff(order) {
  preparingOrderId.value = order.id;
  actionError.value = "";
  try {
    const { data } = await markOrderReadyForHandoff(order.id);
    Object.assign(order, data);
  } catch (err) {
    actionError.value = err.response?.data?.message || "Could not update this order.";
  } finally {
    preparingOrderId.value = null;
  }
}

function markRated(order) {
  order.buyerRated = true;
}

onMounted(loadOrders);
</script>

<template>
  <h1>My Orders</h1>
  <p class="field-hint">Orders you placed or sales made through simulated checkout.</p>

  <p v-if="loading" role="status" aria-live="polite">Loading orders…</p>
  <p v-else-if="errorMessage" class="alert alert-error" role="alert">{{ errorMessage }}</p>
  <p v-else-if="!orders.length" class="empty-state">You don't have any orders yet.</p>

  <template v-else>
    <p v-if="actionError" class="alert alert-error" role="alert">{{ actionError }}</p>
    <div class="table-list">
      <article v-for="order in orders" :key="order.id" class="card">
        <div class="row" style="justify-content: space-between">
          <h2>{{ order.listingTitle }}</h2>
          <span
            class="badge"
            role="status"
            aria-live="polite"
            :class="{
              'badge-success': order.status === 'RECEIVED',
              'badge-danger': order.status === 'CANCELLED',
              'badge-condition': order.status === 'PLACED' || order.status === 'READY_FOR_HANDOFF',
            }"
          >
            {{
              order.status === "RECEIVED"
                ? "Received"
                : order.status === "CANCELLED"
                  ? "Cancelled"
                  : order.status === "READY_FOR_HANDOFF"
                    ? "Ready for hand-off"
                    : "Placed"
            }}
          </span>
        </div>
        <p>Order #{{ order.id }} · {{ formatDate(order.createdAt) }}</p>
        <p>
          {{ isBuyer(order) ? `Seller: ${order.sellerUsername}` : `Buyer: ${order.buyerUsername}` }}
        </p>
        <p>Quantity: {{ order.quantity }} · Total: {{ formatPrice(order.totalAmount) }}</p>
        <p class="field-hint">Payment: simulated — no charge was made.</p>
        <router-link
          :to="{ name: 'product-detail', params: { id: order.productId } }"
          class="btn btn-outline"
        >
          View listing
        </router-link>

        <div v-if="isBuyer(order) && (order.status === 'PLACED' || order.status === 'READY_FOR_HANDOFF')" class="alert alert-info" style="margin-top: var(--space-3)">
          <p v-if="order.status === 'READY_FOR_HANDOFF'">The seller marked this order ready for hand-off. When you have received the item, confirm receipt to complete the order and rate the seller.</p>
          <p v-else>When the item has been handed over, confirm receipt to complete the order and rate the seller.</p>
          <button
            type="button"
            class="btn btn-accent"
            :aria-label="`Confirm receipt for order ${order.id}`"
            :aria-busy="confirmingOrderId === order.id"
            :disabled="confirmingOrderId === order.id"
            @click="confirmReceipt(order)"
          >
            {{ confirmingOrderId === order.id ? "Confirming…" : "Confirm I received this" }}
          </button>
          <button
            type="button"
            class="btn btn-outline"
            :aria-label="`Cancel order ${order.id}`"
            :aria-busy="cancellingOrderId === order.id"
            :disabled="cancellingOrderId === order.id"
            @click="cancelOrder(order)"
          >
            {{ cancellingOrderId === order.id ? "Cancelling…" : "Cancel order" }}
          </button>
        </div>
        <div v-if="!isBuyer(order) && order.status === 'PLACED'" class="alert alert-info" style="margin-top: var(--space-3)">
          <p>Mark this order ready when you are prepared to arrange hand-off with the buyer.</p>
          <button
            type="button"
            class="btn btn-accent"
            :aria-label="`Mark order ${order.id} ready for hand-off`"
            :aria-busy="preparingOrderId === order.id"
            :disabled="preparingOrderId === order.id"
            @click="markReadyForHandoff(order)"
          >
            {{ preparingOrderId === order.id ? "Updating…" : "Mark ready for hand-off" }}
          </button>
        </div>
        <div v-if="isBuyer(order) && order.status === 'RECEIVED'">
          <p v-if="order.buyerRated" class="alert alert-info">Thanks — you've rated this seller.</p>
          <RatingForm v-else :product-id="order.productId" @rated="markRated(order)" />
        </div>
      </article>
    </div>
  </template>
</template>
