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
    <!-- 装饰性背景元素 -->
    <div class="bg-decoration">
      <div class="bg-glow bg-glow--1"></div>
      <div class="bg-glow bg-glow--2"></div>
      <div class="bg-grid"></div>
    </div>
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
  background-color: var(--bf-bg-primary);
  transition: background-color var(--bf-transition-normal, 0.25s ease);
}

/* 默认背景 */
.page-bg--default {
  background: var(--bf-bg-primary);
}

/* 渐变背景 - 用于需要突出强调的页面 (如登录/注册) */
.page-bg--gradient {
  background: linear-gradient(
    135deg,
    var(--bf-bg-primary) 0%,
    var(--bf-bg-secondary) 50%,
    var(--bf-bg-tertiary) 100%
  );
}

/* 暗色模式渐变 */
html.dark .page-bg--gradient {
  background: linear-gradient(
    135deg,
    var(--bf-bg-primary, #0D0D0D) 0%,
    #1a1410 50%,
    var(--bf-bg-secondary, #1A1A1A) 100%
  );
}

/* 极简背景 */
.page-bg--minimal {
  background: var(--bf-bg-primary);
}

/* 装饰性背景元素 */
.bg-decoration {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

/* 发光效果 */
.bg-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  transition: opacity var(--bf-transition-normal, 0.25s ease);
}

.bg-glow--1 {
  width: 600px;
  height: 600px;
  background: var(--bf-primary, #FF6B35);
  top: -200px;
  right: -100px;
  opacity: 0.08;
  animation: float-glow 20s ease-in-out infinite;
}

html.dark .bg-glow--1 {
  opacity: 0.15;
}

.bg-glow--2 {
  width: 400px;
  height: 400px;
  background: #FF9F1C;
  bottom: -100px;
  left: -50px;
  opacity: 0.06;
  animation: float-glow 15s ease-in-out infinite reverse;
}

html.dark .bg-glow--2 {
  opacity: 0.12;
}

@keyframes float-glow {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -20px) scale(1.05);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.95);
  }
}

/* 网格背景 */
.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(var(--bf-border-subtle, rgba(0, 0, 0, 0.02)) 1px, transparent 1px),
    linear-gradient(90deg, var(--bf-border-subtle, rgba(0, 0, 0, 0.02)) 1px, transparent 1px);
  background-size: 60px 60px;
  mask-image: radial-gradient(ellipse 80% 50% at 50% 0%, black 70%, transparent 100%);
}

html.dark .bg-grid {
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.02) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.02) 1px, transparent 1px);
}

/* 内容区域 */
.page-content {
  position: relative;
  z-index: 1;
  min-height: 100vh;
}
</style>
