<script setup>
defineProps({
  label: {
    type: String,
    required: true,
  },
  help: {
    type: String,
    required: true,
  },
})
</script>

<template>
  <span class="flex items-center gap-2 font-medium text-slate-700">
    <span>{{ label }}</span>
    <span class="group relative inline-flex">
      <span class="grid h-5 w-5 cursor-help place-items-center rounded-full border border-slate-300 bg-white text-xs font-bold text-slate-500">
        i
      </span>
      <span class="pointer-events-none absolute bottom-full left-1/2 z-20 mb-2 hidden w-64 -translate-x-1/2 rounded-lg border border-slate-200 bg-slate-950 px-3 py-2 text-xs font-normal leading-5 text-white shadow-lg group-hover:block">
        {{ help }}
      </span>
    </span>
  </span>
</template>
