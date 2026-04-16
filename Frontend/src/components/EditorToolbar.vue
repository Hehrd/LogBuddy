<script setup>
defineProps({
  editorMode: {
    type: String,
    required: true,
  },
  linuxConfigPathAvailable: {
    type: Boolean,
    required: true,
  },
})

defineEmits([
  'set-mode',
  'import-file',
  'load-template',
  'download',
  'format',
  'minify',
  'reset',
])
</script>

<template>
  <div class="flex w-full flex-wrap gap-2 xl:w-auto xl:shrink-0 xl:justify-end">
    <div class="inline-flex rounded-lg border border-slate-300 bg-slate-50 p-1">
      <button
        type="button"
        class="rounded-md px-3 py-1.5 text-sm font-medium transition"
        :class="editorMode === 'form' ? 'bg-white text-ink shadow-sm' : 'text-slate-500'"
        @click="$emit('set-mode', 'form')"
      >
        Form
      </button>
      <button
        type="button"
        class="rounded-md px-3 py-1.5 text-sm font-medium transition"
        :class="editorMode === 'raw' ? 'bg-white text-ink shadow-sm' : 'text-slate-500'"
        @click="$emit('set-mode', 'raw')"
      >
        Raw
      </button>
    </div>

    <label class="inline-flex min-h-10 cursor-pointer items-center justify-center rounded-lg border border-slate-300 bg-white px-4 text-sm font-medium text-ink transition-colors hover:border-slate-400">
      <input type="file" accept=".json,.conf,application/json" class="hidden" @change="$emit('import-file', $event)" />
      <span>Import file</span>
    </label>
    <button
      type="button"
      class="inline-flex min-h-10 items-center justify-center rounded-lg border border-amber-300 bg-amber-50 px-4 text-sm font-medium text-amber-900 transition-colors hover:border-amber-400"
      @click="$emit('load-template')"
    >
      Load template
    </button>
    <button
      type="button"
      class="inline-flex min-h-10 items-center justify-center rounded-lg border border-teal bg-teal px-4 text-sm font-medium text-white transition-colors hover:opacity-90"
      @click="$emit('download')"
    >
      {{ linuxConfigPathAvailable ? 'Save all to Linux config' : 'Download all to PC' }}
    </button>
    <button
      type="button"
      class="inline-flex min-h-10 items-center justify-center rounded-lg border border-slate-300 bg-white px-4 text-sm font-medium text-ink transition-colors hover:border-slate-400"
      @click="$emit('format')"
    >
      Format
    </button>
    <button
      type="button"
      class="inline-flex min-h-10 items-center justify-center rounded-lg border border-slate-300 bg-white px-4 text-sm font-medium text-ink transition-colors hover:border-slate-400"
      @click="$emit('minify')"
    >
      Minify
    </button>
    <button
      type="button"
      class="inline-flex min-h-10 items-center justify-center rounded-lg border border-rose-300 bg-rose-50 px-4 text-sm font-medium text-rose-700 transition-colors hover:border-rose-400"
      @click="$emit('reset')"
    >
      Reset
    </button>
  </div>
</template>
