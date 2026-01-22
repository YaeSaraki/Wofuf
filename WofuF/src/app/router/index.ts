import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/modules/players/views/ServerStats.vue'),
    },
    {
      path: '/players/:name',
      name: 'player',
      component: () => import('@/modules/players/views/PlayerView.vue'),
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('@/app/AboutView.vue'),
    },
  ],
})

export default router
