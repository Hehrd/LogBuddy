<script setup>
defineProps({
  configMeta: {
    type: Object,
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
  <aside class="grid content-start gap-3 md:grid-cols-3 xl:grid-cols-1" aria-label="Config types">
    <button
      v-for="(meta, key) in configMeta"
      :key="key"
      type="button"
      class="rounded-lg border bg-white p-4 text-left transition-colors"
      :class="key === activeConfig ? 'border-teal bg-emerald-50' : 'border-slate-200 hover:border-slate-400'"
      @click="$emit('select', key)"
    >
      <span class="block font-semibold">{{ meta.label }}</span>
      <small class="mt-1.5 block text-sm leading-6 text-slate-500">{{ meta.description }}</small>
    </button>
  </aside>
</template>
