<script setup>
defineProps({
  health: {
    type: Object,
    default: null,
  },
  loading: {
    type: Boolean,
    required: true,
  },
  error: {
    type: String,
    default: '',
  },
})
</script>

<template>
  <section class="rounded-lg border border-slate-200 bg-white p-4">
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <p class="text-sm font-bold uppercase tracking-normal text-teal">Backend health</p>
        <p class="mt-1 text-sm text-slate-500">
          {{ loading ? 'Checking /health...' : error || `${health?.service ?? 'Backend'} is ${health?.status ?? 'unknown'}` }}
        </p>
      </div>
      <span
        class="w-fit rounded-full px-3 py-1 text-sm font-semibold"
        :class="health?.status === 'UP' ? 'bg-emerald-100 text-emerald-800' : 'bg-amber-100 text-amber-900'"
      >
        {{ loading ? 'checking' : health?.status ?? 'unknown' }}
      </span>
    </div>
  </section>
</template>
