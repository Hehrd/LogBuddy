<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink, RouterView } from 'vue-router'
import AlertNotifications from './components/AlertNotifications.vue'
import ControlPlanePanel from './components/ControlPlanePanel.vue'
import HealthStatusCard from './components/HealthStatusCard.vue'
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

const notificationSocket = ref(null)
const notificationAlerts = ref([])
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
    actions: ['sleep', 'wake', 'restart', 'shutdown'],
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
    actions: ['sleep', 'wake', 'restart', 'shutdown'],
    queries: null,
    queryNames: [],
    streamMetrics: null,
  },
])

const alertsMode = computed(() => {
  const mode = import.meta.env.VITE_ALERTS_MODE?.toLowerCase()
  if (mode === 'mock' || mode === 'real') {
    return mode
  }
  return import.meta.env.DEV ? 'mock' : 'real'
})

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

async function runServiceAction(serviceKey, action) {
  const service = getServiceState(serviceKey)
  if (!service) return

  service.error = ''

  try {
    await runControlPlaneAction(serviceKey, action)
    await refreshControlPlaneService(serviceKey)
    await loadHealth()
  } catch (error) {
    service.error = formatApiError(error, `Failed to run ${action} on ${service.label}.`)
  }
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

function connectNotificationSocket() {
  notificationSocket.value?.disconnect?.()

  notificationSocket.value = createAlertsConnection({
    onAlert: (alert) => {
      notificationAlerts.value = [
        normalizeAlert(alert),
        ...notificationAlerts.value,
      ]
    },
    onStateChange: () => {},
    onError: () => {},
  })

  notificationSocket.value.connect()
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
  refreshControlPlaneService('data-processing')
  refreshControlPlaneService('spark')
  connectNotificationSocket()
})

onBeforeUnmount(() => {
  notificationSocket.value?.disconnect?.()
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

    <ControlPlanePanel
      :services="controlPlaneServices"
      class="mb-6"
      @refresh-service="refreshControlPlaneService"
      @run-action="runServiceAction"
      @start-query="startSparkQuery"
      @stop-query="stopSparkQuery"
      @restart-queries="restartSparkQueries"
    />

    <RouterView />
  </main>
</template>
