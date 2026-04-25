<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import AppHeader from '../components/AppHeader.vue'
import { isMockMode } from '../config/runtime.js'
import { createAlertsConnection, loadAlertHistory } from '../services/alertsService.js'
import { formatApiError } from '../services/httpClient.js'

const alerts = ref([])
const connectionState = ref('Disconnected')
const connectionError = ref('')
let alertsConnection = null

const latestAlertTime = computed(() => {
  if (!alerts.value.length) return 'No alerts yet'
  return formatDate(alerts.value[0].triggeredAt ?? alerts.value[0].occurredAt ?? alerts.value[0].timestamp)
})

async function connectAlerts() {
  disconnectAlerts()
  connectionError.value = ''

  try {
    const history = await loadAlertHistory()
    alerts.value = history.slice().reverse()
  } catch (error) {
    connectionError.value = formatApiError(error, 'Failed to load alert history.')
  }

  alertsConnection = createAlertsConnection({
    onAlert: (alert) => {
      alerts.value = [alert, ...alerts.value]
    },
    onStateChange: (state) => {
      connectionState.value = state
    },
    onError: (message) => {
      connectionError.value = message
    },
  })

  await alertsConnection.connect()
}

function disconnectAlerts() {
  if (alertsConnection) {
    alertsConnection.disconnect()
    alertsConnection = null
  }
}

function formatDate(value) {
  if (!value) return 'Unknown time'
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'medium',
  }).format(new Date(value))
}

function formatBoolean(value) {
  return value ? 'enabled' : 'disabled'
}

onMounted(connectAlerts)
onBeforeUnmount(disconnectAlerts)
</script>

<template>
  <section class="grid gap-6">
    <AppHeader
      title="Alerts"
      :intro="isMockMode ? 'Mock alerts are served through MSW and a simulated live stream.' : 'Live alerts from the data processing websocket will appear here.'"
    />

    <section class="grid gap-4 md:grid-cols-3">
      <article class="rounded-lg border border-slate-200 bg-white p-4">
        <span class="text-sm font-semibold text-slate-500">Connection</span>
        <p class="mt-2 text-2xl font-semibold">{{ connectionState }}</p>
      </article>
      <article class="rounded-lg border border-slate-200 bg-white p-4">
        <span class="text-sm font-semibold text-slate-500">Received</span>
        <p class="mt-2 text-2xl font-semibold">{{ alerts.length }}</p>
      </article>
      <article class="rounded-lg border border-slate-200 bg-white p-4">
        <span class="text-sm font-semibold text-slate-500">Latest alert</span>
        <p class="mt-2 text-base font-semibold">{{ latestAlertTime }}</p>
      </article>
    </section>

    <section class="rounded-lg border border-slate-200 bg-white p-5">
      <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 class="text-2xl font-semibold">Alert stream</h2>
          <p class="mt-1 text-slate-500">Subscribed to <span class="font-mono">/topic/alerts</span>.</p>
        </div>
        <button
          type="button"
          class="inline-flex min-h-10 items-center justify-center rounded-lg border border-slate-300 bg-white px-4 text-sm font-medium text-ink transition-colors hover:border-slate-400"
          @click="connectAlerts"
        >
          Reconnect
        </button>
      </div>

      <p v-if="connectionError" class="mt-4 rounded-lg border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700">
        {{ connectionError }}
      </p>

      <div v-if="alerts.length" class="mt-5 grid gap-3">
        <article
          v-for="(alert, index) in alerts"
          :key="`${alert.alertId ?? alert.id ?? alert.triggeredAt ?? index}`"
          class="rounded-lg border border-slate-200 bg-slate-50 p-4"
        >
          <div class="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <h3 class="text-lg font-semibold">{{ alert.alertName ?? 'Alert' }}</h3>
              <p class="text-sm text-slate-500">{{ formatDate(alert.triggeredAt ?? alert.occurredAt ?? alert.timestamp) }}</p>
            </div>
            <span class="rounded-full bg-rose-100 px-3 py-1 text-sm font-semibold text-rose-700">
              {{ alert.completions?.length ?? alert.data?.length ?? 0 }} rule completions
            </span>
          </div>

          <div class="mt-3 grid gap-2 text-sm text-slate-600 sm:grid-cols-2">
            <p><span class="font-semibold text-ink">Alert id:</span> {{ alert.alertId ?? alert.id ?? 'Unknown' }}</p>
            <p><span class="font-semibold text-ink">Source:</span> {{ alert.dataSourceName ?? alert.sourceName ?? 'Unknown' }}</p>
            <p><span class="font-semibold text-ink">Type:</span> {{ alert.alertType ?? 'Unknown' }}</p>
            <p v-if="alert.traceId"><span class="font-semibold text-ink">Trace:</span> {{ alert.traceId }}</p>
            <p><span class="font-semibold text-ink">Triggered:</span> {{ formatDate(alert.triggeredAt ?? alert.occurredAt ?? alert.timestamp) }}</p>
            <p v-if="alert.firstMatchedAt"><span class="font-semibold text-ink">First matched:</span> {{ formatDate(alert.firstMatchedAt) }}</p>
            <p v-if="alert.lastMatchedAt"><span class="font-semibold text-ink">Last matched:</span> {{ formatDate(alert.lastMatchedAt) }}</p>
            <p v-if="alert.timeWindowMillis"><span class="font-semibold text-ink">Window:</span> {{ alert.timeWindowMillis }} ms</p>
            <p><span class="font-semibold text-ink">AI overview:</span> {{ formatBoolean(alert.aiOverviewEnabled) }}</p>
          </div>

          <div v-if="alert.requiredRules?.length" class="mt-4">
            <p class="text-sm font-semibold text-ink">Required rules</p>
            <div class="mt-2 flex flex-wrap gap-1.5">
              <span
                v-for="ruleName in alert.requiredRules"
                :key="ruleName"
                class="rounded-full border border-slate-300 bg-white px-2 py-0.5 text-xs font-semibold text-slate-700"
              >
                {{ ruleName }}
              </span>
            </div>
          </div>

          <div v-if="(alert.completions?.length ?? alert.data?.length)" class="mt-4 grid gap-2">
            <div
              v-for="completion in (alert.completions ?? alert.data)"
              :key="completion?.ruleName ?? JSON.stringify(completion)"
              class="rounded-lg border border-slate-200 bg-white p-3"
            >
              <div class="flex flex-col gap-1 sm:flex-row sm:items-start sm:justify-between">
                <p class="font-semibold">{{ completion?.ruleName ?? 'Unknown rule' }}</p>
                <p v-if="completion?.timestamp" class="text-xs text-slate-500">{{ formatDate(completion.timestamp) }}</p>
              </div>
              <pre class="mt-2 overflow-auto rounded-lg bg-slate-950 p-3 text-xs text-white">{{ completion?.logs ?? completion }}</pre>
            </div>
          </div>

          <div v-if="alert.sampleLogs?.length" class="mt-4">
            <p class="text-sm font-semibold text-ink">Sample logs</p>
            <pre class="mt-2 overflow-auto rounded-lg bg-slate-950 p-3 text-xs text-white">{{ alert.sampleLogs }}</pre>
          </div>
          <pre v-else class="mt-4 overflow-auto rounded-lg bg-slate-950 p-3 text-xs text-white">{{ alert.raw ?? alert }}</pre>
        </article>
      </div>

      <div v-else class="mt-5 rounded-lg border border-dashed border-slate-300 bg-slate-50 p-8 text-center">
        <h3 class="text-lg font-semibold">No alerts yet</h3>
        <p class="mt-2 text-slate-500">When the backend publishes to the websocket, alerts will show up here.</p>
      </div>
    </section>
  </section>
</template>
