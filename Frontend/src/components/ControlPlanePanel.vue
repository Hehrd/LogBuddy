<script setup>
defineProps({
  services: {
    type: Array,
    required: true,
  },
})

defineEmits([
  'refresh-service',
  'run-action',
  'start-query',
  'stop-query',
  'restart-queries',
])

function formatJson(value) {
  if (!value) return ''

  try {
    return JSON.stringify(value, null, 2)
  } catch {
    return String(value)
  }
}
</script>

<template>
  <section class="grid gap-4">
    <article
      v-for="service in services"
      :key="service.key"
      class="rounded-lg border border-slate-200 bg-white p-5"
    >
      <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <p class="text-sm font-bold uppercase tracking-normal text-teal">{{ service.label }}</p>
          <h2 class="mt-1 text-2xl font-semibold">Lifecycle</h2>
          <p class="mt-1.5 text-sm text-slate-500">
            {{ service.loading ? 'Refreshing control-panel state...' : service.error || 'Lifecycle actions are service-specific. Read views come from shared control-panel endpoints.' }}
          </p>
        </div>

        <div class="flex flex-wrap gap-2">
          <button type="button" class="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-ink hover:border-slate-400" @click="$emit('refresh-service', service.key)">
            Refresh
          </button>
          <button v-for="action in service.actions" :key="action" type="button" class="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-ink hover:border-slate-400" @click="$emit('run-action', service.key, action)">
            {{ action }}
          </button>
        </div>
      </div>

      <div class="mt-4 grid gap-4 xl:grid-cols-2">
        <div class="rounded-lg border border-slate-200 bg-slate-50 p-4">
          <h3 class="text-sm font-semibold text-slate-700">Status</h3>
          <pre class="mt-2 overflow-auto rounded-lg bg-slate-950 p-3 text-xs text-white">{{ formatJson(service.status) || 'No status data' }}</pre>
        </div>

        <div class="rounded-lg border border-slate-200 bg-slate-50 p-4">
          <h3 class="text-sm font-semibold text-slate-700">Backend View</h3>
          <pre class="mt-2 overflow-auto rounded-lg bg-slate-950 p-3 text-xs text-white">{{ formatJson(service.config) || 'No backend view available' }}</pre>
        </div>

        <div class="rounded-lg border border-slate-200 bg-slate-50 p-4">
          <h3 class="text-sm font-semibold text-slate-700">Shared Datasources</h3>
          <pre class="mt-2 overflow-auto rounded-lg bg-slate-950 p-3 text-xs text-white">{{ formatJson(service.dataSources) || 'No datasource data' }}</pre>
        </div>

        <div class="rounded-lg border border-slate-200 bg-slate-50 p-4">
          <h3 class="text-sm font-semibold text-slate-700">Shared Rules</h3>
          <pre class="mt-2 overflow-auto rounded-lg bg-slate-950 p-3 text-xs text-white">{{ formatJson(service.rules) || 'No rule data' }}</pre>
        </div>
      </div>

      <div v-if="service.key === 'data-processing'" class="mt-4">
        <div class="rounded-lg border border-slate-200 bg-slate-50 p-4">
          <h3 class="text-sm font-semibold text-slate-700">Stream Metrics</h3>
          <pre class="mt-2 overflow-auto rounded-lg bg-slate-950 p-3 text-xs text-white">{{ formatJson(service.streamMetrics) || 'No stream metrics' }}</pre>
        </div>
      </div>

      <div v-if="service.key === 'spark'" class="mt-4">
        <div class="rounded-lg border border-slate-200 bg-slate-50 p-4">
          <div class="flex items-center justify-between gap-3">
            <h3 class="text-sm font-semibold text-slate-700">Spark Queries</h3>
            <button type="button" class="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-ink hover:border-slate-400" @click="$emit('restart-queries')">
              Restart queries
            </button>
          </div>
          <div v-if="service.queryNames.length" class="mt-3 grid gap-3">
            <div v-for="queryName in service.queryNames" :key="queryName" class="rounded-lg border border-slate-200 bg-white p-3">
              <div class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
                <strong class="text-sm text-ink">{{ queryName }}</strong>
                <div class="flex gap-2">
                  <button type="button" class="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-ink hover:border-slate-400" @click="$emit('start-query', queryName)">
                    Start
                  </button>
                  <button type="button" class="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-ink hover:border-slate-400" @click="$emit('stop-query', queryName)">
                    Stop
                  </button>
                </div>
              </div>
            </div>
          </div>
          <pre v-else class="mt-2 overflow-auto rounded-lg bg-slate-950 p-3 text-xs text-white">{{ formatJson(service.queries) || 'No query data' }}</pre>
        </div>
      </div>
    </article>
  </section>
</template>
