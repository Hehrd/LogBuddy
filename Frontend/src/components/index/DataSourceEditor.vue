<script setup>
import HelpLabel from '../HelpLabel.vue'

defineProps({
  dsSources: Array,
  activeDsSourceKey: String,
  activeDsSource: Object,
  activeAlerts: Array,
  activeDsAlertKey: String,
  activeDsAlert: Object,
  configuredRules: Array,
  selectedKeys: Object,
  fieldHelp: Object,
  platformOptions: Array,
  logTypeOptions: Array,
})

defineEmits([
  'context-menu',
  'add-source',
  'update-source',
  'add-option',
  'update-option',
  'delete-option',
  'add-alert',
  'update-alert',
  'toggle-alert-rule',
])
</script>

<template>
  <section class="grid gap-4">
    <div class="flex flex-wrap gap-2">
      <button
        v-for="[key, source] in dsSources"
        :key="key"
        type="button"
        class="rounded-lg border px-3 py-2 text-sm font-medium"
        :class="key === activeDsSourceKey ? 'border-teal bg-emerald-50 text-ink' : 'border-slate-300 bg-white text-slate-600'"
        @click="selectedKeys.dsSource = key"
        @contextmenu="$emit('context-menu', $event, 'dataSource', key)"
      >
        {{ source.name || key }}
      </button>
      <button type="button" class="rounded-lg border border-dashed border-slate-300 px-3 py-2 text-sm font-medium text-slate-600" @click="$emit('add-source')">
        Add source
      </button>
    </div>

    <div v-if="activeDsSource" class="grid gap-4 rounded-lg border border-slate-200 bg-slate-50 p-4">
      <div class="grid gap-4 md:grid-cols-2">
        <label class="grid gap-1 text-sm">
          <HelpLabel label="Source name" :help="fieldHelp.sourceName" />
          <input :value="activeDsSource.name" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @input="$emit('update-source', 'name', $event.target.value)" />
        </label>
        <label class="grid gap-1 text-sm">
          <HelpLabel label="Platform" :help="fieldHelp.platform" />
          <select :value="activeDsSource.pathInfo?.platform" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @change="$emit('update-source', 'platform', $event.target.value)">
            <option v-for="option in platformOptions" :key="option" :value="option">{{ option }}</option>
          </select>
        </label>
        <label class="grid gap-1 text-sm md:col-span-2">
          <HelpLabel label="Location" :help="fieldHelp.location" />
          <input :value="activeDsSource.pathInfo?.location" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @input="$emit('update-source', 'location', $event.target.value)" />
        </label>

        <div class="grid gap-2 text-sm md:col-span-2">
          <div class="flex flex-wrap items-center justify-between gap-2">
            <HelpLabel label="Options" :help="fieldHelp.options" />
            <button type="button" class="rounded-lg border border-dashed border-slate-300 px-3 py-2 text-sm font-medium text-slate-600" @click="$emit('add-option')">
              Add option
            </button>
          </div>
          <div class="grid gap-2 rounded-lg border border-slate-200 bg-white p-3">
            <div v-for="[optionKey, optionValue] in Object.entries(activeDsSource.pathInfo?.options ?? {})" :key="optionKey" class="grid gap-2 md:grid-cols-[minmax(0,1fr)_minmax(0,1fr)_auto]">
              <input :value="optionKey" aria-label="Option key" class="rounded-lg border border-slate-300 bg-white px-3 py-2" placeholder="key" @input="$emit('update-option', optionKey, $event.target.value, optionValue)" />
              <input :value="optionValue" aria-label="Option value" class="rounded-lg border border-slate-300 bg-white px-3 py-2" placeholder="value" @input="$emit('update-option', optionKey, optionKey, $event.target.value)" />
              <button type="button" class="rounded-lg border border-rose-300 bg-rose-50 px-3 py-2 text-sm font-medium text-rose-700" @click="$emit('delete-option', optionKey)">Delete</button>
            </div>
            <p v-if="!Object.keys(activeDsSource.pathInfo?.options ?? {}).length" class="text-sm text-slate-500">No connector options yet.</p>
          </div>
        </div>

        <label class="grid gap-1 text-sm">
          <HelpLabel label="Log type" :help="fieldHelp.logType" />
          <select :value="activeDsSource.logFormat?.logType" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @change="$emit('update-source', 'logType', $event.target.value)">
            <option v-for="option in logTypeOptions" :key="option" :value="option">{{ option }}</option>
          </select>
        </label>
        <div class="grid gap-2 text-sm md:col-span-2">
          <HelpLabel label="Required rules" :help="fieldHelp.requiredRules" />
          <div v-if="(activeDsSource.requiredRules ?? []).length" class="flex min-h-12 flex-wrap gap-2 rounded-lg border border-slate-200 bg-white p-3">
            <span v-for="ruleName in activeDsSource.requiredRules" :key="ruleName" class="rounded-full bg-emerald-100 px-3 py-1 text-sm font-medium text-emerald-800">{{ ruleName }}</span>
          </div>
          <p v-else class="min-h-12 rounded-lg border border-dashed border-slate-300 bg-white p-3 text-sm text-slate-500">
            This is filled automatically from the selected rules in this source's alerts.
          </p>
        </div>
        <label class="grid gap-1 text-sm">
          <HelpLabel label="Startup delay" :help="fieldHelp.startupDelay" />
          <input :value="activeDsSource.schedule?.delayAfterStartUpMillis" type="number" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @input="$emit('update-source', 'delayAfterStartUpMillis', $event.target.value)" />
        </label>
      </div>

      <div class="grid gap-3 rounded-lg border border-slate-200 bg-white p-4">
        <div class="flex flex-wrap items-center gap-2">
          <h3 class="text-lg font-semibold">Alerts</h3>
          <button type="button" class="rounded-lg border border-dashed border-slate-300 px-3 py-2 text-sm font-medium text-slate-600" @click="$emit('add-alert')">Add alert</button>
        </div>

        <div class="flex flex-wrap gap-2">
          <button
            v-for="[key, alert] in activeAlerts"
            :key="key"
            type="button"
            class="rounded-lg border px-3 py-2 text-sm font-medium"
            :class="key === activeDsAlertKey ? 'border-teal bg-emerald-50 text-ink' : 'border-slate-300 bg-white text-slate-600'"
            @click="selectedKeys.dsAlert = key"
            @contextmenu="$emit('context-menu', $event, 'alert', key)"
          >
            {{ alert.alertName || key }}
          </button>
        </div>

        <div v-if="activeDsAlert" class="grid gap-4 md:grid-cols-2">
          <label class="grid gap-1 text-sm">
            <HelpLabel label="Alert name" :help="fieldHelp.alertName" />
            <input :value="activeDsAlert.alertName" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @input="$emit('update-alert', 'alertName', $event.target.value)" />
          </label>
          <label class="grid gap-1 text-sm">
            <HelpLabel label="Window millis" :help="fieldHelp.windowMillis" />
            <input :value="activeDsAlert.timeWindowMillis" type="number" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @input="$emit('update-alert', 'timeWindowMillis', $event.target.value)" />
          </label>
          <div class="grid gap-2 text-sm md:col-span-2">
            <HelpLabel label="Required rules" :help="fieldHelp.requiredRules" />
            <div v-if="configuredRules.length" class="grid gap-2 rounded-lg border border-slate-200 bg-white p-3">
              <label v-for="[ruleKey, rule] in configuredRules" :key="ruleKey" class="flex items-center gap-3 text-sm text-slate-700">
                <input :checked="(activeDsAlert.requiredRules ?? []).includes(ruleKey)" type="checkbox" class="h-4 w-4 rounded border-slate-300" @change="$emit('toggle-alert-rule', ruleKey)" />
                <span>{{ rule.ruleName || ruleKey }}</span>
              </label>
            </div>
            <p v-else class="rounded-lg border border-dashed border-slate-300 bg-white p-3 text-sm text-slate-500">Create rules in the Rules tab, then attach them here.</p>
          </div>
          <label class="grid gap-1 text-sm md:col-span-2">
            <HelpLabel label="Alert endpoints" :help="fieldHelp.alertEndpoints" />
            <input :value="(activeDsAlert.alertEndpoints ?? []).join(', ')" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @input="$emit('update-alert', 'alertEndpoints', $event.target.value)" />
          </label>
        </div>
      </div>
    </div>
  </section>
</template>
