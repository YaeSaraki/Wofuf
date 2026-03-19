<script lang="ts" setup>
const props = defineProps<{
  points: number
  wasUpvoted: boolean | null
  wasDownvoted: boolean | null
  isVoting: boolean
}>()

const emit = defineEmits<{
  (e: 'vote', direction: 'up' | 'down'): void
}>()
</script>

<template>
  <div class="bf-post-vote-footer">
    <div class="bf-vote-footer-content">
      <span class="bf-vote-label">觉得有用？</span>
      <div class="bf-vote-buttons">
        <!-- 点赞按钮 -->
        <button
          class="bf-vote-btn-footer bf-vote-btn-footer--up"
          :class="{ 'bf-vote-btn-footer--active': wasUpvoted }"
          @click="emit('vote', 'up')"
          :disabled="isVoting"
          title="点赞"
        >
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            width="18"
            height="18"
          >
            <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3" />
          </svg>
          <!-- 已点赞时显示 +1 -->
          <span v-if="wasUpvoted" class="bf-vote-count">+1</span>
        </button>
        <!-- 点踩按钮 -->
        <button
          class="bf-vote-btn-footer bf-vote-btn-footer--down"
          :class="{ 'bf-vote-btn-footer--active-down': wasDownvoted }"
          @click="emit('vote', 'down')"
          :disabled="isVoting"
          title="点踩"
        >
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            width="18"
            height="18"
          >
            <path d="M10 15v4a3 3 0 0 0 3 3l4-9V2H5.72a2 2 0 0 0-2 1.7l-1.38 9a2 2 0 0 0 2 2.3zm7-13h2.67A2.31 2.31 0 0 1 22 4v7a2.31 2.31 0 0 1-2.33 2H17" />
          </svg>
          <!-- 已点踩时显示 -1 -->
          <span v-if="wasDownvoted" class="bf-vote-count">-1</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bf-post-vote-footer {
  display: flex;
  justify-content: center;
  padding: var(--bf-space-lg, 24px) 0;
  margin: var(--bf-space-md, 16px) 0;
  border-top: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
  border-bottom: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.08));
}

.bf-vote-footer-content {
  display: flex;
  align-items: center;
  gap: var(--bf-space-md, 16px);
}

.bf-vote-label {
  color: var(--bf-text-muted, #666666);
  font-size: 0.875rem;
}

.bf-vote-buttons {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 8px);
}

.bf-vote-btn-footer {
  display: inline-flex;
  align-items: center;
  gap: var(--bf-space-xs, 6px);
  padding: var(--bf-space-sm, 8px) var(--bf-space-md, 16px);
  background: transparent;
  border: 1px solid var(--bf-border-default, rgba(255, 255, 255, 0.1));
  border-radius: var(--bf-btn-radius, 12px);
  color: var(--bf-text-secondary, #b3b3b3);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--bf-transition-fast, 0.15s ease);
}

.bf-vote-count {
  font-variant-numeric: tabular-nums;
}

/* 点赞按钮样式 */
.bf-vote-btn-footer--up:hover {
  border-color: var(--bf-primary, #ff6b35);
  background: rgba(255, 107, 53, 0.1);
  color: var(--bf-primary, #ff6b35);
}

.bf-vote-btn-footer--active {
  background: rgba(255, 107, 53, 0.15);
  border-color: var(--bf-primary, #ff6b35);
  color: var(--bf-primary, #ff6b35);
}

/* 点踩按钮样式 - 使用紫色，与 PostList 一致 */
.bf-vote-btn-footer--down:hover {
  border-color: #8B5CF6;
  background: rgba(139, 92, 246, 0.1);
  color: #8B5CF6;
}

.bf-vote-btn-footer--active-down {
  background: rgba(139, 92, 246, 0.15);
  border-color: #8B5CF6;
  color: #8B5CF6;
}

.bf-vote-btn-footer:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 响应式 */
@media (max-width: 640px) {
  .bf-vote-footer-content {
    flex-direction: column;
    gap: var(--bf-space-sm, 8px);
  }

  .bf-vote-btn-footer {
    padding: var(--bf-space-xs, 6px) var(--bf-space-sm, 12px);
    font-size: 0.8125rem;
  }
}
</style>
