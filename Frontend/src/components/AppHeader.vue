<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  intro: {
    type: String,
    required: true,
  },
})
</script>

<template>
  <div class="flex items-start gap-4">
    <img src="/logo/image.png" alt="LogBuddy logo" class="h-12 w-12 rounded-lg object-contain" />
    <div>
      <p class="mb-2 text-sm font-bold uppercase tracking-normal text-teal">LogBuddy</p>
      <h1 class="text-4xl font-semibold tracking-normal sm:text-5xl">{{ title }}</h1>
      <p class="mt-4 max-w-3xl text-base text-slate-600 sm:text-lg">
        {{ intro }}
      </p>
    </div>
  </div>
</template>
