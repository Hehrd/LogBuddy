<script setup>
defineProps({
  configs: {
    type: Array,
    required: true,
  },
  activeConfig: {
    type: String,
    required: true,
  },
})

defineEmits(['select'])
</script>

<template>
  <div class="grid gap-3 md:grid-cols-3" aria-label="Config status overview">
    <button
      v-for="config in configs"
      :key="config.key"
      type="button"
      class="grid gap-1 rounded-lg border bg-white p-4"
      :class="[
        config.key === activeConfig ? 'border-teal ring-1 ring-teal' : 'border-slate-200',
        !config.valid ? 'border-rose-300' : '',
      ]"
      @click="$emit('select', config.key)"
    >
      <span class="text-sm font-semibold text-ink">{{ config.label }}</span>
      <strong class="text-3xl font-semibold">{{ config.itemCount }}</strong>
      <span class="text-sm text-slate-500">{{ config.valid ? 'Valid JSON' : 'Needs fixes' }}</span>
    </button>
  </div>
</template>
