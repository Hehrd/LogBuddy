<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink, RouterView } from 'vue-router'
import AlertNotifications from './components/AlertNotifications.vue'
import { normalizeAlert } from './services/alertFormatter.js'
import { createAlertsConnection } from './services/alertsService.js'
import { formatApiError } from './services/httpClient.js'
import {
  fetchControlPlaneConfig,
  fetchControlPlaneDataSources,
  fetchControlPlaneRules,
  fetchControlPlaneStreamMetrics,
  fetchControlPlaneStatus,
  fetchHealth,
  fetchSparkQueries,
  restartSparkQueriesAction,
  runControlPlaneAction,
  runSparkQueryAction,
} from './services/backendApi.js'

function extractQueryNames(queriesPayload) {
  if (Array.isArray(queriesPayload)) return queriesPayload
  if (Array.isArray(queriesPayload?.queries)) return queriesPayload.queries
  return Object.keys(queriesPayload ?? {})
}

const alertsConnection = ref(null)
const alerts = ref([])
const alertConnectionState = ref('Disconnected')
const alertConnectionError = ref('')
const notificationsOpen = ref(false)
const health = ref(null)
const healthLoading = ref(true)
const healthError = ref('')
const controlPlaneServices = ref([
  {
    key: 'data-processing',
    label: 'Data Processing',
    loading: false,
    error: '',
    status: null,
    config: null,
    dataSources: null,
    rules: null,
    queries: null,
    queryNames: [],
    streamMetrics: null,
  },
  {
    key: 'spark',
    label: 'Spark Processing',
    loading: false,
    error: '',
    status: null,
    config: null,
    dataSources: null,
    rules: null,
    queries: null,
    queryNames: [],
    streamMetrics: null,
  },
])

const maxCachedAlerts = 100
const notificationAlerts = computed(() => alerts.value.map((alert) => normalizeAlert(alert)))

function getAlertId(alert) {
  return alert.alertId ?? alert.id
}

function getAlertTime(alert) {
  const rawTime = alert.triggeredAt ?? alert.occurredAt ?? alert.timestamp ?? alert.rawAlert?.triggeredAt
  const parsed = rawTime ? new Date(rawTime).getTime() : 0
  return Number.isNaN(parsed) ? 0 : parsed
}

function setOrderedAlerts(nextAlerts) {
  const byId = new Map()

  for (const alert of nextAlerts) {
    const alertId = getAlertId(alert)
    if (!alertId) continue

    const existing = byId.get(alertId)
    if (!existing || getAlertTime(alert) >= getAlertTime(existing)) {
      byId.set(alertId, alert)
    }
  }

  alerts.value = [...byId.values()]
    .sort((left, right) => getAlertTime(right) - getAlertTime(left))
    .slice(0, maxCachedAlerts)
}

function addAlert(alert) {
  setOrderedAlerts([alert, ...alerts.value])
}

async function loadHealth() {
  healthLoading.value = true
  healthError.value = ''

  try {
    health.value = await fetchHealth()
  } catch (error) {
    healthError.value = formatApiError(error, 'Health check failed.')
  } finally {
    healthLoading.value = false
  }
}

function getServiceState(serviceKey) {
  return controlPlaneServices.value.find((service) => service.key === serviceKey)
}

async function refreshControlPlaneService(serviceKey) {
  const service = getServiceState(serviceKey)
  if (!service) return

  service.loading = true
  service.error = ''

  try {
    service.status = await fetchControlPlaneStatus(serviceKey)
    service.config = await fetchControlPlaneConfig(serviceKey)
    service.dataSources = await fetchControlPlaneDataSources(serviceKey)
    service.rules = await fetchControlPlaneRules(serviceKey)

    if (serviceKey === 'data-processing') {
      service.streamMetrics = await fetchControlPlaneStreamMetrics()
    }

    if (serviceKey === 'spark') {
      service.queries = await fetchSparkQueries()
      service.queryNames = extractQueryNames(service.queries)
    }
  } catch (error) {
    service.error = formatApiError(error, `Failed to refresh ${service.label}.`)
  } finally {
    service.loading = false
  }
}

async function runAllServicesAction(action) {
  const services = controlPlaneServices.value

  for (const service of services) {
    service.error = ''
  }

  await Promise.all(
    services.map(async (service) => {
      try {
        await runControlPlaneAction(service.key, action)
        await refreshControlPlaneService(service.key)
      } catch (error) {
        service.error = formatApiError(error, `Failed to run ${action} on ${service.label}.`)
      }
    }),
  )

  await loadHealth()
}

async function startSparkQuery(dataSource) {
  const service = getServiceState('spark')
  if (!service) return

  try {
    await runSparkQueryAction(dataSource, 'start')
    await refreshControlPlaneService('spark')
  } catch (error) {
    service.error = formatApiError(error, `Failed to start query ${dataSource}.`)
  }
}

async function stopSparkQuery(dataSource) {
  const service = getServiceState('spark')
  if (!service) return

  try {
    await runSparkQueryAction(dataSource, 'stop')
    await refreshControlPlaneService('spark')
  } catch (error) {
    service.error = formatApiError(error, `Failed to stop query ${dataSource}.`)
  }
}

async function restartSparkQueries() {
  const service = getServiceState('spark')
  if (!service) return

  service.error = ''

  try {
    await restartSparkQueriesAction()
    await refreshControlPlaneService('spark')
  } catch (error) {
    service.error = formatApiError(error, 'Failed to restart Spark queries.')
  }
}

function connectAlerts() {
  alertsConnection.value?.disconnect?.()
  alertConnectionError.value = ''

  alertsConnection.value = createAlertsConnection({
    onAlert: (alert) => {
      addAlert(alert)
    },
    onStateChange: (state) => {
      alertConnectionState.value = state
    },
    onError: (message) => {
      alertConnectionError.value = message
    },
  })

  alertsConnection.value.connect()
}

function toggleNotifications() {
  notificationsOpen.value = !notificationsOpen.value
}

function clearNotifications() {
  alerts.value = []
}

function closeNotifications() {
  notificationsOpen.value = false
}

onMounted(() => {
  loadHealth()
  refreshControlPlaneService('data-processing')
  refreshControlPlaneService('spark')
  connectAlerts()
})

onBeforeUnmount(() => {
  alertsConnection.value?.disconnect?.()
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
          to="/info"
          class="rounded-lg border px-4 py-2 transition-colors"
          :class="$route.path.startsWith('/info') ? 'border-teal bg-emerald-50 text-ink' : 'border-slate-300 bg-white text-slate-600 hover:border-slate-400'"
        >
          Info
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
          @close="closeNotifications"
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

    <RouterView
      :services="controlPlaneServices"
      :health="health"
      :health-loading="healthLoading"
      :health-error="healthError"
      :alerts="alerts"
      :alert-connection-state="alertConnectionState"
      :alert-connection-error="alertConnectionError"
      @refresh-service="refreshControlPlaneService"
      @run-all-action="runAllServicesAction"
      @start-query="startSparkQuery"
      @stop-query="stopSparkQuery"
      @restart-queries="restartSparkQueries"
      @reconnect-alerts="connectAlerts"
    />
  </main>
</template>
