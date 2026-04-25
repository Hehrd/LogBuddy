<script setup>
import HelpLabel from '../HelpLabel.vue'

defineProps({
  value: Object,
  fieldHelp: Object,
})

defineEmits(['update-field'])
</script>

<template>
  <section class="grid gap-4 rounded-lg border border-slate-200 bg-slate-50 p-4 md:grid-cols-2">
    <label class="grid gap-1 text-sm">
      <HelpLabel label="Control panel server port" :help="fieldHelp.controlPanelServerPort" />
      <input :value="value.controlPanelServerPort" type="number" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @input="$emit('update-field', 'controlPanelServerPort', Number($event.target.value))" />
    </label>
    <label class="grid gap-1 text-sm">
      <HelpLabel label="gRPC host" :help="fieldHelp.grpcHost" />
      <input :value="value.grpcSettings?.serverHost" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @input="$emit('update-field', 'grpcSettings.serverHost', $event.target.value)" />
    </label>
    <label class="grid gap-1 text-sm">
      <HelpLabel label="gRPC port" :help="fieldHelp.grpcPort" />
      <input :value="value.grpcSettings?.serverPort" type="number" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @input="$emit('update-field', 'grpcSettings.serverPort', Number($event.target.value))" />
    </label>
    <label class="grid gap-1 text-sm">
      <HelpLabel label="Max lines per request" :help="fieldHelp.maxLinesPerReq" />
      <input :value="value.grpcSettings?.maxLinesPerReq" type="number" class="rounded-lg border border-slate-300 bg-white px-3 py-2" @input="$emit('update-field', 'grpcSettings.maxLinesPerReq', Number($event.target.value))" />
    </label>
  </section>
</template>
