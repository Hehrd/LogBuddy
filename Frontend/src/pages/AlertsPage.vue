<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import AlertStreamPanel from '../components/AlertStreamPanel.vue'
import AppHeader from '../components/AppHeader.vue'
import { MockAlertWebSocket } from '../mocks/mockAlertWebSocket.js'
import { normalizeAlert } from '../services/alertFormatter.js'
import { createAlertsSocket, getDefaultAlertsSocketUrl } from '../services/alertsSocket.js'

const alertSocket = ref(null)
const alertSocketStatus = ref('idle')
const liveAlerts = ref([])

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

function connectAlertsSocket() {
  alertSocket.value?.close()

  alertSocket.value = createAlertsSocket({
    url: alertsSocketUrl.value,
    WebSocketImpl: useMockAlerts.value ? MockAlertWebSocket : window.WebSocket,
    onAlert: addLiveAlert,
    onStatusChange: (status) => {
      alertSocketStatus.value = status
    },
    onError: () => {
      alertSocketStatus.value = 'error'
    },
  })
}

function addLiveAlert(alert) {
  liveAlerts.value = [
    normalizeAlert(alert),
    ...liveAlerts.value,
  ]
}

function pingAlertsSocket() {
  alertSocket.value?.send({
    type: 'ping',
    sentAt: new Date().toISOString(),
  })
}

function clearLiveAlerts() {
  liveAlerts.value = []
}

onMounted(() => {
  connectAlertsSocket()
})

onBeforeUnmount(() => {
  alertSocket.value?.close()
})
</script>

<template>
  <section class="grid gap-6">
    <AppHeader
      title="Alerts"
      intro="Watch incoming alert events separately from configuration editing."
    />

    <AlertStreamPanel
      :alerts="liveAlerts"
      :status="alertSocketStatus"
      :using-mocks="useMockAlerts"
      @ping="pingAlertsSocket"
      @clear="clearLiveAlerts"
    />
  </section>
</template>
