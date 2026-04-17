<script setup>
defineProps({
  alerts: {
    type: Array,
    required: true,
  },
  status: {
    type: String,
    required: true,
  },
  usingMocks: {
    type: Boolean,
    required: true,
  },
})

defineEmits(['ping', 'clear'])

const severityClasses = {
  critical: 'border-rose-300 bg-rose-50 text-rose-800',
  warning: 'border-amber-300 bg-amber-50 text-amber-900',
  info: 'border-sky-300 bg-sky-50 text-sky-800',
}

function formatTime(value) {
  return value
    ? new Intl.DateTimeFormat(undefined, {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      }).format(new Date(value))
    : 'Just now'
}

function formatRules(alert) {
  return alert.ruleNames?.length ? alert.ruleNames.join(', ') : 'Unknown rule'
}
</script>

<template>
  <section class="rounded-lg border border-slate-200 bg-white p-5">
    <div class="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
      <div>
        <p class="text-sm font-bold uppercase tracking-normal text-teal">Live alerts</p>
        <h2 class="mt-1 text-2xl font-semibold">Alert stream</h2>
        <p class="mt-1.5 text-sm text-slate-500">
          {{ usingMocks ? 'Development mock WebSocket is streaming sample alerts.' : 'Connected to the configured alert WebSocket.' }}
        </p>
      </div>

      <div class="flex flex-wrap items-center gap-2">
        <span class="rounded-full border border-slate-200 px-3 py-1 text-sm font-semibold capitalize text-slate-700">
          {{ status }}
        </span>
        <button type="button" class="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-ink hover:border-slate-400" @click="$emit('ping')">
          Ping socket
        </button>
        <button type="button" class="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-ink hover:border-slate-400" @click="$emit('clear')">
          Clear
        </button>
      </div>
    </div>

    <div v-if="alerts.length" class="mt-4 max-h-[34rem] overflow-y-auto pr-1">
      <div class="grid gap-3 lg:grid-cols-3">
        <article
          v-for="alert in alerts"
          :key="alert.id"
          class="rounded-lg border p-4"
          :class="severityClasses[alert.severity] ?? severityClasses.info"
        >
          <div class="flex items-start justify-between gap-3">
            <strong class="text-sm uppercase tracking-normal">{{ alert.severity }}</strong>
            <span class="text-xs font-semibold">{{ formatTime(alert.occurredAt) }}</span>
          </div>
          <h3 class="mt-2 font-semibold text-ink">{{ alert.alertName }}</h3>
          <p class="mt-1 text-sm leading-5 text-slate-700">{{ alert.message }}</p>
          <p class="mt-3 text-xs font-semibold text-slate-500">
            {{ alert.sourceName }} / {{ alert.matchedCount }} matches
          </p>
          <p class="mt-1 text-xs font-semibold text-slate-500">
            Rules: {{ formatRules(alert) }}
          </p>
        </article>
      </div>
    </div>

    <p v-else class="mt-4 rounded-lg border border-dashed border-slate-300 bg-slate-50 p-4 text-sm text-slate-500">
      Waiting for alert messages from the socket.
    </p>
  </section>
</template>
