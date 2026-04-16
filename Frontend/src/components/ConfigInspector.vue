<script setup>
defineProps({
  summary: {
    type: Array,
    required: true,
  },
})
</script>

<template>
  <aside class="grid content-start gap-4">
    <section class="rounded-lg border border-slate-200 bg-white p-4">
      <h3 class="text-lg font-semibold">Structure</h3>
      <div v-if="summary.length" class="mt-4 grid gap-3">
        <article
          v-for="item in summary"
          :key="item.title"
          class="rounded-lg border border-slate-200 bg-white p-4"
        >
          <strong class="block font-semibold">{{ item.title }}</strong>
          <span class="mt-1 block text-sm text-slate-500">{{ item.subtitle }}</span>
          <p class="mt-2 text-sm text-slate-500">{{ item.detail }}</p>
        </article>
      </div>
      <p v-else class="mt-4 text-sm text-slate-500">
        Fix the JSON to see a readable summary here.
      </p>
    </section>

    <section class="rounded-lg border border-slate-200 bg-white p-4">
      <h3 class="text-lg font-semibold">Editing notes</h3>
      <ul class="mt-4 list-disc space-y-2 pl-5 text-sm text-slate-500">
        <li>Use form mode for the common fields and raw mode for anything not exposed yet.</li>
        <li>Lists like rules, intervals, and endpoints are comma-separated for now.</li>
        <li>Format before downloading if you want cleaner diffs.</li>
        <li>Load template and reset both replace the current tab completely.</li>
      </ul>
    </section>
  </aside>
</template>
