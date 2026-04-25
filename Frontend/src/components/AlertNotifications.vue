<script setup>
defineProps({
  alerts: {
    type: Array,
    required: true,
  },
  open: {
    type: Boolean,
    required: true,
  },
})

defineEmits(['toggle', 'clear'])

const ruleClasses = {
  critical: 'bg-rose-100 text-rose-700 border-rose-200',
  warning: 'bg-amber-100 text-amber-800 border-amber-200',
  info: 'bg-sky-100 text-sky-700 border-sky-200',
}

const severityClasses = {
  critical: 'bg-rose-100 text-rose-700',
  warning: 'bg-amber-100 text-amber-800',
  info: 'bg-sky-100 text-sky-700',
}

function getRuleNames(alert) {
  return alert.ruleNames?.length ? alert.ruleNames : ['Unknown rule']
}
</script>

<template>
  <div class="relative">
    <button
      type="button"
      class="relative rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-ink hover:border-slate-400"
      @click="$emit('toggle')"
    >
      Notifications
      <span v-if="alerts.length" class="ml-2 rounded-full bg-rose-600 px-2 py-0.5 text-xs text-white">
        {{ alerts.length }}
      </span>
    </button>

    <section
      v-if="open"
      class="absolute right-0 z-50 mt-2 w-[min(28rem,calc(100vw-2rem))] rounded-lg border border-slate-200 bg-white p-3 shadow-xl"
    >
      <div class="mb-3 flex items-center justify-between gap-3">
        <h2 class="font-semibold">Alert notifications</h2>
        <button type="button" class="text-sm font-semibold text-slate-500 hover:text-ink" @click="$emit('clear')">
          Clear
        </button>
      </div>

      <div v-if="alerts.length" class="max-h-96 overflow-y-auto pr-1">
        <article v-for="alert in alerts" :key="alert.id" class="mb-2 rounded-lg border border-slate-200 bg-slate-50 p-3 last:mb-0">
          <div class="flex items-start justify-between gap-3">
            <strong class="text-sm text-ink">{{ alert.alertName }}</strong>
            <span
              class="rounded-full px-2 py-0.5 text-xs font-semibold"
              :class="severityClasses[alert.severity] ?? severityClasses.info"
            >
              {{ alert.severity }}
            </span>
          </div>
          <div class="mt-2 grid gap-1 text-xs text-slate-500">
            <p><span class="font-semibold text-slate-700">Id:</span> {{ alert.id }}</p>
            <p><span class="font-semibold text-slate-700">Source:</span> {{ alert.sourceName ?? 'Unknown' }}</p>
            <p><span class="font-semibold text-slate-700">Type:</span> {{ alert.alertType ?? 'Unknown' }}</p>
            <p><span class="font-semibold text-slate-700">Triggered:</span> {{ alert.occurredAt ?? 'Unknown' }}</p>
            <p v-if="alert.traceId"><span class="font-semibold text-slate-700">Trace:</span> {{ alert.traceId }}</p>
            <p v-if="alert.timeWindowMillis"><span class="font-semibold text-slate-700">Window:</span> {{ alert.timeWindowMillis }} ms</p>
            <p><span class="font-semibold text-slate-700">AI overview:</span> {{ alert.aiOverviewEnabled ? 'enabled' : 'disabled' }}</p>
          </div>
          <p class="mt-1 text-sm text-slate-600">{{ alert.message }}</p>
          <div class="mt-2 flex flex-wrap gap-1.5">
            <span
              v-for="ruleName in getRuleNames(alert)"
              :key="ruleName"
              class="rounded-full border px-2 py-0.5 text-xs font-semibold"
              :class="ruleClasses[alert.severity] ?? ruleClasses.info"
            >
              {{ ruleName }}
            </span>
          </div>
          <div v-if="alert.firstMatchedAt || alert.lastMatchedAt" class="mt-2 grid gap-1 text-xs text-slate-500">
            <p v-if="alert.firstMatchedAt"><span class="font-semibold text-slate-700">First matched:</span> {{ alert.firstMatchedAt }}</p>
            <p v-if="alert.lastMatchedAt"><span class="font-semibold text-slate-700">Last matched:</span> {{ alert.lastMatchedAt }}</p>
          </div>
          <pre v-if="alert.sampleLogs?.length" class="mt-2 overflow-auto rounded-lg bg-slate-950 p-3 text-xs text-white">{{ alert.sampleLogs }}</pre>
        </article>
      </div>

      <p v-else class="rounded-lg border border-dashed border-slate-300 bg-slate-50 p-4 text-sm text-slate-500">
        No alert notifications yet.
      </p>
    </section>
  </div>
</template>
