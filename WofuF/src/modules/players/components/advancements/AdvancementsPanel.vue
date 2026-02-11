<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useAsyncLoader } from '@SU/async/useAsyncLoader.ts'
import { useLocale } from '@S/services/i18n/useLocale.ts'
import type { PlayerAdvancement, PlayerAdvancementList } from '@M/players/dtos/PlayerAdvancement.ts'
import { advancementService } from '@M/players/services'
import DraggablePopup from '@S/components/DraggablePopup.vue'

import './styles'

const props = defineProps<{ playerUuid: string }>()

/* ---------------- 复用通用加载逻辑 ---------------- */
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

/* ---------------- 弹窗相关逻辑 ---------------- */
const popupState = ref({
  visible: false,
  currentAdvancement: null as PlayerAdvancement | null,
  initX: 0,
  initY: 0,
})

const openPopup = (advancement: PlayerAdvancement, event: MouseEvent) => {
  event.stopPropagation()
  popupState.value = {
    visible: true,
    currentAdvancement: advancement,
    initX: event.clientX,
    initY: event.clientY,
  }
}

const closePopup = () => {
  popupState.value = {
    visible: false,
    currentAdvancement: null,
    initX: 0,
    initY: 0,
  }
}

/* ---------------- 业务状态 ---------------- */

const playerAdvancementList = ref<PlayerAdvancementList | null>(null)

// 展示未分组的成就
const showUngrouped = ref(false)

// 控制显示/隐藏未完成成就的状态
const showIncomplete = ref(false) // 默认隐藏未完成成就

// 国际化
const { translate } = useLocale()

// 获取玩家成就数据
async function fetchPlayerAdvancements() {
  if (!props.playerUuid) return
  const result = await executeAsync(
    async (signal) => {
      const result = await advancementService.getPlayerAdvancements(props.playerUuid, { signal })
      if (result.isSuccess) {
        return result.getValue()
      }
    },
    translate('players', 'error.loading-advancements'),
  )

  if (result?.advancements) {
    playerAdvancementList.value = result
  }
}

// 监听玩家名变化，重新获取数据
watch(
  () => props.playerUuid,
  () => {
    fetchPlayerAdvancements()
  },
)

// 过滤后的成就列表
const filteredAdvancements = computed(() => {
  if (!playerAdvancementList?.value?.advancements) return []
  return advancementService.filterNonGroupedAdvancements(playerAdvancementList.value.advancements)
})

// 成就统计
const advancementStats = computed(() => {
  if (!filteredAdvancements.value.length) return { total: 0, completed: 0, percentage: 0 }

  // 以分组中的成就总数为基础计算
  const total = advancementService.getTotalAdvancementCount()
  const completed = filteredAdvancements.value.filter((adv) => adv.done).length
  const percentage = Math.round((completed / total) * 100)

  return { total, completed, percentage }
})

// 按分类分组的成就
const advancementsByCategory = computed(() => {
  if (!filteredAdvancements.value.length) return []
  return advancementService.calculateAdvancementGroupTotals(
    playerAdvancementList.value!.advancements,
  )
})

// 未分组的成就
const ungroupedAdvancements = computed(() => {
  if (!playerAdvancementList?.value?.advancements) return []
  return advancementService.getUngroupedAdvancements(playerAdvancementList.value.advancements)
})

// 获取分类的未完成成就
const getGroupIncompleteAdvancements = (categoryId: string) => {
  const groupAdvancements = advancementService.getAdvancementsByGroup(
    playerAdvancementList.value!.advancements,
    categoryId,
  )
  return groupAdvancements.filter((adv) => !adv.done)
}

// 获取分类的完成成就
const getGroupCompleteAdvancements = (categoryId: string) => {
  const groupAdvancements = advancementService.getAdvancementsByGroup(
    playerAdvancementList.value!.advancements,
    categoryId,
  )
  return groupAdvancements.filter((adv) => adv.done)
}

// 获取未分组的完成成就
const ungroupedCompleteAdvancements = computed(() => {
  if (!ungroupedAdvancements.value.length) return []
  return ungroupedAdvancements.value.filter((adv) => adv.done)
})

// 获取未分组的未完成成就
const ungroupedIncompleteAdvancements = computed(() => {
  if (!ungroupedAdvancements.value.length) return []
  return ungroupedAdvancements.value.filter((adv) => !adv.done)
})

// 切换显示/隐藏未完成成就
const toggleShowIncomplete = () => {
  showIncomplete.value = !showIncomplete.value
}
</script>

<template>
  <div class="bg-zinc-200 dark:bg-zinc-700 rounded-xl shadow-sm p-4 relative">
    <div class="flex justify-between items-center mb-4">
      <h3 class="font-bold text-lg text-zinc-800 dark:text-white">
        {{ translate('players', 'advancements.title') }}
      </h3>

      <button
        @click="toggleShowIncomplete"
        class="flex items-center gap-2 px-3 py-1.5 text-sm bg-zinc-300/50 dark:bg-zinc-600/50 hover:bg-zinc-300/70 dark:hover:bg-zinc-600/70 rounded-lg border border-zinc-200 dark:border-zinc-700 transition-colors"
        :title="
          showIncomplete
            ? translate('players', 'advancements.hide-incomplete')
            : translate('players', 'advancements.show-incomplete')
        "
      >
        <svg
          v-if="!showIncomplete"
          class="w-4 h-4 text-gray-600 dark:text-gray-400"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L6.59 6.59m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"
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
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
          />
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
          />
        </svg>
        <span class="text-xs text-zinc-700 dark:text-zinc-300">
          {{
            showIncomplete
              ? translate('players', 'advancements.incomplete-show')
              : translate('players', 'advancements.incomplete-hidden')
          }}
        </span>
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
    <div v-else-if="playerAdvancementList">
      <!-- 成就统计概览 -->
      <div class="mb-6">
        <h4 class="font-semibold text-sm text-zinc-600 dark:text-zinc-300 mb-2">
          {{ translate('players', 'advancements.overview') }}
        </h4>

        <!-- 统计卡片 -->
        <div class="flex flex-wrap gap-3 mb-4 adv-stats-container">
          <div
            class="adv-stat-card bg-linear-to-br from-blue-50 to-indigo-50 dark:from-blue-900/30 dark:to-indigo-900/30 p-3 rounded-lg border border-blue-100 dark:border-blue-800"
          >
            <div class="text-xs text-blue-600 dark:text-blue-400 mb-1">
              {{ translate('players', 'advancements.total') }}
            </div>
            <div class="text-xl font-bold text-zinc-800 dark:text-white">
              {{ advancementStats.total }}
            </div>
          </div>
          <div
            class="adv-stat-card bg-linear-to-br from-green-50 to-emerald-50 dark:from-green-900/30 dark:to-emerald-900/30 p-3 rounded-lg border border-green-100 dark:border-green-800"
          >
            <div class="text-xs text-green-600 dark:text-green-400 mb-1">
              {{ translate('players', 'advancements.completed') }}
            </div>
            <div class="text-xl font-bold text-zinc-800 dark:text-white">
              {{ advancementStats.completed }}
            </div>
          </div>
          <div
            class="adv-stat-card bg-linear-to-br from-purple-50 to-pink-50 dark:from-purple-900/30 dark:to-pink-900/30 p-3 rounded-lg border border-purple-100 dark:border-purple-800"
          >
            <div class="text-xs text-purple-600 dark:text-purple-400 mb-1">
              {{ translate('players', 'advancements.completion-rate') }}
            </div>
            <div class="text-xl font-bold text-zinc-800 dark:text-white">
              {{ advancementStats.percentage }}%
            </div>
          </div>
        </div>

        <!-- 进度条 -->
        <div class="w-full bg-zinc-200 dark:bg-zinc-700 rounded-full h-2.5">
          <div
            class="bg-linear-to-r from-blue-500 to-indigo-600 h-2.5 rounded-full transition-all duration-500"
            :style="{ width: `${advancementStats.percentage}%` }"
          ></div>
        </div>
      </div>

      <!-- 成就分类展示 -->
      <div class="space-y-6">
        <div
          v-for="group in advancementsByCategory"
          :key="group.category"
          class="bg-zinc-300/50 dark:bg-zinc-600/50 rounded-lg p-3 border border-zinc-200 dark:border-zinc-700"
        >
          <h4 class="font-semibold text-sm text-zinc-600 dark:text-zinc-300 mb-3 flex items-center">
            <span class="w-2 h-2 rounded-full bg-green-500 mr-2"></span>
            {{ group.name }}
            <span class="ml-2 text-xs text-zinc-500 dark:text-zinc-400">
              ({{ group.completed }}/{{ group.total }})
            </span>
          </h4>

          <!-- 已完成的成就 -->
          <div v-if="getGroupCompleteAdvancements(group.category).length > 0" class="mb-3">
            <div class="text-xs text-green-600 dark:text-green-400 mb-1 flex items-center">
              {{ translate('players', 'advancements.completed') }}
            </div>
            <div class="flex flex-wrap gap-x-4 gap-y-7 adv-item-container">
              <div
                v-for="adv in getGroupCompleteAdvancements(group.category)"
                :key="adv.key"
                class="hover:scale-105 adv-item flex items-center py-1 px-2 rounded transition-colors cursor-pointer"
                @click="openPopup(adv, $event)"
              >
                <!-- 成就图标框和图标 -->
                <div class="absolute w-12 h-12 mr-2 translate-y-2">
                  <!-- 图标框 -->
                  <img
                    :src="advancementService.getAdvancementFramePath(adv)"
                    :alt="`${advancementService.translateAdvancement(adv)} frame`"
                    class="absolute -translate-x-2 -translate-y-2 inset-0 w-full h-full object-contain z-0"
                  />
                  <!-- 成就图标 -->
                  <img
                    :src="advancementService.getAdvancementImagePath(adv)"
                    :alt="advancementService.translateAdvancement(adv)"
                    class="absolute w-8 h-8 mr-2 object-contain rounded z-1"
                  />
                </div>
                <span class="ml-12 text-l text-zinc-600 dark:text-zinc-400">
                  {{ advancementService.translateAdvancement(adv) }}
                </span>
                <!-- 完成状态 -->
                <span class="ml-auto text-xs text-green-600 dark:text-green-400">
                  {{ adv.completed.length }} {{ adv.remaining.length > 0 ? ':' : '/' }}
                  {{ adv.completed.length + adv.remaining.length }}
                </span>
              </div>
            </div>
          </div>

          <div
            v-if="showIncomplete && getGroupIncompleteAdvancements(group.category).length > 0"
            class="mt-3 pt-3 border-t border-zinc-200 dark:border-zinc-700"
          >
            <div class="text-xs text-red-600 dark:text-red-400 mb-1 flex items-center">
              {{ translate('players', 'advancements.not-completed') }}
            </div>
            <div class="flex flex-wrap gap-x-4 gap-y-2 adv-item-container">
              <div
                v-for="adv in getGroupIncompleteAdvancements(group.category)"
                :key="adv.key"
                class="hover:scale-105 adv-item flex items-center py-1 px-2 rounded transition-colors cursor-pointer"
                @click="openPopup(adv, $event)"
              >
                <!-- 成就图标框和图标 -->
                <div class="absolute w-12 h-12 mr-2 translate-y-2">
                  <!-- 图标框 -->
                  <img
                    :src="advancementService.getAdvancementFramePath(adv)"
                    :alt="`${advancementService.translateAdvancement(adv)} frame`"
                    class="absolute -translate-x-2 -translate-y-2 inset-0 w-full h-full object-contain z-0"
                  />
                  <!-- 成就图标 -->
                  <img
                    :src="advancementService.getAdvancementImagePath(adv)"
                    :alt="advancementService.translateAdvancement(adv)"
                    class="absolute w-8 h-8 mr-2 object-contain rounded z-1"
                  />
                </div>
                <span class="ml-12 text-l text-zinc-600 dark:text-zinc-400">
                  {{ advancementService.translateAdvancement(adv) }}
                </span>
                <!-- 完成状态 -->
                <span class="ml-auto text-xs text-red-600 dark:text-red-400">
                  {{ adv.completed.length }}/{{ adv.completed.length + adv.remaining.length }}
                </span>
              </div>
            </div>
          </div>

          <div
            v-if="!showIncomplete && getGroupIncompleteAdvancements(group.category).length > 0"
            class="mt-3 pt-3 border-t border-zinc-200 dark:border-zinc-700"
          >
            <button
              @click="toggleShowIncomplete"
              class="w-full text-xs text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 flex items-center justify-center gap-1 py-1"
            >
              <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M19 9l-7 7-7-7"
                />
              </svg>
              {{ getGroupIncompleteAdvancements(group.category).length }}
              {{ translate('players', 'advancements.show-incomplete') }}
            </button>
          </div>

          <!-- 无数据提示 -->
          <div
            v-if="
              advancementService.getAdvancementsByGroup(
                playerAdvancementList.advancements,
                group.category,
              ).length === 0
            "
            class="text-sm text-gray-500 dark:text-gray-400 italic"
          >
            {{ translate('players', 'advancements.no-data') }}
          </div>
        </div>
      </div>

      <!-- 其他未分组成就 -->
      <div v-if="ungroupedAdvancements.length > 0 && showUngrouped" class="mt-2">
        <h4 class="font-semibold text-sm text-zinc-600 dark:text-zinc-300 mb-3 flex items-center">
          <span class="w-2 h-2 rounded-full bg-gray-500 mr-2"></span>
          {{ translate('players', 'advancements.other') }}
        </h4>

        <div
          class="bg-zinc-300/50 dark:bg-zinc-600/50 rounded-lg p-3 border border-zinc-200 dark:border-zinc-700"
        >
          <!-- 已完成的成就 -->
          <div v-if="ungroupedCompleteAdvancements.length > 0" class="mb-3">
            <div class="text-xs text-green-600 dark:text-green-400 mb-1 flex items-center">
              {{ translate('players', 'advancements.completed') }}
            </div>
            <div class="flex flex-wrap gap-x-4 gap-y-2 adv-item-container">
              <div
                v-for="adv in ungroupedCompleteAdvancements"
                :key="adv.key"
                class="hover:scale-105 adv-item flex items-center py-1 px-2 rounded transition-colors cursor-pointer"
                @click="openPopup(adv, $event)"
              >
                <!-- 成就图标框和图标 -->
                <div class="absolute w-12 h-12 mr-2 translate-y-2">
                  <!-- 图标框 -->
                  <img
                    :src="advancementService.getAdvancementFramePath(adv)"
                    :alt="`${advancementService.translateAdvancement(adv)} frame`"
                    class="absolute -translate-x-2 -translate-y-2 inset-0 w-full h-full object-contain z-0"
                  />
                  <!-- 成就图标 -->
                  <img
                    :src="advancementService.getAdvancementImagePath(adv)"
                    :alt="advancementService.translateAdvancement(adv)"
                    class="absolute w-8 h-8 mr-2 object-contain rounded z-1"
                  />
                </div>
                <div class="flex-1">
                  <span class="text-l text-zinc-600 dark:text-zinc-400">
                    {{ advancementService.translateAdvancement(adv) }}
                  </span>
                </div>
                <!-- 完成状态 -->
                <span class="ml-auto text-xs text-green-600 dark:text-green-400">
                  {{ adv.completed.length }}/{{ adv.completed.length + adv.remaining.length }}
                </span>
              </div>
            </div>
          </div>

          <!-- 未完成的成就（根据 showIncomplete 状态决定是否显示） -->
          <div
            v-if="showIncomplete && ungroupedIncompleteAdvancements.length > 0"
            class="mt-3 pt-3 border-t border-zinc-200 dark:border-zinc-700"
          >
            <div class="text-xs text-red-600 dark:text-red-400 mb-1 flex items-center">
              {{ translate('players', 'advancements.not-completed') }}
            </div>
            <div class="flex flex-wrap gap-x-4 gap-y-2 adv-item-container">
              <div
                v-for="adv in ungroupedIncompleteAdvancements"
                :key="adv.key"
                class="hover:scale-105 adv-item flex items-center py-1 px-2 rounded transition-colors cursor-pointer"
                @click="openPopup(adv, $event)"
              >
                <!-- 成就图标框和图标 -->
                <div class="absolute w-12 h-12 mr-2 translate-y-2">
                  <!-- 图标框 -->
                  <img
                    :src="advancementService.getAdvancementFramePath(adv)"
                    :alt="`${advancementService.translateAdvancement(adv)} frame`"
                    class="absolute -translate-x-2 -translate-y-2 inset-0 w-full h-full object-contain z-0"
                  />
                  <!-- 成就图标 -->
                  <img
                    :src="advancementService.getAdvancementImagePath(adv)"
                    :alt="advancementService.translateAdvancement(adv)"
                    class="absolute w-8 h-8 mr-2 object-contain rounded z-1"
                  />
                </div>
                <div class="flex-1">
                  <span class="text-l text-zinc-600 dark:text-zinc-400">
                    {{ advancementService.translateAdvancement(adv) }}
                  </span>
                </div>
                <!-- 完成状态 -->
                <span class="ml-auto text-xs text-red-600 dark:text-red-400">
                  {{ adv.completed.length }}/{{ adv.completed.length + adv.remaining.length }}
                </span>
              </div>
            </div>
          </div>

          <!-- 显示未完成成就提示 -->
          <div
            v-if="!showIncomplete && ungroupedIncompleteAdvancements.length > 0"
            class="mt-3 pt-3 border-t border-zinc-200 dark:border-zinc-700"
          >
            <button
              @click="toggleShowIncomplete"
              class="w-full text-xs text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 flex items-center justify-center gap-1 py-1"
            >
              <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M19 9l-7 7-7-7"
                />
              </svg>
              {{ ungroupedIncompleteAdvancements.length }}
              {{ translate('players', 'advancements.incomplete-hidden') }}
            </button>
          </div>
        </div>
      </div>

      <!-- 无数据提示 -->
      <div
        v-if="filteredAdvancements.length === 0"
        class="text-center text-sm text-gray-500 dark:text-gray-400 py-4 italic"
      >
        {{ translate('players', 'advancements.no-data') }}
      </div>
    </div>

    <!-- 引入封装后的弹窗组件，填充业务内容 -->
    <DraggablePopup
      :visible="popupState.visible"
      :init-x="popupState.initX"
      :init-y="popupState.initY"
      width="320px"
      max-height="400px"
      @close="closePopup"
    >
      <template #content="{ handleDragStart }">
        <div
          class="p-3 border-b border-zinc-200 dark:border-zinc-700 cursor-move shrink-0"
          @mousedown="handleDragStart"
          @touchstart="handleDragStart"
        >
          <div class="flex items-center justify-between">
            <div class="flex items-center flex-1 min-w-0">
              <img
                v-if="popupState.currentAdvancement"
                :src="advancementService.getAdvancementImagePath(popupState.currentAdvancement)"
                :alt="advancementService.translateAdvancement(popupState.currentAdvancement)"
                class="w-10 h-10 mr-2 rounded z-1"
              />
              <div class="min-w-0 flex-1" v-if="popupState.currentAdvancement">
                <h5 class="font-semibold text-sm text-zinc-800 dark:text-white truncate">
                  {{ advancementService.translateAdvancement(popupState.currentAdvancement) }}
                </h5>
                <div class="text-xs text-zinc-500 dark:text-zinc-400">
                  {{ popupState.currentAdvancement.completed.length }} /
                  {{
                    popupState.currentAdvancement.completed.length +
                    popupState.currentAdvancement.remaining.length
                  }}
                  {{ translate('players', 'advancements.completion-items') }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="flex-1 overflow-y-scroll no-scrollbar p-0" v-if="popupState.currentAdvancement">
          <div
            v-if="
              popupState.currentAdvancement.remaining.length > 0 &&
              !popupState.currentAdvancement.done
            "
            class="p-3"
          >
            <h6 class="text-xs font-medium text-red-600 dark:text-red-400 mb-2 flex items-center">
              <span class="mr-1"></span>
              {{ translate('players', 'advancements.remaining-items') }}
            </h6>
            <ul class="space-y-1">
              <li
                v-for="(item, index) in popupState.currentAdvancement.remaining"
                :key="`remaining-${index}`"
                class="text-xs text-zinc-700 dark:text-zinc-300 px-2 py-1 bg-red-50 dark:bg-red-900/20 rounded"
              >
                {{ translate('players', item) }}
              </li>
            </ul>
          </div>
          <div v-if="popupState.currentAdvancement.completed.length > 0" class="p-3">
            <h6
              class="text-xs font-medium text-green-600 dark:text-green-400 mb-2 flex items-center"
            >
              <span class="mr-1"></span>
              {{ translate('players', 'advancements.completed-items') }}
            </h6>
            <ul class="space-y-1">
              <li
                v-for="(item, index) in popupState.currentAdvancement.completed"
                :key="`completed-${index}`"
                class="text-xs text-zinc-700 dark:text-zinc-300 px-2 py-1 bg-green-50 dark:bg-green-900/20 rounded"
              >
                {{ translate('players', item) }}
              </li>
            </ul>
          </div>
        </div>
      </template>
    </DraggablePopup>
  </div>
</template>
