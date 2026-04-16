<script setup>
defineProps({
  open: {
    type: Boolean,
    required: true,
  },
  title: {
    type: String,
    required: true,
  },
  message: {
    type: String,
    required: true,
  },
  confirmLabel: {
    type: String,
    required: true,
  },
})

defineEmits(['cancel', 'confirm'])
</script>

<template>
  <div
    v-if="open"
    class="fixed inset-0 z-50 grid place-items-center bg-slate-950/45 px-4"
    role="dialog"
    aria-modal="true"
    aria-labelledby="confirm-dialog-title"
  >
    <section class="w-full max-w-md rounded-lg border border-slate-200 bg-white p-5 shadow-xl">
      <h2 id="confirm-dialog-title" class="text-xl font-semibold">{{ title }}</h2>
      <p class="mt-3 text-sm leading-6 text-slate-600">{{ message }}</p>
      <div class="mt-5 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
        <button
          type="button"
          class="inline-flex min-h-10 items-center justify-center rounded-lg border border-slate-300 bg-white px-4 text-sm font-medium text-ink transition-colors hover:border-slate-400"
          @click="$emit('cancel')"
        >
          Cancel
        </button>
        <button
          type="button"
          class="inline-flex min-h-10 items-center justify-center rounded-lg border border-rose-600 bg-rose-600 px-4 text-sm font-medium text-white transition-colors hover:bg-rose-700"
          @click="$emit('confirm')"
        >
          {{ confirmLabel }}
        </button>
      </div>
    </section>
  </div>
</template>
