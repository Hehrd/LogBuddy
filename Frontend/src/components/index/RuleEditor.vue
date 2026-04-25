<script setup>
import HelpLabel from '../HelpLabel.vue'

defineProps({
  rulesList: Array,
  activeRuleKey: String,
  activeRule: Object,
  selectedKeys: Object,
  fieldHelp: Object,
  checkTypeOptions: Array,
})

defineEmits(['context-menu', 'add-rule', 'update-rule'])
</script>

<template>
  <section class="grid gap-4">
    <div class="flex flex-wrap gap-2">
      <button
        v-for="[key, rule] in rulesList"
        :key="key"
        type="button"
        class="rounded-lg border px-3 py-2 text-sm font-medium"
        :class="key === activeRuleKey ? 'border-teal bg-emerald-50 text-ink' : 'border-slate-300 bg-white text-slate-600'"
        @click="selectedKeys.rule = key"
        @contextmenu="$emit('context-menu', $event, 'rule', key)"
      >
        {{ rule.ruleName || key }}
      </button>
      <button type="button" class="rounded-lg border border-dashed border-slate-300 px-3 py-2 text-sm font-medium text-slate-600" @click="$emit('add-rule')">
        Add rule
      </button>
    </div>

    <div v-if="activeRule" class="grid gap-4 rounded-lg border border-slate-200 bg-slate-50 p-4 md:grid-cols-2">
      <label class="grid gap-1 text-sm">
        <HelpLabel label="Rule name" :help="fieldHelp.ruleName" />
        <input :value="activeRule.ruleName" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @input="$emit('update-rule', 'ruleName', $event.target.value)" />
      </label>
      <label class="grid gap-1 text-sm">
        <HelpLabel label="Check type" :help="fieldHelp.checkType" />
        <select :value="activeRule.check?.type" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @change="$emit('update-rule', 'type', $event.target.value)">
          <option v-for="option in checkTypeOptions" :key="option" :value="option">{{ option }}</option>
        </select>
      </label>
      <label v-if="'pattern' in (activeRule.check ?? {})" class="grid gap-1 text-sm">
        <HelpLabel label="Pattern" :help="fieldHelp.pattern" />
        <input :value="activeRule.check?.pattern" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @input="$emit('update-rule', 'pattern', $event.target.value)" />
      </label>
      <label v-if="'level' in (activeRule.check ?? {})" class="grid gap-1 text-sm">
        <HelpLabel label="Level" :help="fieldHelp.level" />
        <input :value="activeRule.check?.level" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @input="$emit('update-rule', 'level', $event.target.value)" />
      </label>
      <label v-if="'shorterThan' in (activeRule.check ?? {})" class="grid gap-1 text-sm">
        <HelpLabel label="Shorter than" :help="fieldHelp.shorterThan" />
        <input :value="activeRule.check?.shorterThan" type="number" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @input="$emit('update-rule', 'shorterThan', $event.target.value)" />
      </label>
      <label v-if="'longerThan' in (activeRule.check ?? {})" class="grid gap-1 text-sm">
        <HelpLabel label="Longer than" :help="fieldHelp.longerThan" />
        <input :value="activeRule.check?.longerThan" type="number" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @input="$emit('update-rule', 'longerThan', $event.target.value)" />
      </label>
      <label v-if="'before' in (activeRule.check ?? {})" class="grid gap-1 text-sm">
        <HelpLabel label="Before timestamp" :help="fieldHelp.before" />
        <input :value="activeRule.check?.before" type="datetime-local" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @input="$emit('update-rule', 'before', $event.target.value)" />
      </label>
      <label v-if="'after' in (activeRule.check ?? {})" class="grid gap-1 text-sm">
        <HelpLabel label="After timestamp" :help="fieldHelp.after" />
        <input :value="activeRule.check?.after" type="datetime-local" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @input="$emit('update-rule', 'after', $event.target.value)" />
      </label>
      <label class="grid gap-1 text-sm">
        <HelpLabel label="Log target count" :help="fieldHelp.logTargetCount" />
        <input :value="activeRule.logTargetCount" type="number" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @input="$emit('update-rule', 'logTargetCount', $event.target.value)" />
      </label>
      <label class="grid gap-1 text-sm">
        <HelpLabel label="Max completions" :help="fieldHelp.maxCompletions" />
        <input :value="activeRule.maxCompletionsPerAlert" type="number" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @input="$emit('update-rule', 'maxCompletionsPerAlert', $event.target.value)" />
      </label>
    </div>
  </section>
</template>
