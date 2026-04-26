<script setup>
import { computed, ref } from 'vue'

defineOptions({
  name: 'JsonTreeNode',
})

const props = defineProps({
  label: {
    type: String,
    required: true,
  },
  path: {
    type: String,
    required: true,
  },
  value: {
    type: null,
    default: null,
  },
  depth: {
    type: Number,
    required: true,
  },
  expandedPaths: {
    type: Object,
    required: true,
  },
  togglePath: {
    type: Function,
    required: true,
  },
})

const longValueOpen = ref(false)

const isContainer = computed(() => props.value !== null && typeof props.value === 'object')
const isArray = computed(() => Array.isArray(props.value))
const isOpen = computed(() => props.expandedPaths.has(props.path))
const entries = computed(() => {
  if (isArray.value) return props.value.map((item, index) => [String(index), item])
  if (isContainer.value) return Object.entries(props.value)
  return []
})
const typeLabel = computed(() => {
  if (isArray.value) return `array[${entries.value.length}]`
  if (isContainer.value) return `object{${entries.value.length}}`
  if (props.value === null) return 'null'
  return typeof props.value
})
const stringValue = computed(() => {
  if (typeof props.value === 'string') return props.value
  if (props.value === null) return 'null'
  return String(props.value)
})
const isLongString = computed(() => typeof props.value === 'string' && props.value.length > 56)
const previewValue = computed(() => {
  if (!isLongString.value) return stringValue.value
  return `${props.value.slice(0, 56)}...`
})
const valueClass = computed(() => {
  if (typeof props.value === 'string') return 'border-sky-200 bg-sky-50 text-sky-900'
  if (typeof props.value === 'number') return 'border-violet-200 bg-violet-50 text-violet-800'
  if (typeof props.value === 'boolean') return 'border-emerald-200 bg-emerald-50 text-emerald-800'
  if (props.value === null) return 'border-slate-200 bg-slate-100 text-slate-500'
  return 'border-slate-200 bg-white text-slate-700'
})
</script>

<template>
  <div>
    <div
      class="group flex min-w-max items-start gap-2 rounded-md px-2 py-1 hover:bg-slate-50"
      :style="{ paddingLeft: `${depth * 18 + 8}px` }"
    >
      <button
        v-if="isContainer"
        type="button"
        class="mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded border border-slate-300 bg-white text-[10px] font-bold text-slate-600 hover:border-slate-400"
        :aria-label="isOpen ? `Collapse ${label}` : `Expand ${label}`"
        @click="togglePath(path)"
      >
        {{ isOpen ? '-' : '+' }}
      </button>
      <span v-else class="h-5 w-5 shrink-0" />

      <div class="min-w-0 flex-1">
        <div class="flex flex-wrap items-center gap-2">
          <span class="font-semibold text-slate-900">{{ label }}</span>
          <span class="rounded-full border border-slate-200 bg-slate-50 px-2 py-0.5 text-[11px] font-semibold text-slate-500">
            {{ typeLabel }}
          </span>

          <template v-if="!isContainer">
            <button
              v-if="isLongString"
              type="button"
              class="max-w-[32rem] truncate rounded-md border px-2 py-0.5 text-left"
              :class="valueClass"
              @click="longValueOpen = !longValueOpen"
            >
              {{ longValueOpen ? 'collapse value' : previewValue }}
            </button>
            <span
              v-else
              class="max-w-[32rem] rounded-md border px-2 py-0.5"
              :class="valueClass"
            >
              {{ previewValue }}
            </span>
          </template>
        </div>

        <div
          v-if="isLongString && longValueOpen"
          class="mt-2 max-w-[48rem] whitespace-pre-wrap break-words rounded-md border border-sky-200 bg-sky-50 p-2 text-sky-950"
        >
          {{ stringValue }}
        </div>
      </div>
    </div>

    <div v-if="isContainer && isOpen">
      <JsonTreeNode
        v-for="[childKey, childValue] in entries"
        :key="`${path}.${childKey}`"
        :label="childKey"
        :path="`${path}.${childKey}`"
        :value="childValue"
        :depth="depth + 1"
        :expanded-paths="expandedPaths"
        :toggle-path="togglePath"
      />
    </div>
  </div>
</template>
