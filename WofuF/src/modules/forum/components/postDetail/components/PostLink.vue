<script lang="ts" setup>
const props = defineProps<{
  link: string
}>()
</script>

<template>
  <div v-if="link" class="bf-link-preview">
    <svg
      class="bf-link-icon"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="1.5"
    >
      <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" />
      <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" />
    </svg>
    <a :href="link" target="_blank" class="bf-link-text">{{ link }}</a>
  </div>
</template>

<style scoped>
.bf-link-preview {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
  padding: var(--bf-space-md, 16px);
  background: var(--bf-input-bg, rgba(255, 255, 255, 0.04));
  border: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
  border-radius: var(--bf-input-radius, 10px);
  margin-bottom: var(--bf-space-md, 16px);
}

.bf-link-icon {
  width: 18px;
  height: 18px;
  color: var(--bf-primary, #ff6b35);
  flex-shrink: 0;
}

.bf-link-text {
  color: var(--bf-primary, #ff6b35);
  text-decoration: none;
  word-break: break-all;
  font-size: 0.875rem;
}

.bf-link-text:hover {
  text-decoration: underline;
}
</style>
