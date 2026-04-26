<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import ControlPlanePanel from '../components/ControlPlanePanel.vue'
import HealthStatusCard from '../components/HealthStatusCard.vue'

const props = defineProps({
  serviceKey: {
    type: String,
    required: true,
  },
  services: {
    type: Array,
    required: true,
  },
  health: {
    type: Object,
    default: null,
  },
  healthLoading: {
    type: Boolean,
    required: true,
  },
  healthError: {
    type: String,
    default: '',
  },
})

defineEmits([
  'refresh-service',
  'run-all-action',
  'start-query',
  'stop-query',
  'restart-queries',
])

const serviceActions = ['sleep', 'wake', 'restart', 'shutdown']

const activeService = computed(() =>
  props.services.find((service) => service.key === props.serviceKey),
)

const visibleServices = computed(() =>
  activeService.value ? [activeService.value] : [],
)
</script>

<template>
  <section class="grid gap-6">
    <HealthStatusCard :health="health" :loading="healthLoading" :error="healthError" />

    <section class="rounded-lg border border-slate-200 bg-white p-4">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div>
          <p class="text-sm font-bold uppercase tracking-normal text-teal">Info</p>
          <h2 class="mt-1 text-2xl font-semibold">Service controls</h2>
          <p class="mt-1.5 text-sm text-slate-500">
            Lifecycle actions apply to Data Processing and Spark Processing. Refresh stays scoped to the selected service.
          </p>
        </div>

        <div class="flex flex-wrap gap-2">
          <button
            v-for="action in serviceActions"
            :key="action"
            type="button"
            class="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-ink hover:border-slate-400"
            @click="$emit('run-all-action', action)"
          >
            {{ action }}
          </button>
        </div>
      </div>

      <div class="mt-4 flex flex-wrap gap-2 border-t border-slate-200 pt-4 text-sm font-semibold">
        <RouterLink
          to="/info/data-processing"
          class="rounded-lg border px-4 py-2 transition-colors"
          :class="serviceKey === 'data-processing' ? 'border-teal bg-emerald-50 text-ink' : 'border-slate-300 bg-white text-slate-600 hover:border-slate-400'"
        >
          Data Processing
        </RouterLink>
        <RouterLink
          to="/info/spark"
          class="rounded-lg border px-4 py-2 transition-colors"
          :class="serviceKey === 'spark' ? 'border-teal bg-emerald-50 text-ink' : 'border-slate-300 bg-white text-slate-600 hover:border-slate-400'"
        >
          Spark Processing
        </RouterLink>
      </div>
    </section>

    <ControlPlanePanel
      v-if="visibleServices.length"
      :services="visibleServices"
      @refresh-service="$emit('refresh-service', $event)"
      @start-query="$emit('start-query', $event)"
      @stop-query="$emit('stop-query', $event)"
      @restart-queries="$emit('restart-queries')"
    />

    <section v-else class="rounded-lg border border-slate-200 bg-white p-8 text-center">
      <h2 class="text-xl font-semibold">Service not found</h2>
      <p class="mt-2 text-slate-500">Choose Data Processing or Spark Processing from the navigation.</p>
    </section>
  </section>
</template>
