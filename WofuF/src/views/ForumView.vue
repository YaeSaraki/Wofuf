<script lang="ts" setup>
import PostList from '@M/forum/components/postList/PostList.vue'
import { translate } from '@S/services/i18n'
import Button from 'primevue/button'
import PageBackground from '@S/components/PageBackground.vue'
import { authService } from '@M/auth/services/AuthService.ts'
import { computed } from 'vue'
import router from '@S/infra/router'

// 是否已登录
const isLoggedIn = computed(() => authService.isAuthenticated())

// 跳转到发帖页
function goToCreatePost() {
  if (!isLoggedIn.value) {
    router.push('/forum/login')
    return
  }
  router.push('/forum/create')
}
</script>

<template>
  <PageBackground variant="default" :show-pattern="true">
    <!-- 页面头部 -->
    <header class="forum-header">
      <div class="header-content">
        <div class="header-text">
          <h1 class="forum-title">{{ translate('forum', 'forumTitle') }}</h1>
          <p class="forum-subtitle">{{ translate('forum', 'forumSubtitle') }}</p>
        </div>
        <Button
          :label="
            isLoggedIn ? translate('forum', 'create_post') : translate('forum', 'loginToPost')
          "
          icon="pi pi-plus"
          @click="goToCreatePost"
        />
      </div>
    </header>
    <PostList />
  </PageBackground>
</template>

<style scoped>
/* 头部 */
.forum-header {
  background: var(--w-header-gradient);
  padding: 3rem 1rem;
}

.header-content {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1.5rem;
}

.header-text {
  flex: 1;
}

.forum-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: white;
  margin: 0 0 0.5rem 0;
}

.forum-subtitle {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
}
</style>
