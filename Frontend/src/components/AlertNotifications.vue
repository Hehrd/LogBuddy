<script setup>
import { RouterLink } from 'vue-router'

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

defineEmits(['toggle', 'clear', 'close'])

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

function getAlertId(alert) {
  return alert.alertId ?? alert.id
}
</script>

<template>
  <div class="relative">
    <button
      type="button"
      class="relative inline-flex h-10 w-10 items-center justify-center rounded-lg border border-slate-300 bg-white text-ink hover:border-slate-400"
      aria-label="Alert notifications"
      @click="$emit('toggle')"
    >
      <svg viewBox="0 0 90 90" class="h-5 w-5" aria-hidden="true">
        <path fill="currentColor" d="M76.662 77.645H13.337c-3.975 0-7.209-3.234-7.209-7.209 0-4.127 1.8-8.031 4.938-10.711 1.355-1.158 2.133-2.845 2.133-4.628V31.801C13.199 14.266 27.465 0 45 0s31.801 14.266 31.801 31.801v23.296c0 1.783.777 3.47 2.134 4.629 3.137 2.68 4.937 6.583 4.937 10.71 0 3.974-3.235 7.209-7.21 7.209ZM14.18 69.645h61.64c-.193-1.484-.928-2.852-2.081-3.837-3.139-2.682-4.938-6.585-4.938-10.711V31.801C68.801 18.677 58.124 8 45 8S21.199 18.677 21.199 31.801v23.296c0 4.126-1.799 8.029-4.937 10.711-1.154.985-1.888 2.352-2.082 3.837Z"/>
        <path fill="currentColor" d="M45 90c-8.975 0-16.277-7.302-16.277-16.276 0-.072.003-.165.007-.249.089-2.13 1.844-3.83 3.997-3.83 2.209 0 4 1.791 4 4 0 .048-.001.104-.003.162C36.768 78.332 40.464 82 45 82c4.534 0 8.229-3.666 8.276-8.189-.001-.007-.001-.013-.001-.018-.082-2.208 1.642-4.063 3.849-4.146 2.196-.087 4.063 1.641 4.146 3.849.006.09.007.138.007.228C61.276 82.698 53.975 90 45 90Z"/>
      </svg>
      <span v-if="alerts.length" class="absolute -right-1 -top-1 rounded-full bg-rose-600 px-1.5 py-0.5 text-[10px] font-semibold leading-none text-white">
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
          <div class="mt-2 grid gap-1 text-xs text-slate-500 sm:grid-cols-2">
            <p v-if="getAlertId(alert)" class="sm:col-span-2"><span class="font-semibold text-slate-700">Id:</span> {{ getAlertId(alert) }}</p>
            <p><span class="font-semibold text-slate-700">Source:</span> {{ alert.sourceName ?? alert.dataSourceName ?? 'Unknown' }}</p>
            <p><span class="font-semibold text-slate-700">Rules:</span> {{ alert.completions?.length ?? alert.data?.length ?? getRuleNames(alert).length }}</p>
            <p class="sm:col-span-2"><span class="font-semibold text-slate-700">Triggered:</span> {{ alert.occurredAt ?? alert.triggeredAt ?? 'Unknown' }}</p>
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
          <RouterLink
            v-if="getAlertId(alert)"
            :to="{ path: '/alerts', query: { alert: getAlertId(alert) } }"
            class="mt-3 inline-flex min-h-9 items-center justify-center rounded-lg border border-teal bg-teal px-3 text-sm font-semibold text-white hover:opacity-90"
            @click="$emit('close')"
          >
            View in Alerts
          </RouterLink>
        </article>
      </div>

      <p v-else class="rounded-lg border border-dashed border-slate-300 bg-slate-50 p-4 text-sm text-slate-500">
        No alert notifications yet.
      </p>
    </section>
  </div>
</template>
