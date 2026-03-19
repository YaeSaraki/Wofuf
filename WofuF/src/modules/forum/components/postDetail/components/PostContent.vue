<script lang="ts" setup>
import { ref } from 'vue'
import MarkdownRenderer from '@M/forum/components/shared/MarkdownRenderer.vue'

const props = defineProps<{
  content: string
}>()

const emit = defineEmits<{
  (e: 'image-click', src: string, alt: string): void
}>()

const postContentRef = ref<HTMLElement | null>(null)

function openLightbox(src: string, alt: string) {
  emit('image-click', src, alt)
}

defineExpose({
  postContentRef
})
</script>

<template>
  <div class="bf-post-content" ref="postContentRef">
    <MarkdownRenderer :content="content" @image-click="openLightbox" />
  </div>
</template>

<style scoped>
.bf-post-content {
  word-wrap: break-word;
  overflow-wrap: break-word;
  overflow-x: hidden;
  color: var(--bf-text-secondary, #b3b3b3);
  line-height: 1.7;
  margin-bottom: var(--bf-space-md, 16px);
}

.bf-post-content * {
  max-width: 100%;
  height: auto;
  box-sizing: border-box;
}

.bf-post-content pre {
  overflow-x: auto;
  max-width: 100%;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.bf-post-content code {
  word-break: break-all;
  white-space: normal;
}
</style>
