import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/ServerStatsView.vue'),
    },
    {
      path: '/players/:name',
      name: 'player',
      component: () => import('@/views/PlayerView.vue'),
    },
    {
      path: '/forum',
      name: 'forum',
      component: () => import('@/views/ForumView.vue'),
    },
    {
      path: '/forum/create',
      name: 'createPost',
      component: () => import('@/views/CreatePostView.vue'),
    },
    {
      path: '/forum/edit/:slug',
      name: 'editPost',
      component: () => import('@/views/EditPostView.vue'),
    },
    {
      path: '/forum/posts/:slug',
      name: 'post',
      component: () => import('@/views/PostView.vue'),
    },
    {
      path: '/forum/login',
      name: 'login',
      component: () => import('@/views/ForumLoginView.vue'),
    },
    {
      path: '/forum/register',
      name: 'register',
      component: () => import('@/views/ForumRegisterView.vue'),
    },
    {
      path: '/forum/admin',
      name: 'admin',
      component: () => import('@/views/AdminView.vue'),
    },
    {
      path: '/forum/members/:nickname',
      name: 'memberProfile',
      component: () => import('@/views/MemberProfileView.vue'),
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('@/views/AboutView.vue'),
    },
  ],
  scrollBehavior(to, from, savedPosition) {
    // 如果是通过浏览器前进/后退触发的路由变化，返回保存的位置
    if (savedPosition) {
      return savedPosition
    }
    // 如果是正常的路由跳转（push），滚动到顶部
    return { top: 0 }
  },
})

export default router
