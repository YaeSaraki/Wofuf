<script lang="ts" setup>
const props = defineProps<{
  numComments: number
  showComments: boolean
}>()

const emit = defineEmits<{
  (e: 'toggle-comments'): void
}>()
</script>

<template>
  <div class="bf-post-actions">
    <button class="bf-action-btn" @click="emit('toggle-comments')">
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="1.5"
        width="18"
        height="18"
      >
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
      </svg>
      <span>{{ showComments ? '隐藏评论' : '评论' }}</span>
      <span class="bf-action-count">({{ numComments }})</span>
    </button>
    <button class="bf-action-btn">
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="1.5"
        width="18"
        height="18"
      >
        <circle cx="18" cy="5" r="3" />
        <circle cx="6" cy="12" r="3" />
        <circle cx="18" cy="19" r="3" />
        <line x1="8.59" y1="13.51" x2="15.42" y2="17.49" />
        <line x1="15.41" y1="6.51" x2="8.59" y2="10.49" />
      </svg>
      <span>分享</span>
    </button>
    <button class="bf-action-btn">
      <svg
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="1.5"
        width="18"
        height="18"
      >
        <path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z" />
      </svg>
      <span>收藏</span>
    </button>
  </div>
</template>

<style scoped>
.bf-post-actions {
  display: flex;
  gap: var(--bf-space-sm, 8px);
  padding-top: var(--bf-space-md, 16px);
  border-top: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
}

.bf-action-btn {
  display: flex;
  align-items: center;
  gap: var(--bf-space-xs, 4px);
  padding: var(--bf-space-sm, 8px) var(--bf-space-md, 16px);
  background: transparent;
  border: none;
  border-radius: var(--bf-btn-radius, 12px);
  color: var(--bf-text-secondary, #b3b3b3);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-action-btn:hover {
  background: rgba(255, 255, 255, 0.04);
  color: var(--bf-text-primary, #ffffff);
}

.bf-action-count {
  color: var(--bf-text-muted, #666666);
  font-size: 0.75rem;
}
</style>
