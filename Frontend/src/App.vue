<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink, RouterView } from 'vue-router'
import AlertNotifications from './components/AlertNotifications.vue'
import HealthStatusCard from './components/HealthStatusCard.vue'
import { MockAlertWebSocket } from './mocks/mockAlertWebSocket.js'
import { normalizeAlert } from './services/alertFormatter.js'
import { createAlertsSocket, getDefaultAlertsSocketUrl } from './services/alertsSocket.js'
import { fetchHealth } from './services/backendApi.js'

const notificationSocket = ref(null)
const notificationAlerts = ref([])
const notificationsOpen = ref(false)
const health = ref(null)
const healthLoading = ref(true)
const healthError = ref('')

const alertsMode = computed(() => {
  const mode = import.meta.env.VITE_ALERTS_MODE?.toLowerCase()
  if (mode === 'mock' || mode === 'real') {
    return mode
  }
  return import.meta.env.DEV ? 'mock' : 'real'
})

const useMockAlerts = computed(() => alertsMode.value === 'mock')

const alertsSocketUrl = computed(() => {
  return import.meta.env.VITE_ALERTS_WS_URL ?? getDefaultAlertsSocketUrl()
})

async function loadHealth() {
  healthLoading.value = true
  healthError.value = ''

  try {
    health.value = await fetchHealth()
  } catch (error) {
    healthError.value = error.message ?? 'Health check failed'
  } finally {
    healthLoading.value = false
  }
}

function connectNotificationSocket() {
  notificationSocket.value?.close()

  notificationSocket.value = createAlertsSocket({
    url: alertsSocketUrl.value,
    WebSocketImpl: useMockAlerts.value ? MockAlertWebSocket : window.WebSocket,
    onAlert: (alert) => {
      notificationAlerts.value = [
        normalizeAlert(alert),
        ...notificationAlerts.value,
      ]
    },
  })
}

function toggleNotifications() {
  notificationsOpen.value = !notificationsOpen.value
}

function clearNotifications() {
  notificationAlerts.value = []
}

function closeNotifications() {
  notificationsOpen.value = false
}

onMounted(() => {
  loadHealth()
  connectNotificationSocket()
})

onBeforeUnmount(() => {
  notificationSocket.value?.close()
})
</script>

<template>
  <main class="mx-auto min-h-screen max-w-[1440px] px-4 py-8 text-ink sm:px-5 sm:py-10">
    <header class="mb-6 flex flex-col gap-4 rounded-lg border border-slate-200 bg-white p-4 sm:flex-row sm:items-center sm:justify-between">
      <RouterLink to="/config" class="flex items-center gap-3">
        <img src="/logo/image.png" alt="LogBuddy logo" class="h-11 w-11 rounded-lg object-contain" />
        <div>
          <p class="text-sm font-bold uppercase tracking-normal text-teal">LogBuddy</p>
          <h1 class="text-xl font-semibold">Control panel</h1>
        </div>
      </RouterLink>

      <nav class="flex flex-wrap items-center gap-2 text-sm font-semibold">
        <RouterLink
          to="/config"
          class="rounded-lg border px-4 py-2 transition-colors"
          :class="$route.path === '/config' ? 'border-teal bg-emerald-50 text-ink' : 'border-slate-300 bg-white text-slate-600 hover:border-slate-400'"
        >
          Config
        </RouterLink>
        <RouterLink
          to="/alerts"
          class="rounded-lg border px-4 py-2 transition-colors"
          :class="$route.path === '/alerts' ? 'border-teal bg-emerald-50 text-ink' : 'border-slate-300 bg-white text-slate-600 hover:border-slate-400'"
        >
          Alerts
        </RouterLink>
        <AlertNotifications
          :alerts="notificationAlerts"
          :open="notificationsOpen"
          @toggle="toggleNotifications"
          @clear="clearNotifications"
        />
      </nav>
    </header>

    <button
      v-if="notificationsOpen"
      type="button"
      class="fixed inset-0 z-40 cursor-default bg-transparent"
      aria-label="Close notifications"
      @click="closeNotifications"
    />

    <HealthStatusCard :health="health" :loading="healthLoading" :error="healthError" class="mb-6" />

    <RouterView />
  </main>
</template>
