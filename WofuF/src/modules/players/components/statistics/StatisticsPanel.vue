<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import { statisticGroups } from '@M/players/config/statisticGroups.ts'
import { useLocale } from '@S/services/i18n/useLocale.ts'
import { statisticService } from '@M/players/services'
import type { PlayerStatisticList } from '@M/players/dtos/PlayerStatistic.ts'

const props = defineProps<{ playerUuid: string }>()

/* ---------------- 复用通用加载逻辑 ---------------- */
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

/* ---------------- 业务状态 ---------------- */
const playerStatisticList = ref<PlayerStatisticList | null>(null)

// 国际化
const { translate } = useLocale()

// 获取玩家统计数据
async function fetchPlayerStatistics() {
  if (!props.playerUuid) return
  // 从所有分组中提取所有具体统计键
  const allRequiredKeys = statisticGroups.flatMap((group) => group.statistics)

  const result = await executeAsync(async (signal) => {
    const result = await statisticService.getPlayerStatistics(props.playerUuid, {
      signal,
      keys: allRequiredKeys, // 使用具体的统计键作为keys参数
    })
    if (result.isSuccess) {
      return result.getValue()
    }
  }, translate('players','error.loading-stats')) // 使用带模块前缀的翻译键

  if (result) {
    playerStatisticList.value = result
  }
}

// 监听玩家名变化，重新获取数据
watch(
  () => props.playerUuid,
  () => {
    fetchPlayerStatistics()
  },
)

// 组件挂载时获取数据
onMounted(() => {
  fetchPlayerStatistics()
})

// 获取所有统计项（带翻译）
const allStats = computed(() => {
  if (!playerStatisticList.value) return []

  return Object.values(playerStatisticList.value.statistics).map((stat) => ({
    ...stat,
     translatedKey: translate('players', `stats.item.${stat.key}`), // 使用带模块前缀的翻译键
  }))
})

// 计算分组统计
const groupedStats = computed(() => {
  if (!playerStatisticList.value) return []

  return statisticService.calculateGroupTotals(playerStatisticList.value.statistics)
})

// 获取特定分组的统计项
const getStatsByGroup = (groupId: string) => {
  if (!playerStatisticList.value) return []

  const group = statisticGroups.find((g) => g.category === groupId)
  if (!group) return []

  return group.statistics
    .map((statKey) => playerStatisticList.value?.statistics[statKey])
    .filter((stat) => stat !== undefined)
    .map((stat) => ({
      ...stat!,
       translatedKey: translate('players', `stats.item.${stat!.key}`), // 使用带模块前缀的翻译键
    }))
}
</script>
<template>
  <div class="bg-zinc-200 dark:bg-zinc-700 rounded-xl shadow-sm p-4 gap-4">
    <h3 class="font-bold text-lg mb-4 text-zinc-800 dark:text-white">
      {{ translate('players', 'stats.title') }}
    </h3>

    <div
      v-if="isLoading"
      class="flex justify-center items-center h-40 bg-zinc-50 dark:bg-zinc-700 rounded-lg"
    >
      <div
        class="animate-spin h-8 w-8 rounded-full border-2 border-blue-500 border-t-transparent"
      ></div>
    </div>

    <div
      v-else-if="errorMsg"
      class="text-center text-red-500 p-4 bg-red-50 dark:bg-red-900/20 rounded-lg"
    >
      {{ errorMsg }}
    </div>

    <div v-else>
      <!-- 分组统计概览 - 固定大小 Flex 布局（每行对齐） -->
      <div class="mb-6">
        <h4 class="font-semibold text-sm text-zinc-600 dark:text-zinc-300 mb-2">
          {{ translate('players', 'stats.overview') }}
        </h4>
        <div class="flex flex-wrap gap-3 stats-overview-container">
          <div
            v-for="group in groupedStats"
            :key="group.category"
            class="stat-overview-card bg-linear-to-br from-blue-50 to-indigo-50 dark:from-blue-900/30 dark:to-indigo-900/30 p-3 rounded-lg border border-blue-100 dark:border-blue-800"
          >
            <div class="text-xs text-blue-600 dark:text-blue-400 mb-1">
              {{ translate('players', `stats.group.${group.category.toLowerCase()}`) }}
            </div>
            <div class="text-xl font-bold text-zinc-800 dark:text-white">
              {{ group.total }}
            </div>
          </div>
        </div>
      </div>

      <!-- 详细统计分组展示-->
      <div class="space-y-6">
        <div
          v-for="group in statisticGroups"
          :key="group.category"
          class="bg-zinc-300/50 dark:bg-zinc-600/50 rounded-lg p-3 border border-zinc-200 dark:border-zinc-700"
        >
          <h4 class="font-semibold text-sm text-zinc-600 dark:text-zinc-300 mb-3 flex items-center">
            <span class="w-2 h-2 rounded-full bg-blue-500 mr-2"></span>
            {{ translate('players', `stats.group.${group.category.toLowerCase()}`) }}
          </h4>

          <div class="flex flex-wrap gap-x-4 gap-y-2 stats-detail-container">
            <div
              v-for="stat in getStatsByGroup(group.category)"
              :key="stat.key"
              class="stat-detail-item flex justify-between items-center py-1 px-2 hover:bg-zinc-400/50 dark:hover:bg-zinc-500/50 rounded transition-colors"
            >
              <span class="text-sm text-zinc-600 dark:text-zinc-400">{{ stat.translatedKey }}</span>
              <span class="text-sm font-medium text-zinc-800 dark:text-white">{{
                stat.value
              }}</span>
            </div>
          </div>

          <!-- 无数据提示 -->
          <div
            v-if="getStatsByGroup(group.category).length === 0"
            class="text-sm text-gray-500 dark:text-gray-400 italic py-2"
          >
            {{ translate('players', 'stats.no-data') }}
          </div>
        </div>
      </div>

      <!-- 其他未分组统计-->
      <div
        v-if="
          allStats.filter((s) => !statisticGroups.some((g) => g.statistics.includes(s.key)))
            .length > 0
        "
        class="mt-6 bg-zinc-50 dark:bg-zinc-700/50 rounded-lg p-3 border border-zinc-200 dark:border-zinc-700"
      >
        <h4 class="font-semibold text-sm text-zinc-600 dark:text-zinc-300 mb-3 flex items-center">
          <span class="w-2 h-2 rounded-full bg-gray-500 mr-2"></span>
          {{ translate('players', 'stats.other') }}
        </h4>

        <div class="flex flex-wrap gap-x-4 gap-y-2 stats-detail-container">
          <div
            v-for="stat in allStats.filter(
              (s) => !statisticGroups.some((g) => g.statistics.includes(s.key)),
            )"
            :key="stat.key"
            class="stat-detail-item flex justify-between items-center py-1 px-2 hover:bg-zinc-100 dark:hover:bg-zinc-600/50 rounded transition-colors"
          >
            <span class="text-sm text-zinc-600 dark:text-zinc-400">{{ stat.translatedKey }}</span>
            <span class="text-sm font-medium text-zinc-800 dark:text-white">{{ stat.value }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
/* ---------------- 固定元素大小，保证每行对齐 ---------------- */
/* 统计概览容器（顶部卡片容器） */
.stats-overview-container {
  justify-content: flex-start; /* 横向左对齐，保证每行子项对齐规整 */
  align-items: flex-start; /* 多行顶端对齐，避免子项拉伸 */
  align-content: flex-start; /* 换行后多行顶端对齐，间距由 gap 控制 */
}

/* 统计概览卡片（固定宽高，保证所有卡片一致） */
.stat-overview-card {
  width: 180px; /* 固定宽度：保证同一行卡片宽度一致，每行对齐 */
  height: 90px; /* 固定高度：保证所有卡片高度一致，上下对齐 */
  box-sizing: border-box; /* 关键：padding 不占用固定宽高，避免尺寸偏差 */
}

/* 统计详情容器（详细数据列表容器） */
.stats-detail-container {
  justify-content: flex-start; /* 横向左对齐，保证每行详情项对齐规整 */
  align-items: flex-start; /* 多行顶端对齐，避免子项拉伸 */
  align-content: flex-start; /* 换行后多行顶端对齐，间距由 gap 控制 */
}

/* 统计详情项（固定宽高，保证所有详情项一致） */
.stat-detail-item {
  width: 220px; /* 固定宽度：保证同一行详情项宽度一致，每行对齐 */
  height: 45px; /* 固定高度：保证所有详情项高度一致，上下对齐 */
  box-sizing: border-box; /* 关键：padding 不占用固定宽高，避免尺寸偏差 */
}

/* 媒体查询：不同屏幕下调整固定宽度，兼顾适配性（保证每行显示数量规整） */
@media (max-width: 768px) {
  .stat-overview-card {
    width: 140px; /* 小屏缩小卡片宽度，保证每行2列 */
  }
  .stat-detail-item {
    width: 180px; /* 小屏缩小详情项宽度，保证每行2列 */
  }
}

/* 超小屏（手机）：每行1列（固定宽度100%） */
@media (max-width: 400px) {
  .stat-overview-card {
    width: 100%; /* 超小屏占满一行，保证显示完整 */
    height: 80px;
  }
  .stat-detail-item {
    width: 100%; /* 超小屏占满一行，保证显示完整 */
    height: 40px;
  }
}
</style>
