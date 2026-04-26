<script setup>
import { computed, onMounted, ref } from 'vue'
import AppHeader from '../components/AppHeader.vue'
import JsonTree from '../components/JsonTree.vue'
import { fetchControlPlaneDataSources } from '../services/backendApi.js'
import { formatApiError } from '../services/httpClient.js'

const dataSources = ref({})
const loading = ref(true)
const error = ref('')

const entries = computed(() => Object.entries(dataSources.value ?? {}))

async function loadDataSources() {
  loading.value = true
  error.value = ''

  try {
    dataSources.value = await fetchControlPlaneDataSources()
  } catch (requestError) {
    error.value = formatApiError(requestError, 'Failed to load datasources.')
  } finally {
    loading.value = false
  }
}

function alertConditionCount(source) {
  return Object.keys(source.globalAlertConditions ?? {}).length +
    Object.keys(source.traceAlertConditions ?? {}).length
}

function ruleCount(source) {
  return (source.globalRequiredRules?.length ?? 0) + (source.traceRequiredRules?.length ?? 0)
}

onMounted(loadDataSources)
</script>

<template>
  <section class="grid gap-6">
    <AppHeader
      title="Datasources"
      intro="Configured log inputs, parser settings, alert conditions, and schedules."
    />

    <section class="rounded-lg border border-slate-200 bg-white p-5">
      <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 class="text-2xl font-semibold">Datasource overview</h2>
          <p class="mt-1 text-slate-500">{{ entries.length }} configured datasource{{ entries.length === 1 ? '' : 's' }}.</p>
        </div>
        <button
          type="button"
          class="inline-flex min-h-10 items-center justify-center rounded-lg border border-slate-300 bg-white px-4 text-sm font-medium text-ink transition-colors hover:border-slate-400"
          @click="loadDataSources"
        >
          Refresh
        </button>
      </div>

      <p v-if="error" class="mt-4 rounded-lg border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700">
        {{ error }}
      </p>

      <div v-if="loading" class="mt-5 rounded-lg border border-dashed border-slate-300 bg-slate-50 p-8 text-center text-slate-500">
        Loading datasources...
      </div>

      <div v-else-if="entries.length" class="mt-5 grid gap-4">
        <article
          v-for="[key, source] in entries"
          :key="key"
          class="rounded-lg border border-slate-200 bg-slate-50 p-4"
        >
          <div class="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <p class="text-sm font-bold uppercase tracking-normal text-teal">{{ source.pathInfo?.platform ?? 'Unknown platform' }}</p>
              <h3 class="mt-1 text-xl font-semibold">{{ source.name ?? key }}</h3>
              <p class="mt-1 text-sm text-slate-500">{{ source.pathInfo?.location ?? 'No location configured' }}</p>
            </div>
            <div class="flex flex-wrap gap-2">
              <span class="rounded-full bg-sky-100 px-3 py-1 text-sm font-semibold text-sky-800">
                {{ source.logFormat?.logType ?? 'Unknown format' }}
              </span>
              <span class="rounded-full bg-emerald-100 px-3 py-1 text-sm font-semibold text-emerald-800">
                {{ ruleCount(source) }} rules
              </span>
              <span class="rounded-full bg-rose-100 px-3 py-1 text-sm font-semibold text-rose-700">
                {{ alertConditionCount(source) }} alerts
              </span>
            </div>
          </div>

          <JsonTree
            :value="source"
            default-mode="branches"
            max-height-class="max-h-96"
            empty-label="No datasource detail"
          />
        </article>
      </div>

      <div v-else class="mt-5 rounded-lg border border-dashed border-slate-300 bg-slate-50 p-8 text-center">
        <h3 class="text-lg font-semibold">No datasources configured</h3>
        <p class="mt-2 text-slate-500">The control panel did not return any datasource entries.</p>
      </div>
    </section>
  </section>
</template>
