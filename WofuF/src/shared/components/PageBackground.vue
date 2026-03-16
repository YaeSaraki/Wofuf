<script setup lang="ts">
import { computed } from 'vue'

type BackgroundVariant = 'default' | 'gradient' | 'minimal'

const props = withDefaults(defineProps<{
  variant?: BackgroundVariant
  showPattern?: boolean
}>(), {
  variant: 'default',
  showPattern: true,
})

const bgClass = computed(() => ({
  'page-bg': true,
  [`page-bg--${props.variant}`]: true,
  'page-bg--pattern': props.showPattern,
}))
</script>

<template>
  <div :class="bgClass">
    <!-- 内容插槽 -->
    <div class="page-content">
      <slot></slot>
    </div>
  </div>
</template>

<style scoped>
.page-bg {
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

/* 默认背景 */
.page-bg--default {
  background: var(--w-surface);
}

/* 渐变背景 - 用于需要突出强调的页面 (如登录/注册) */
.page-bg--gradient {
  background: linear-gradient(
    180deg,
    #f4f4f5 0%,
    #e4e4e7 100%
  );
}

:global(.dark) .page-bg--gradient {
  background: linear-gradient(
    180deg,
    #09090b 0%,
    #18181b 100%
  );
}

/* 极简背景 */
.page-bg--minimal {
  background: var(--w-surface);
}

/* 内容区域 */
.page-content {
  position: relative;
  z-index: 1;
  min-height: 100vh;
}
</style>
