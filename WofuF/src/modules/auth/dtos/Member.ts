/**
 * 论坛成员数据传输对象
 */

import type { PermissionPoint } from '@M/forum/admin/dtos/Admin.ts'

// 成员信息
export interface Member {
  memberId: string
  userId: string
  playerId: string
  nickname: string
  reputation: number
  permissions?: PermissionPoint[]
  isAdminUser?: boolean
  adminUser?: boolean  // API 返回的字段名
  isBanned?: boolean
  banned?: boolean  // API 返回的字段名
  bannedAt?: number | null
  bannedUntil?: number | null
  bannedReason?: string | null
}

// 创建成员请求
export interface CreateMemberRequest {
  playerId: string
  nickName: string
  lastPlayed: string
  code: string
}

// 创建成员响应 (无返回数据)
export type CreateMemberResponse = void

// 获取当前成员响应
export interface GetCurrentMemberResponse {
  memberId: string
  userId: string
  playerId: string
  nickname: string
  reputation: number
  permissions?: PermissionPoint[]
  isAdminUser?: boolean
  adminUser?: boolean  // API 返回的字段名
  isBanned?: boolean
  banned?: boolean  // API 返回的字段名
  bannedAt?: number | null
  bannedUntil?: number | null
  bannedReason?: string | null
}

// 成员详情 (用于显示)
export interface MemberDetails {
  nickname: string
  reputation: number
}
