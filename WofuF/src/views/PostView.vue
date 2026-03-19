<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import PostDetail from '@M/forum/components/postDetail/PostDetail.vue'
import PageBackground from '@S/components/PageBackground.vue'

// 动画效果
const isVisible = ref(false)
onMounted(() => {
  setTimeout(() => {
    isVisible.value = true
  }, 100)
})
</script>

<template>
  <PageBackground variant="default" :show-pattern="true">
    <div class="bf-post-view" :class="{ 'visible': isVisible }">
      <PostDetail />
    </div>
  </PageBackground>
</template>

<style scoped>
.bf-post-view {
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.bf-post-view.visible {
  opacity: 1;
  transform: translateY(0);
}
</style>
