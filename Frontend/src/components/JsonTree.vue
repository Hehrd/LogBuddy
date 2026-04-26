<script setup>
import { computed, ref, watch } from 'vue'
import JsonTreeNode from './JsonTreeNode.vue'

const props = defineProps({
  value: {
    type: null,
    default: null,
  },
  emptyLabel: {
    type: String,
    default: 'No data available',
  },
  defaultMode: {
    type: String,
    default: 'branches',
    validator: (value) => ['none', 'branches', 'all'].includes(value),
  },
  maxHeightClass: {
    type: String,
    default: 'max-h-[720px]',
  },
})

const expandedPaths = ref(new Set())

const hasData = computed(() => props.value !== null && props.value !== undefined && props.value !== '')

function isContainer(value) {
  return value !== null && typeof value === 'object'
}

function childEntries(value) {
  if (Array.isArray(value)) return value.map((item, index) => [String(index), item])
  if (isContainer(value)) return Object.entries(value)
  return []
}

function hasContainerChild(value) {
  return childEntries(value).some(([, child]) => isContainer(child))
}

function collectPaths(value, mode, path = 'root') {
  if (!isContainer(value)) return []

  const paths = []
  const children = childEntries(value)
  const shouldOpen =
    mode === 'all' ||
    path === 'root' ||
    (mode === 'branches' && hasContainerChild(value))

  if (shouldOpen) paths.push(path)

  for (const [key, child] of children) {
    paths.push(...collectPaths(child, mode, `${path}.${key}`))
  }

  return paths
}

function setExpandedMode(mode) {
  if (mode === 'none') {
    expandedPaths.value = new Set()
    return
  }

  expandedPaths.value = new Set(collectPaths(props.value, mode))
}

function togglePath(path) {
  const next = new Set(expandedPaths.value)
  if (next.has(path)) {
    next.delete(path)
  } else {
    next.add(path)
  }
  expandedPaths.value = next
}

watch(
  () => props.value,
  () => setExpandedMode(props.defaultMode),
  { immediate: true },
)
</script>

<template>
  <section class="mt-2 rounded-lg border border-slate-200 bg-white">
    <div class="flex flex-col gap-2 border-b border-slate-200 p-2 sm:flex-row sm:items-center sm:justify-between">
      <span class="text-xs font-semibold uppercase tracking-normal text-slate-500">JSON tree</span>
      <div class="flex flex-wrap gap-1.5">
        <button type="button" class="rounded-md border border-slate-300 bg-white px-2.5 py-1 text-xs font-medium text-slate-700 hover:border-slate-400" @click="setExpandedMode('none')">
          Collapse all
        </button>
        <button type="button" class="rounded-md border border-slate-300 bg-white px-2.5 py-1 text-xs font-medium text-slate-700 hover:border-slate-400" @click="setExpandedMode('branches')">
          Open branches
        </button>
        <button type="button" class="rounded-md border border-slate-300 bg-white px-2.5 py-1 text-xs font-medium text-slate-700 hover:border-slate-400" @click="setExpandedMode('all')">
          Open all
        </button>
      </div>
    </div>

    <div v-if="hasData" class="overflow-auto p-2 font-mono text-xs leading-5 text-slate-800" :class="maxHeightClass">
      <JsonTreeNode
        label="root"
        path="root"
        :value="value"
        :depth="0"
        :expanded-paths="expandedPaths"
        :toggle-path="togglePath"
      />
    </div>

    <p v-else class="p-4 text-sm text-slate-500">{{ emptyLabel }}</p>
  </section>
</template>
