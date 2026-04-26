<script setup>
import { computed, onMounted, ref } from 'vue'
import AppHeader from '../components/AppHeader.vue'
import JsonTree from '../components/JsonTree.vue'
import { fetchControlPlaneRules } from '../services/backendApi.js'
import { formatApiError } from '../services/httpClient.js'

const rules = ref({})
const loading = ref(true)
const error = ref('')

const entries = computed(() => Object.entries(rules.value ?? {}))

async function loadRules() {
  loading.value = true
  error.value = ''

  try {
    rules.value = await fetchControlPlaneRules()
  } catch (requestError) {
    error.value = formatApiError(requestError, 'Failed to load rules.')
  } finally {
    loading.value = false
  }
}

function checkTypes(rule) {
  return (rule.checks ?? [])
    .map((check) => check.type)
    .filter(Boolean)
}

onMounted(loadRules)
</script>

<template>
  <section class="grid gap-6">
    <AppHeader
      title="Rules"
      intro="Configured checks, target counts, and alert completion limits."
    />

    <section class="rounded-lg border border-slate-200 bg-white p-5">
      <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 class="text-2xl font-semibold">Rule overview</h2>
          <p class="mt-1 text-slate-500">{{ entries.length }} configured rule{{ entries.length === 1 ? '' : 's' }}.</p>
        </div>
        <button
          type="button"
          class="inline-flex min-h-10 items-center justify-center rounded-lg border border-slate-300 bg-white px-4 text-sm font-medium text-ink transition-colors hover:border-slate-400"
          @click="loadRules"
        >
          Refresh
        </button>
      </div>

      <p v-if="error" class="mt-4 rounded-lg border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700">
        {{ error }}
      </p>

      <div v-if="loading" class="mt-5 rounded-lg border border-dashed border-slate-300 bg-slate-50 p-8 text-center text-slate-500">
        Loading rules...
      </div>

      <div v-else-if="entries.length" class="mt-5 grid gap-4">
        <article
          v-for="[key, rule] in entries"
          :key="key"
          class="rounded-lg border border-slate-200 bg-slate-50 p-4"
        >
          <div class="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <p class="text-sm font-bold uppercase tracking-normal text-teal">Rule</p>
              <h3 class="mt-1 text-xl font-semibold">{{ rule.ruleName ?? key }}</h3>
              <p class="mt-1 text-sm text-slate-500">
                Target {{ rule.logTargetCount ?? 0 }}, max completions {{ rule.maxCompletionsPerAlert ?? 0 }}
              </p>
            </div>
            <div class="flex flex-wrap gap-2">
              <span
                v-for="type in checkTypes(rule)"
                :key="type"
                class="rounded-full bg-sky-100 px-3 py-1 text-sm font-semibold text-sky-800"
              >
                {{ type }}
              </span>
              <span
                v-if="!checkTypes(rule).length"
                class="rounded-full bg-slate-100 px-3 py-1 text-sm font-semibold text-slate-600"
              >
                No checks
              </span>
            </div>
          </div>

          <JsonTree
            :value="rule"
            default-mode="branches"
            max-height-class="max-h-96"
            empty-label="No rule detail"
          />
        </article>
      </div>

      <div v-else class="mt-5 rounded-lg border border-dashed border-slate-300 bg-slate-50 p-8 text-center">
        <h3 class="text-lg font-semibold">No rules configured</h3>
        <p class="mt-2 text-slate-500">The control panel did not return any rule entries.</p>
      </div>
    </section>
  </section>
</template>
