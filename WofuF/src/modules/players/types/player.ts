export interface ApiResponse<T> {
  success: boolean
  code: string
  data: T
  message: string
}

export interface PlayerProfile {
  id: string
  name: string
  firstLogin: number
  lastLogin: number
  totalPlaytimeSeconds: number
  updateTime: number
}

export interface PlayerName {
  name : string
}

export interface PlayerProfileList {
  data: PlayerProfile[]
}

export interface PlayerNameList {
  data: PlayerName[]
}

export interface PlayerStatistic {
  category: string
  key: string
  value: number
}

export interface PlayerStatisticsData {
  statistics: Record<string, PlayerStatistic>
}

export interface PlayerAdvancement {
  key: string
  done: boolean
  completed: string[]
  remaining: string[]
}

export interface PlayerAdvancementsData {
  advancements: PlayerAdvancement[]
}
