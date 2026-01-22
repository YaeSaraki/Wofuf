<script setup lang="ts">
import { onUnmounted, ref, watch, nextTick } from 'vue'
import type { Player } from '@M/players/dtos/Player.ts'
import { PlayerService } from '@M/players/services/PlayerService.ts'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import { Render, WalkingAnimation } from 'skin3d'
import type { PlayerSkin } from '@M/players/dtos/PlayerSkin.ts'
import { translate } from '@S/services/i18n'
import { base64Util } from '@SU/Base64Util.ts'

const props = defineProps<{ player: Player | null }>()
const playerService = new PlayerService()
const hasSkinViewerResizeListener = ref(false)

/* ---------------- 复用通用加载逻辑 ---------------- */
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()
/* ---------------- 获取玩家皮肤 ---------------- */

const playerSkin = ref<PlayerSkin | null>(null)
const canvas = ref<HTMLCanvasElement | null>(null)
let viewer: Render | null = null
let resizeTimer: ReturnType<typeof setTimeout> | null = null

// 监听玩家变化，重新获取皮肤
watch(
  () => props.player?.id,
  async (newId, oldId) => {
    if (newId && newId !== oldId) {
      await fetchPlayerSkin()
    } else if (!newId) {
      // 如果没有玩家ID，清除皮肤
      playerSkin.value = null
      destroyViewer()
    }
  },
  { immediate: true },
)

// 监听皮肤数据变化，初始化/更新查看器
watch(playerSkin, (newSkin) => {
  if (newSkin) {
    nextTick(() => {
      initOrUpdateViewer(newSkin)
    })
  } else {
    destroyViewer()
  }
})

async function fetchPlayerSkin() {
  if (!props.player?.id) {
    playerSkin.value = null
    return
  }
  try {
    const playerSkinData = await executeAsync(async (signal) => {
      const apiResult = await playerService.getPlayerSkin(props.player!.id, { signal })
      if (apiResult.isSuccess) {
        return apiResult.getValue()
      }
      throw new Error('玩家皮肤获取失败')
    }, '获取玩家皮肤失败')

    playerSkin.value = playerSkinData || null
  } catch (error) {
    console.error('获取玩家皮肤异常：', error)
    playerSkin.value = null
  }
}

/* ---------------- 3D皮肤展示 ---------------- */
function getOptimalDimensions() {
  if (window.innerWidth < 768) {
    return {
      width: Math.min(window.innerWidth * 0.3, 300),
      height: Math.min(window.innerWidth * 0.6, 600),
      quality: window.devicePixelRatio || 1,
    }
  }
  return {
    width: Math.min(window.innerWidth * 0.1, 200),
    height: Math.min(window.innerWidth * 0.2, 400),
    quality: 1,
  }
}

const dims = getOptimalDimensions()
function initOrUpdateViewer(skinData: PlayerSkin) {
  if (!canvas.value) {
    console.error('Canvas element not found')
    return
  }

  if (viewer) {
    // 更新现有查看器
    viewer.setSize(dims.width, dims.height)
    viewer.loadSkin(skinData.skin || '')
    if (skinData.cape) {
      viewer.loadCape(skinData.cape)
    }
  } else {
    // 创建新查看器
    viewer = new Render({
      canvas: canvas.value,
      width: dims.width,
      height: dims.height,
      skin: base64Util(skinData.skin || ''),
      cape: base64Util(skinData.cape || ''),
    })

    viewer.animation = new WalkingAnimation()
    viewer.animation.speed = 0.5
    // viewer.autoRotate = true
    // viewer.autoRotateSpeed = 0.5

    // 确保事件监听只添加一次
    if (!hasSkinViewerResizeListener.value) {
      window.addEventListener('resize', onResize)
      hasSkinViewerResizeListener.value = true
    }
  }
}

function resizeViewer() {
  if (!viewer || !canvas.value) return

  const dims = getOptimalDimensions()
  viewer.setSize(dims.width, dims.height)
}

function onResize() {
  if (resizeTimer) clearTimeout(resizeTimer)
  resizeTimer = setTimeout(resizeViewer, 150)
}

function destroyViewer() {
  if (viewer) {
    viewer.dispose()
    viewer = null
  }
}

// 清理函数
function cleanup() {
  if (resizeTimer) {
    clearTimeout(resizeTimer)
    resizeTimer = null
  }

  destroyViewer()

  // 移除全局事件监听
  if (hasSkinViewerResizeListener.value) {
    window.removeEventListener('resize', onResize)
    hasSkinViewerResizeListener.value = false
  }
}

// onMounted(() => {
//   // 如果props中已经有玩家数据，watch会立即触发获取
//   // 不需要额外的setTimeout
// })

onUnmounted(() => {
  cleanup()
})

// defineExpose({
//   refreshSkin: fetchPlayerSkin,
//   getViewer: () => viewer
// })
</script>

<template>
  <div v-if="isLoading" class="flex justify-center items-center h-40">
    <div
      class="animate-spin h-6 w-6 rounded-full border-2 border-blue-500 border-t-transparent"
    ></div>
  </div>
  <div v-else-if="errorMsg" class="text-center text-red-500">
    {{ errorMsg }}
  </div>
  <div
    v-else-if="player"
    class="flex flex-col justify-center items-center text-center space-y-4 px-6 py-8 w-full"
  >
    <!-- 玩家名称 -->
    <h2 class="text-2xl md:text-3xl font-bold text-zinc-800 dark:text-zinc-100 tracking-tight">
      {{ player.name }}
    </h2>

    <!--    &lt;!&ndash; 玩家头像（加圆角+阴影，提升质感） &ndash;&gt;-->
    <!--    <div class="relative">-->
    <!--      <img-->
    <!--        class="w-24 h-24 md:w-28 md:h-28 rounded-full border-4 border-white dark:border-zinc-700 shadow-lg object-cover"-->
    <!--        :src="`https://visage.surgeplay.com/bust/128/${player.name}`"-->
    <!--        :alt="`${player.name} ${translate('players', 'alt.avatar')}`"-->
    <!--      />-->
    <!--    </div>-->

    <canvas
      class="rounded-lg border-purple-300 hover:hover:border-2 hover:scale-105"
      ref="canvas"
      :width="dims.width"
      :height="dims.height"
    />

    <!-- 信息面板 -->
    <div class="grid grid-cols-1 gap-3 w-full max-w-70">
      <!-- 游玩时长 -->
      <div class="bg-zinc-200 dark:bg-zinc-700 rounded-lg p-3 shadow-sm">
        <p class="text-sm text-zinc-500 dark:text-zinc-400 font-medium">
          {{ translate('players', 'player.playtime') }}
        </p>
        <p class="text-lg font-semibold text-zinc-800 dark:text-zinc-100">
          {{ (player.totalPlaytimeSeconds / 360000000).toFixed(1) }} h
        </p>
      </div>

      <!-- 上次游玩 -->
      <div class="bg-zinc-200 dark:bg-zinc-700 rounded-lg p-3 shadow-sm">
        <p class="text-sm text-zinc-500 dark:text-zinc-400 font-medium">
          {{ translate('players', 'player.last-login') }}
        </p>
        <p class="text-lg font-semibold text-zinc-800 dark:text-zinc-100">
          {{ new Date(player.lastLogin).toLocaleString().split(',')[0] }}
        </p>
      </div>

      <!-- 注册时间 -->
      <div class="bg-zinc-200 dark:bg-zinc-700 rounded-lg p-3 shadow-sm">
        <p class="text-sm text-zinc-500 dark:text-zinc-400 font-medium">
          {{ translate('players', 'player.register-time') }}
        </p>
        <p class="text-lg font-semibold text-zinc-800 dark:text-zinc-100">
          {{ new Date(player.firstLogin).toLocaleString().split(',')[0] }}
        </p>
      </div>
    </div>
  </div>
</template>
