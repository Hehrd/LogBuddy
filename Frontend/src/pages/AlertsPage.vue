<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '../components/AppHeader.vue'
import JsonTree from '../components/JsonTree.vue'

const props = defineProps({
  alerts: {
    type: Array,
    required: true,
  },
  alertConnectionState: {
    type: String,
    required: true,
  },
  alertConnectionError: {
    type: String,
    default: '',
  },
})

defineEmits(['reconnect-alerts'])

const route = useRoute()
const alertsPerPage = 10
const currentPage = ref(1)

const latestAlertTime = computed(() => {
  if (!props.alerts.length) return 'No alerts yet'
  return formatDate(props.alerts[0].triggeredAt ?? props.alerts[0].occurredAt ?? props.alerts[0].timestamp)
})

const totalPages = computed(() => Math.max(1, Math.ceil(props.alerts.length / alertsPerPage)))
const pageStart = computed(() => (currentPage.value - 1) * alertsPerPage)
const pageEnd = computed(() => Math.min(pageStart.value + alertsPerPage, props.alerts.length))
const visibleAlerts = computed(() => props.alerts.slice(pageStart.value, pageEnd.value))

function formatDate(value) {
  if (!value) return 'Unknown time'
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'medium',
  }).format(new Date(value))
}

function alertDetailPayload(alert) {
  return alert.raw ?? alert
}

function getAlertId(alert) {
  return alert.alertId ?? alert.id
}

function isTargetAlert(alert) {
  return route.query.alert && getAlertId(alert) === route.query.alert
}

function setCurrentPage(page) {
  currentPage.value = Math.min(Math.max(page, 1), totalPages.value)
}

function setPageForAlert(alertId) {
  const alertIndex = props.alerts.findIndex((alert) => getAlertId(alert) === alertId)
  if (alertIndex === -1) return

  setCurrentPage(Math.floor(alertIndex / alertsPerPage) + 1)
}

async function scrollToTargetAlert() {
  const targetId = route.query.alert
  if (!targetId) return

  setPageForAlert(targetId)
  await nextTick()
  document.getElementById(`alert-${targetId}`)?.scrollIntoView({
    behavior: 'smooth',
    block: 'center',
  })
}

onMounted(scrollToTargetAlert)

watch(() => route.query.alert, scrollToTargetAlert)
watch(totalPages, () => {
  if (currentPage.value > totalPages.value) setCurrentPage(totalPages.value)
})
</script>

<template>
  <section class="grid gap-6">
    <AppHeader
      title="Alerts"
      intro="Live alert activity and recent alert payloads."
    />

    <section class="grid gap-4 md:grid-cols-3">
      <article class="rounded-lg border border-slate-200 bg-white p-4">
        <span class="text-sm font-semibold text-slate-500">Connection</span>
        <p class="mt-2 text-2xl font-semibold">{{ alertConnectionState }}</p>
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
          @click="$emit('reconnect-alerts')"
        >
          Reconnect
        </button>
      </div>

      <p v-if="alertConnectionError" class="mt-4 rounded-lg border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700">
        {{ alertConnectionError }}
      </p>

      <div v-if="alerts.length" class="mt-5 max-h-[calc(100vh-22rem)] overflow-y-auto pr-2">
        <div class="grid gap-3">
        <article
          v-for="(alert, index) in visibleAlerts"
          :key="`${alert.alertId ?? alert.id ?? alert.triggeredAt ?? index}`"
          :id="getAlertId(alert) ? `alert-${getAlertId(alert)}` : undefined"
          class="rounded-lg border bg-slate-50 p-4 transition-colors"
          :class="isTargetAlert(alert) ? 'border-teal ring-2 ring-emerald-100' : 'border-slate-200'"
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

          <div class="mt-4">
            <p class="text-sm font-semibold text-ink">Alert payload</p>
            <JsonTree
              :value="alertDetailPayload(alert)"
              default-mode="branches"
              max-height-class="max-h-96"
              empty-label="No alert payload"
            />
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
              <JsonTree
                :value="completion?.logs ?? completion"
                default-mode="none"
                max-height-class="max-h-72"
                empty-label="No completion detail"
              />
            </div>
          </div>

          <div v-if="alert.sampleLogs?.length" class="mt-4">
            <p class="text-sm font-semibold text-ink">Sample logs</p>
            <JsonTree
              :value="alert.sampleLogs"
              default-mode="none"
              max-height-class="max-h-72"
              empty-label="No sample logs"
            />
          </div>
        </article>
        </div>
      </div>

      <div
        v-if="alerts.length > alertsPerPage"
        class="mt-4 flex flex-col gap-3 border-t border-slate-200 pt-4 sm:flex-row sm:items-center sm:justify-between"
      >
        <p class="text-sm text-slate-500">
          Showing {{ pageStart + 1 }}-{{ pageEnd }} of {{ alerts.length }} alerts
        </p>
        <div class="flex flex-wrap gap-2">
          <button
            type="button"
            class="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-ink hover:border-slate-400 disabled:cursor-not-allowed disabled:opacity-50"
            :disabled="currentPage === 1"
            @click="setCurrentPage(currentPage - 1)"
          >
            Previous
          </button>
          <button
            v-for="page in totalPages"
            :key="page"
            type="button"
            class="min-w-10 rounded-lg border px-3 py-2 text-sm font-semibold transition-colors"
            :class="page === currentPage ? 'border-teal bg-emerald-50 text-ink' : 'border-slate-300 bg-white text-slate-600 hover:border-slate-400'"
            @click="setCurrentPage(page)"
          >
            {{ page }}
          </button>
          <button
            type="button"
            class="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-ink hover:border-slate-400 disabled:cursor-not-allowed disabled:opacity-50"
            :disabled="currentPage === totalPages"
            @click="setCurrentPage(currentPage + 1)"
          >
            Next
          </button>
        </div>
      </div>

      <div v-if="!alerts.length" class="mt-5 rounded-lg border border-dashed border-slate-300 bg-slate-50 p-8 text-center">
        <h3 class="text-lg font-semibold">No alerts yet</h3>
        <p class="mt-2 text-slate-500">When the backend publishes to the websocket, alerts will show up here.</p>
      </div>
    </section>
  </section>
</template>
