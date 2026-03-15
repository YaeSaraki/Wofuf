<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import { statisticGroups } from '@M/players/config/statisticGroups.ts'
import { useLocale } from '@S/services/i18n/useLocale.ts'
import { statisticService } from '@M/players/services'
import type { PlayerStatisticList } from '@M/players/dtos/PlayerStatistic.ts'

import './styles'

const props = defineProps<{ playerUuid: string }>()

/* ---------------- 复用通用加载逻辑 ---------------- */
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

/* ---------------- 业务状态 ---------------- */
const playerStatisticList = ref<PlayerStatisticList | null>(null)
const useConvertedUnits = ref(true)

// 国际化
const { translate } = useLocale()

// 获取玩家统计数据
async function fetchPlayerStatistics() {
  if (!props.playerUuid) return
  // 从所有分组中提取所有具体统计键
  const allRequiredKeys = statisticGroups.flatMap((group) => group.statistics)

  const result = await executeAsync(
    async (signal) => {
      const result = await statisticService.getPlayerStatistics(props.playerUuid, {
        signal,
        keys: allRequiredKeys, // 使用具体的统计键作为keys参数
      })
      if (result.isSuccess) {
        return result.getValue()
      }
    },
    translate('players', 'error.loading-stats'),
  ) // 使用带模块前缀的翻译键

  if (result) {
    playerStatisticList.value = result
  }
}

// 单位转换函数
function formatValue(value: number, key: string): string {
  if (!useConvertedUnits.value) {
    return value.toString()
  }

  // 路程单位转换为块
  if (key.includes('distance') || key.includes('travel')) {
    return `${value} 块`
  }

  // 其他可能的单位转换
  if (value >= 1000000) {
    return `${(value / 1000000).toFixed(1)} M`
  } else if (value >= 1000) {
    return `${(value / 1000).toFixed(1)} K`
  }

  return value.toString()
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
    <div class="flex justify-between items-center mb-4">
      <h3 class="font-bold text-lg text-zinc-800 dark:text-white">
        {{ translate('players', 'stats.title') }}
      </h3>
      <button
        @click="useConvertedUnits = !useConvertedUnits"
        class="flex items-center gap-2 px-3 py-1.5 text-sm bg-zinc-300/50 dark:bg-zinc-600/50 hover:bg-zinc-300/70 dark:hover:bg-zinc-600/70 rounded-lg border border-zinc-200 dark:border-zinc-700 transition-colors"
      >
        <svg
          v-if="!useConvertedUnits"
          class="w-4 h-4 text-gray-600 dark:text-gray-400"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
          />
          <path
            d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
          />
        </svg>
        <svg
          v-else
          class="w-4 h-4 text-gray-600 dark:text-gray-400"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L6.59 6.59m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
          />
        </svg>
        {{
          useConvertedUnits
            ? translate('players', 'stats.units.original')
            : translate('players', 'stats.units.converted')
        }}
      </button>
    </div>

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
              {{ useConvertedUnits ? formatValue(group.total, group.category) : group.total }}
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
                formatValue(stat.value, stat.key)
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
            <span class="text-sm font-medium text-zinc-800 dark:text-white">{{
              formatValue(stat.value, stat.key)
            }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
