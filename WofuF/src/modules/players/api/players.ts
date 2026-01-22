import http from '../../../shared/api/http.ts'
import type { RequestOptions } from '../../../shared/types/RequestOptions.ts'
import type {
  ApiResponse,
  PlayerStatisticsData,
  PlayerAdvancementsData,
  PlayerProfileList,
  PlayerNameList,
} from '@/modules/players/types/player.ts'

/* ---------------- 随机玩家 ---------------- */

export function getRandomPlayerProfile(params?: {limit?: number},options?: RequestOptions) {
  return http.get<ApiResponse<PlayerProfileList>>(
    '/v1/players/random-profile',
    { signal: options?.signal, params },
  )
}

/* ---------------- 昨日在线玩家 ---------------- */

export function getPlayerYesterdayOnline(options?: RequestOptions) {
  return http.get<ApiResponse<PlayerNameList>>('/v1/players/yesterday', {
    signal: options?.signal,
  })
}

/* ---------------- 玩家统计 ---------------- */

export function getPlayerStatistics(playerName: string, options?: RequestOptions) {
  return http.get<ApiResponse<PlayerStatisticsData>>(`/v1/players/statistics/${playerName}`, {
    signal: options?.signal,
  })
}

/* ---------------- 玩家成就 ---------------- */

export function getPlayerAdvancements(playerName: string, options?: RequestOptions) {
  return http.get<ApiResponse<PlayerAdvancementsData>>(
    `/v1/players/advancements/${playerName}`,
    {
      signal: options?.signal,
    },
  )
}
