import type { RequestOptions } from '@SU/async/RequestOptions.ts'
import type { Player } from '@M/players/dtos/Player.ts'
import type { PlayerSkin } from '@M/players/dtos/PlayerSkin.ts'
import type { PlayerUuid } from '@M/players/dtos/PlayerUuid.ts'
import type { PlayerNameList } from '@M/players/dtos/PlayerName.ts'
import type { ApiResponse } from '@S/infra/api/v1/models/ApiResponse.ts'
import { Result } from '@S/core/Result.ts'
import { http } from '@S/infra/api/http.ts'

export interface IPlayerService {
  /* ---------------- 随机玩家 ---------------- */
  getRandomPlayerProfile(
    params?: { limit?: number },
    options?: RequestOptions,
  ): Promise<Result<PlayerNameList>>
  /* ---------------- 获取玩家信息 ---------------- */
  getPlayerProfile(playerNameOrUuid: string, options?: RequestOptions): Promise<Result<Player>>
  /* ---------------- 昨日在线玩家 ---------------- */
  getPlayerYesterdayOnline(options?: RequestOptions): Promise<Result<PlayerNameList>>
}

export class PlayerService implements IPlayerService {
  /* ---------------- 随机玩家 ---------------- */
  public async getRandomPlayerProfile(
    params?: { limit?: number },
    options?: RequestOptions,
  ): Promise<Result<PlayerNameList>> {
    const response = await http.get<ApiResponse<PlayerNameList>>('/v1/players/random-profile', {
      signal: options?.signal,
      params,
    })
    if (response.data.success) {
      return Result.success<PlayerNameList>(response.data.data)
    }
    return Result.failure<PlayerNameList>(response.data.message)
  }

  /* ---------------- 获取玩家信息 ---------------- */
  public async getPlayerProfile(
    playerNameOrUuid: string,
    options?: RequestOptions,
  ): Promise<Result<Player>> {
    const response = await http.get<ApiResponse<Player>>(`/api/v1/players/profile/${playerNameOrUuid}`, {
      signal: options?.signal,
    })
    if (response.data.success) {
      return Result.success<Player>(response.data.data)
    }
    return Result.failure<Player>(response.data.message)
  }

  /* ---------------- 昨日在线玩家 ---------------- */
  public async getPlayerYesterdayOnline(options?: RequestOptions): Promise<Result<PlayerNameList>> {
    const response = await http.get<ApiResponse<PlayerNameList>>('/api/v1/players/yesterday', {
      signal: options?.signal,
    })
    if (response.data.success) {
      return Result.success<PlayerNameList>(response.data.data)
    }
    return Result.failure<PlayerNameList>(response.data.message)
  }



  /* ---------------- 获取玩家皮肤 ---------------- */
  public async getPlayerSkin(
    playerUuid: PlayerUuid,
    options?: RequestOptions,
  ): Promise<Result<PlayerSkin>> {
    const response = await http.get<ApiResponse<PlayerSkin>>(
      `/api/v1/players/skins/${playerUuid}`,
      {
        signal: options?.signal,
      },
    )
    if (response.data.success) {
      return Result.success<PlayerSkin>(response.data.data)
    }
    return Result.failure<PlayerSkin>(response.data.message)
  }
}
