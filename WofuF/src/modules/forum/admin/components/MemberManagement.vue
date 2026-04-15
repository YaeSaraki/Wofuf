<script lang="ts" setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useToast } from 'primevue/usetoast'
import { adminService } from '@M/forum/admin/services/AdminService.ts'
import { PlayerService } from '@M/players/services/PlayerService.ts'
import type { MemberSummary, MemberProfile, PermissionPoint } from '@M/forum/admin/dtos/Admin.ts'
import { translate } from '@S/services/i18n'

const toast = useToast()
const playerService = new PlayerService()

// ==================== 状态 ====================
const members = ref<MemberSummary[]>([])
const totalMembers = ref(0)
const currentPage = ref(0)
const pageSize = ref(20)
const nicknameSearch = ref('')
const isLoading = ref(false)
const errorMsg = ref('')

// 查看中的成员资料
const viewingMember = ref<MemberProfile | null>(null)
const isLoadingProfile = ref(false)

// 成员头像缓存（使用普通对象而非Map以保证Vue3响应式）
const memberAvatars = ref<Record<string, string>>({})
const profileAvatar = ref<string | null>(null)

// 列表头像加载
const listAvatars = ref<Record<string, string>>({})

// 可授予的权限列表（排除系统管理权限）
const grantablePermissions: PermissionPoint[] = [
  'POST_PIN',
  'POST_FEATURE',
  'POST_HIDE',
  'POST_REVIEW',
  'POST_DELETE_ANY',
  'COMMENT_DELETE_ANY',
  'COMMENT_VIEW_HIDDEN',
  'CATEGORY_MANAGE',
  'USER_BAN',
  'USER_VIEW_BANNED',
  'VIEW_MEMBER_PROFILES',
]

// ==================== 计算属性 ====================
const totalPages = computed(() => Math.ceil(totalMembers.value / pageSize.value))

// 未授予的权限（可用于授予）
const availableToGrant = computed(() => {
  if (!viewingMember.value) return []
  const currentPerms = viewingMember.value.permissions
  return grantablePermissions.filter(p => !currentPerms.includes(p))
})

// ==================== 辅助函数 ====================

// 加载玩家头像
async function loadAvatar(playerId: string, cacheKey: string) {
  if (cacheKey in memberAvatars.value) return memberAvatars.value[cacheKey]

  try {
    const result = await playerService.getPlayerSkin(playerId)
    if (result.isSuccess) {
      const skinData = result.getValue()
      if (skinData?.skin) {
        const avatar = await playerService.renderAvatar(skinData.skin, 40)
        memberAvatars.value[cacheKey] = avatar
        return avatar
      }
    }
  } catch (e) {
    console.warn('[MemberManagement] Failed to load avatar:', e)
  }
  return null
}

// 加载成员头像
async function loadMemberAvatar(playerId: string, memberId: string) {
  const key = `member_${memberId}`
  if (key in listAvatars.value) return listAvatars.value[key]

  try {
    const result = await playerService.getPlayerSkin(playerId)
    if (result.isSuccess) {
      const skinData = result.getValue()
      if (skinData?.skin) {
        const avatar = await playerService.renderAvatar(skinData.skin, 40)
        listAvatars.value[key] = avatar
        return avatar
      }
    }
  } catch (e) {
    console.warn('[MemberManagement] Failed to load member avatar:', e)
  }
  return null
}

// 获取成员头像URL
function getMemberAvatarUrl(member: MemberSummary): string | null {
  const key = `member_${member.memberId}`
  return listAvatars.value[key] || null
}

// 初始化加载列表头像
async function loadListAvatars() {
  for (const member of members.value) {
    await loadMemberAvatar(member.playerId, member.memberId)
  }
}

// 获取权限颜色
function getPermissionColor(permission: string): string {
  const colorMap: Record<string, string> = {
    POST_PIN: 'bf-perm-blue',
    POST_FEATURE: 'bf-perm-purple',
    POST_HIDE: 'bf-perm-red',
    POST_REVIEW: 'bf-perm-yellow',
    POST_DELETE_ANY: 'bf-perm-red-dark',
    COMMENT_DELETE_ANY: 'bf-perm-orange',
    COMMENT_VIEW_HIDDEN: 'bf-perm-amber',
    CATEGORY_MANAGE: 'bf-perm-cyan',
    USER_BAN: 'bf-perm-rose',
    USER_VIEW_BANNED: 'bf-perm-pink',
    VIEW_MEMBER_PROFILES: 'bf-perm-teal',
    ADMIN_ACCESS: 'bf-perm-emerald',
    PERMISSION_GRANT: 'bf-perm-indigo',
  }
  return colorMap[permission] || 'bf-perm-gray'
}

// 获取权限显示名称
function getPermissionName(permission: string): string {
  const nameMap: Record<string, string> = {
    POST_PIN: translate('forum', 'admin.perm.pin'),
    POST_FEATURE: translate('forum', 'admin.perm.feature'),
    POST_HIDE: translate('forum', 'admin.perm.hide'),
    POST_REVIEW: translate('forum', 'admin.perm.review'),
    POST_DELETE_ANY: translate('forum', 'admin.perm.deletePost'),
    COMMENT_DELETE_ANY: translate('forum', 'admin.perm.deleteComment'),
    COMMENT_VIEW_HIDDEN: translate('forum', 'admin.perm.viewHidden'),
    CATEGORY_MANAGE: translate('forum', 'admin.perm.manageCategory'),
    USER_BAN: translate('forum', 'admin.perm.banUser'),
    USER_VIEW_BANNED: translate('forum', 'admin.perm.viewBanned'),
    VIEW_MEMBER_PROFILES: translate('forum', 'admin.perm.viewProfile'),
    ADMIN_ACCESS: translate('forum', 'admin.perm.adminAccess'),
    PERMISSION_GRANT: translate('forum', 'admin.perm.grantPermission'),
  }
  return nameMap[permission] || permission
}

// 格式化时间戳
function formatDateTime(dateStr: string | null): string {
  if (!dateStr) return '-'
  try {
    const date = new Date(dateStr)
    return date.toLocaleString()
  } catch {
    return dateStr
  }
}

// 格式化时间相对描述
function formatTimeAgo(dateStr: string | null): string {
  if (!dateStr) return '-'
  try {
    const date = new Date(dateStr)
    const now = new Date()
    const diffMs = now.getTime() - date.getTime()
    const diffMins = Math.floor(diffMs / 60000)
    const diffHours = Math.floor(diffMs / 3600000)
    const diffDays = Math.floor(diffMs / 86400000)

    if (diffMins < 1) return translate('forum', 'time.justNow')
    if (diffMins < 60) return `${diffMins} ${translate('forum', 'time.minutesAgo')}`
    if (diffHours < 24) return `${diffHours} ${translate('forum', 'time.hoursAgo')}`
    if (diffDays < 7) return `${diffDays} ${translate('forum', 'time.daysAgo')}`

    return date.toLocaleDateString()
  } catch {
    return dateStr
  }
}

// 跳转到帖子
function goToPost(slug: string) {
  if (!slug) return
  sessionStorage.setItem('forum_post_from', '/forum/admin')
  window.location.href = `/forum/posts/${slug}`
}

// ==================== 数据加载 ====================
async function loadMembers() {
  isLoading.value = true
  errorMsg.value = ''

  try {
    const result = await adminService.getMembersList(
      nicknameSearch.value.trim() || undefined,
      currentPage.value,
      pageSize.value
    )

    if (result.isSuccess) {
      const data = result.getValue()
      members.value = data.members
      totalMembers.value = data.total
      // 加载头像
      loadListAvatars()
    } else {
      errorMsg.value = String(result.error) || translate('forum', 'error')
    }
  } catch (e) {
    errorMsg.value = translate('forum', 'error')
  } finally {
    isLoading.value = false
  }
}

async function loadMemberProfile(memberId: string) {
  isLoadingProfile.value = true
  profileAvatar.value = null

  try {
    const result = await adminService.getMemberProfile(memberId, 0, 10)
    if (result.isSuccess) {
      viewingMember.value = result.getValue()
      // 加载玩家头像
      loadProfileAvatar(result.getValue().playerId)
    } else {
      errorMsg.value = String(result.error) || translate('forum', 'error')
    }
  } catch (e) {
    errorMsg.value = translate('forum', 'error')
  } finally {
    isLoadingProfile.value = false
  }
}

async function loadProfileAvatar(playerId: string) {
  try {
    const result = await playerService.getPlayerSkin(playerId)
    if (result.isSuccess) {
      const skinData = result.getValue()
      if (skinData?.skin) {
        profileAvatar.value = await playerService.renderAvatar(skinData.skin, 64)
      }
    }
  } catch (e) {
    console.warn('[MemberManagement] Failed to load profile avatar:', e)
  }
}

// ==================== 成员操作 ====================
const isOperating = ref(false)

async function banMember() {
  if (!viewingMember.value) return

  isOperating.value = true
  try {
    const result = await adminService.banMember(viewingMember.value.memberId, '违反社区准则')
    if (result.isSuccess) {
      toast.add({ severity: 'success', summary: '封禁成功', life: 3000 })
      await loadMemberProfile(viewingMember.value.memberId)
      await loadMembers()
    } else {
      toast.add({ severity: 'error', summary: translate('forum', 'admin.operationFailed'), detail: String(result.error), life: 5000 })
    }
  } catch (e) {
    toast.add({ severity: 'error', summary: translate('forum', 'admin.operationFailed'), detail: String(e), life: 5000 })
  } finally {
    isOperating.value = false
  }
}

async function unbanMember() {
  if (!viewingMember.value) return

  isOperating.value = true
  try {
    const result = await adminService.unbanMember(viewingMember.value.memberId)
    if (result.isSuccess) {
      toast.add({ severity: 'success', summary: '解封成功', life: 3000 })
      await loadMemberProfile(viewingMember.value.memberId)
      await loadMembers()
    } else {
      toast.add({ severity: 'error', summary: translate('forum', 'admin.operationFailed'), detail: String(result.error), life: 5000 })
    }
  } catch (e) {
    toast.add({ severity: 'error', summary: translate('forum', 'admin.operationFailed'), detail: String(e), life: 5000 })
  } finally {
    isOperating.value = false
  }
}

async function revokePermission(permission: PermissionPoint) {
  if (!viewingMember.value) return

  isOperating.value = true
  try {
    const result = await adminService.revokePermission(viewingMember.value.memberId, permission)
    if (result.isSuccess) {
      toast.add({ severity: 'success', summary: '权限撤销成功', life: 3000 })
      await loadMemberProfile(viewingMember.value.memberId)
    } else {
      toast.add({ severity: 'error', summary: translate('forum', 'admin.operationFailed'), detail: String(result.error), life: 5000 })
    }
  } catch (e) {
    toast.add({ severity: 'error', summary: translate('forum', 'admin.operationFailed'), detail: String(e), life: 5000 })
  } finally {
    isOperating.value = false
  }
}

async function grantPermission(permission: PermissionPoint) {
  if (!viewingMember.value) return

  isOperating.value = true
  try {
    const result = await adminService.grantPermission(viewingMember.value.memberId, permission)
    if (result.isSuccess) {
      toast.add({ severity: 'success', summary: '权限授予成功', life: 3000 })
      await loadMemberProfile(viewingMember.value.memberId)
    } else {
      toast.add({ severity: 'error', summary: translate('forum', 'admin.operationFailed'), detail: String(result.error), life: 5000 })
    }
  } catch (e) {
    toast.add({ severity: 'error', summary: translate('forum', 'admin.operationFailed'), detail: String(e), life: 5000 })
  } finally {
    isOperating.value = false
  }
}

// ==================== 分页 ====================
function prevPage() {
  if (currentPage.value > 0) {
    currentPage.value--
    loadMembers()
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++
    loadMembers()
  }
}

// 搜索防抖
let searchTimeout: ReturnType<typeof setTimeout> | null = null
function onSearchInput() {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    currentPage.value = 0
    loadMembers()
  }, 300)
}

// 点击成员查看详情
function viewMember(member: MemberSummary) {
  loadMemberProfile(member.memberId)
}

// 关闭详情面板
function closeProfile() {
  viewingMember.value = null
  profileAvatar.value = null
}

// ==================== 生命周期 ====================
onMounted(() => {
  loadMembers()
})
</script>

<template>
  <div class="admin-container">
    <!-- 页面标题栏 -->
    <div class="admin-header">
      <div class="admin-header__title">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
          <circle cx="9" cy="7" r="4"/>
          <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
          <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
        </svg>
        <h1>{{ translate('forum', 'admin.membersManagement') }}</h1>
      </div>
      <span class="admin-header__count">{{ totalMembers }} {{ translate('forum', 'admin.members') || '成员' }}</span>
    </div>

    <!-- 搜索栏 -->
    <div class="admin-toolbar">
      <div class="admin-search">
        <svg class="admin-search__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8" />
          <path d="M21 21l-4.35-4.35" />
        </svg>
        <input
          v-model="nicknameSearch"
          type="text"
          :placeholder="translate('forum', 'searchByNickname') || '按昵称搜索...'"
          class="admin-search__input"
          @input="onSearchInput"
        />
      </div>
      <button class="admin-btn admin-btn--secondary" @click="loadMembers" :disabled="isLoading">
        <svg class="admin-btn__icon" :class="{ spinning: isLoading }" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M23 4v6h-6M1 20v-6h6" />
          <path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15" />
        </svg>
        {{ translate('forum', 'refresh') }}
      </button>
    </div>

    <!-- 错误提示 -->
    <div v-if="errorMsg" class="admin-error">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
      </svg>
      <span>{{ errorMsg }}</span>
      <button class="admin-btn admin-btn--ghost" @click="errorMsg = ''">×</button>
    </div>

    <!-- 加载状态 -->
    <div v-if="isLoading && members.length === 0" class="admin-loading">
      <div class="admin-loading__spinner"></div>
      <span>{{ translate('forum', 'loading') }}</span>
    </div>

    <!-- 主内容区：左侧成员列表 + 右侧详情面板 -->
    <div v-else class="member-layout">
      <!-- 成员列表 -->
      <div class="member-list-panel">
        <!-- 空状态 -->
        <div v-if="!isLoading && members.length === 0" class="admin-empty">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z"/>
          </svg>
          <h3>暂无成员</h3>
        </div>

        <!-- 成员卡片列表 -->
        <div v-else class="admin-list">
          <div
            v-for="member in members"
            :key="member.memberId"
            class="member-card"
            :class="{
              'member-card--banned': member.isBanned,
              'member-card--active': viewingMember?.memberId === member.memberId
            }"
            @click="viewMember(member)"
          >
            <div class="member-card__avatar">
              <img v-if="getMemberAvatarUrl(member)" :src="getMemberAvatarUrl(member)!" :alt="member.nickname" class="avatar-img-small" />
              <span v-else>{{ member.nickname?.charAt(0)?.toUpperCase() || '?' }}</span>
            </div>
            <div class="member-card__info">
              <div class="member-card__header">
                <span class="member-card__nickname">{{ member.nickname }}</span>
                <span v-if="member.isBanned" class="member-card__badge">已封禁</span>
              </div>
              <div class="member-card__meta">
                <span>{{ translate('forum', 'points') }}: {{ member.reputation }}</span>
                <span v-if="member.permissions.length > 0" class="member-card__perm-count">
                  {{ member.permissions.length }} 个权限
                </span>
              </div>
            </div>
            <div class="member-card__arrow">›</div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="totalPages > 1" class="admin-pagination">
          <button class="admin-btn admin-btn--secondary" :disabled="currentPage === 0" @click="prevPage">
            {{ translate('forum', 'prevPage') }}
          </button>
          <span class="admin-pagination__info">{{ currentPage + 1 }} / {{ totalPages }}</span>
          <button class="admin-btn admin-btn--secondary" :disabled="currentPage >= totalPages - 1" @click="nextPage">
            {{ translate('forum', 'nextPage') }}
          </button>
        </div>
      </div>

      <!-- 详情面板 -->
      <div class="member-detail-panel" :class="{ 'member-detail-panel--open': viewingMember }">
        <!-- 加载状态 -->
        <div v-if="isLoadingProfile" class="admin-loading">
          <div class="admin-loading__spinner"></div>
        </div>

        <!-- 成员资料 -->
        <template v-else-if="viewingMember">
          <div class="detail-header">
            <h2 class="detail-title">成员详情</h2>
            <button class="admin-btn admin-btn--ghost" @click="closeProfile">×</button>
          </div>

          <!-- 基本信息 -->
          <div class="member-profile-card">
            <div class="member-profile-header">
              <div class="member-profile-avatar">
                <img v-if="profileAvatar" :src="profileAvatar" alt="avatar" class="avatar-img" />
                <span v-else>{{ viewingMember.nickname?.charAt(0)?.toUpperCase() || '?' }}</span>
              </div>
              <div class="member-profile-info">
                <h3 class="member-profile-nickname">{{ viewingMember.nickname }}</h3>
                <div class="member-profile-badges">
                  <span v-if="viewingMember.isBanned" class="member-badge member-badge--banned">◉ 已封禁</span>
                  <span class="member-badge member-badge--reputation">{{ translate('forum', 'points') }}: {{ viewingMember.reputation }}</span>
                </div>
              </div>
            </div>

            <div class="member-profile-details">
              <div class="member-detail-item">
                <span class="member-detail-label">Member ID</span>
                <span class="member-detail-value">{{ viewingMember.memberId }}</span>
              </div>
              <div class="member-detail-item">
                <span class="member-detail-label">User ID</span>
                <span class="member-detail-value">{{ viewingMember.userId }}</span>
              </div>
              <div class="member-detail-item">
                <span class="member-detail-label">Player ID</span>
                <span class="member-detail-value">{{ viewingMember.playerId }}</span>
              </div>
              <div v-if="viewingMember.isBanned" class="member-detail-item">
                <span class="member-detail-label">封禁时间</span>
                <span class="member-detail-value">{{ formatDateTime(viewingMember.bannedAt) }}</span>
              </div>
              <div v-if="viewingMember.isBanned" class="member-detail-item">
                <span class="member-detail-label">解封时间</span>
                <span class="member-detail-value">{{ formatDateTime(viewingMember.bannedUntil) }}</span>
              </div>
              <div v-if="viewingMember.isBanned && viewingMember.bannedReason" class="member-detail-item">
                <span class="member-detail-label">封禁原因</span>
                <span class="member-detail-value">{{ viewingMember.bannedReason }}</span>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="member-profile-actions">
              <button
                v-if="viewingMember.isBanned"
                class="admin-btn admin-btn--success"
                @click="unbanMember"
                :disabled="isOperating"
              >
                <span class="admin-btn__symbol">⊙</span> 解封用户
              </button>
              <button
                v-else
                class="admin-btn admin-btn--danger"
                @click="banMember"
                :disabled="isOperating"
              >
                <span class="admin-btn__symbol">⊘</span> 封禁用户
              </button>
            </div>
          </div>

          <!-- 权限列表 -->
          <div class="member-permissions-card">
            <h4 class="member-section-title">权限列表</h4>
            <div class="member-permissions-list">
              <div v-for="permission in viewingMember.permissions" :key="permission" class="member-permission-item">
                <span class="bf-permission-tag" :class="getPermissionColor(permission)">
                  {{ getPermissionName(permission) }}
                </span>
                <button
                  class="admin-btn admin-btn--small admin-btn--danger"
                  @click="revokePermission(permission as PermissionPoint)"
                  :disabled="isOperating"
                  title="撤销权限"
                >×</button>
              </div>
              <div v-if="viewingMember.permissions.length === 0" class="member-empty-text">暂无权限</div>
            </div>

            <!-- 授予权限 -->
            <div v-if="availableToGrant.length > 0" class="member-grant-section">
              <h5 class="member-grant-title">授予权限</h5>
              <div class="member-permissions-list">
                <div v-for="permission in availableToGrant" :key="permission" class="member-permission-item">
                  <span class="bf-permission-tag bf-permission-tag--outline" :class="getPermissionColor(permission)">
                    {{ getPermissionName(permission) }}
                  </span>
                  <button
                    class="admin-btn admin-btn--small admin-btn--success"
                    @click="grantPermission(permission)"
                    :disabled="isOperating"
                    title="授予权限"
                  >+</button>
                </div>
              </div>
            </div>
          </div>

          <!-- 发帖历史 -->
          <div class="member-posts-card">
            <h4 class="member-section-title">
              发帖历史 <span class="member-section-count">({{ viewingMember.totalPosts }})</span>
            </h4>

            <div v-if="viewingMember.postHistory.length === 0" class="member-empty-text">暂无发帖</div>

            <div v-else class="admin-list">
              <div
                v-for="post in viewingMember.postHistory"
                :key="post.postId"
                class="admin-card"
                :class="{
                  'admin-card--hidden': post.status === 'HIDDEN',
                  'admin-card--pinned': post.isPinned,
                  'admin-card--featured': post.isFeatured
                }"
              >
                <div class="admin-card__main" @click="goToPost(post.slug)">
                  <div class="admin-card__header">
                    <p class="admin-card__title">{{ post.title }}</p>
                    <div class="admin-card__badges">
                      <span v-if="post.isPinned" class="admin-badge" title="置顶">📌</span>
                      <span v-if="post.isFeatured" class="admin-badge" title="加精">⭐</span>
                      <span v-if="post.status === 'HIDDEN'" class="admin-badge admin-badge--hidden" title="已隐藏">◉</span>
                    </div>
                  </div>
                  <div class="admin-card__meta">
                    <span class="admin-card__category">{{ post.category }}</span>
                    <span class="admin-card__dot">·</span>
                    <span>{{ post.points }} {{ translate('forum', 'admin.points') }}</span>
                    <span class="admin-card__dot">·</span>
                    <span>{{ post.numComments }} {{ translate('forum', 'admin.comments') }}</span>
                    <span class="admin-card__dot">·</span>
                    <span>{{ formatTimeAgo(post.createdAt) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 未选中状态 -->
        <div v-else class="member-detail-empty">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z"/>
          </svg>
          <p>点击左侧成员查看详情</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* === 页面容器 === */
.admin-container {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-md, 16px);
  padding: var(--bf-space-lg, 20px);
  min-height: 100%;
}

/* === 页面标题栏 === */
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.admin-header__title {
  display: flex;
  align-items: center;
  gap: var(--bf-space-sm, 10px);
}

.admin-header__title svg {
  width: 24px;
  height: 24px;
  color: var(--bf-primary);
}

.admin-header__title h1 {
  font-size: 18px;
  font-weight: 600;
  color: var(--bf-text-primary);
  margin: 0;
}

.admin-header__count {
  font-size: 14px;
  color: var(--bf-text-muted);
}

/* === 工具栏 === */
.admin-toolbar {
  display: flex;
  align-items: center;
  gap: var(--bf-space-md, 16px);
  flex-wrap: wrap;
}

.admin-search {
  flex: 1;
  min-width: 200px;
  position: relative;
}

.admin-search__icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  color: var(--bf-text-muted);
  pointer-events: none;
}

.admin-search__input {
  width: 100%;
  padding: 10px 12px 10px 40px;
  background: var(--bf-input-bg);
  border: 1px solid var(--bf-border-default);
  border-radius: var(--bf-radius-lg, 10px);
  color: var(--bf-text-primary);
  font-size: 14px;
  outline: none;
  transition: all 0.2s ease;
}

.admin-search__input:focus {
  border-color: var(--bf-border-accent);
  box-shadow: 0 0 0 2px var(--bf-primary-glow);
}

.admin-search__input::placeholder {
  color: var(--bf-text-muted);
}

/* === 按钮样式 === */
.admin-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border: 1px solid transparent;
  border-radius: var(--bf-radius-lg, 10px);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.admin-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.admin-btn--secondary {
  background: var(--bf-btn-secondary-bg);
  border-color: var(--bf-border-default);
  color: var(--bf-text-secondary);
}

.admin-btn--secondary:hover:not(:disabled) {
  border-color: var(--bf-border-accent);
  color: var(--bf-text-primary);
}

.admin-btn--ghost {
  background: transparent;
  border: none;
  color: var(--bf-text-muted);
  padding: 4px 8px;
}

.admin-btn--ghost:hover { color: var(--bf-text-primary); }

.admin-btn--success {
  background: rgba(34, 197, 94, 0.15);
  border-color: rgba(34, 197, 94, 0.3);
  color: #22c55e;
}

.admin-btn--success:hover:not(:disabled) { background: rgba(34, 197, 94, 0.25); }

.admin-btn--danger {
  background: rgba(239, 68, 68, 0.15);
  border-color: rgba(239, 68, 68, 0.3);
  color: #ef4444;
}

.admin-btn--danger:hover:not(:disabled) { background: rgba(239, 68, 68, 0.25); }

.admin-btn--small { padding: 4px 8px; font-size: 12px; }

.admin-btn__icon { width: 16px; height: 16px; }
.admin-btn__icon.spinning { animation: spin 0.8s linear infinite; }
.admin-btn__symbol { font-size: 14px; }

@keyframes spin { to { transform: rotate(360deg); } }

/* === 错误提示 === */
.admin-error {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: var(--bf-radius-lg, 10px);
  color: #ef4444;
  font-size: 14px;
}

.admin-error svg { width: 20px; height: 20px; flex-shrink: 0; }
.admin-error span { flex: 1; }

/* === 加载状态 === */
.admin-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  gap: 1rem;
  color: var(--bf-text-muted);
}

.admin-loading__spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--bf-border-default);
  border-top-color: var(--bf-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* === 空状态 === */
.admin-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  text-align: center;
  color: var(--bf-text-muted);
  gap: 0.75rem;
}

.admin-empty svg { width: 48px; height: 48px; opacity: 0.5; }
.admin-empty h3 { margin: 0; font-size: 1rem; font-weight: 500; color: var(--bf-text-secondary); }

/* === 双栏布局 === */
.member-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--bf-space-lg, 20px);
  flex: 1;
  min-height: 0;
}

.member-list-panel {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-md, 16px);
  overflow: hidden;
}

.member-detail-panel {
  display: flex;
  flex-direction: column;
  gap: var(--bf-space-md, 16px);
  background: var(--bf-card-bg);
  border: 1px solid var(--bf-card-border);
  border-radius: var(--bf-card-radius, 16px);
  padding: var(--bf-space-lg, 20px);
  overflow-y: auto;
  max-height: calc(100vh - 250px);
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--bf-text-primary);
}

/* === 成员卡片 === */
.member-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: var(--bf-card-bg);
  border: 1px solid var(--bf-card-border);
  border-radius: var(--bf-card-radius, 12px);
  cursor: pointer;
  transition: all 0.2s ease;
}

.member-card:hover {
  border-color: var(--bf-border-accent);
  transform: translateX(4px);
}

.member-card--banned {
  opacity: 0.7;
  border-color: rgba(239, 68, 68, 0.3);
}

.member-card--active {
  border-color: var(--bf-primary);
  background: var(--bf-fire-gradient-subtle);
}

.member-card__avatar {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bf-fire-gradient);
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  flex-shrink: 0;
  overflow: hidden;
}

.avatar-img-small {
  width: 100%;
  height: 100%;
  object-fit: cover;
  image-rendering: pixelated;
  border-radius: 8px;
}

.member-card__info {
  flex: 1;
  min-width: 0;
}

.member-card__header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.member-card__nickname {
  font-size: 15px;
  font-weight: 600;
  color: var(--bf-text-primary);
}

.member-card__badge {
  font-size: 11px;
  padding: 2px 6px;
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
  border-radius: 4px;
}

.member-card__meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--bf-text-muted);
}

.member-card__perm-count {
  color: var(--bf-primary);
}

.member-card__arrow {
  font-size: 20px;
  color: var(--bf-text-muted);
}

/* === 列表 === */
.admin-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow-y: auto;
}

.admin-card {
  padding: 12px 14px;
  background: var(--bf-input-bg);
  border: 1px solid var(--bf-border-default);
  border-radius: var(--bf-radius-lg, 10px);
  transition: all 0.2s ease;
}

.admin-card:hover { border-color: var(--bf-border-accent); }

.admin-card--hidden { opacity: 0.6; border-color: rgba(239, 68, 68, 0.3); }
.admin-card--pinned { border-color: rgba(255, 107, 53, 0.3); }
.admin-card--featured { border-color: rgba(234, 179, 8, 0.3); }

.admin-card__main { cursor: pointer; }

.admin-card__header {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 6px;
}

.admin-card__title {
  flex: 1;
  margin: 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--bf-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.admin-card__badges { display: flex; gap: 4px; }
.admin-badge { font-size: 12px; }

.admin-card__meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--bf-text-muted);
  flex-wrap: wrap;
}

.admin-card__category { color: var(--bf-text-secondary); }
.admin-card__dot { opacity: 0.5; }

/* === 分页 === */
.admin-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: auto;
  padding-top: var(--bf-space-md, 16px);
}

.admin-pagination__info {
  font-size: 14px;
  color: var(--bf-text-muted);
}

/* === 详情面板 === */
.member-detail-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  text-align: center;
  color: var(--bf-text-muted);
  gap: 1rem;
}

.member-detail-empty svg { width: 48px; height: 48px; opacity: 0.5; }
.member-detail-empty p { margin: 0; font-size: 14px; }

/* === 成员资料卡片 === */
.member-profile-card { padding: 0; }

.member-profile-header {
  display: flex;
  align-items: center;
  gap: var(--bf-space-md, 16px);
  margin-bottom: var(--bf-space-md, 16px);
}

.member-profile-avatar {
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bf-fire-gradient);
  border-radius: 8px;
  font-size: 20px;
  font-weight: 600;
  color: white;
  flex-shrink: 0;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
  image-rendering: pixelated;
}

.member-profile-info { flex: 1; }

.member-profile-nickname {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 600;
  color: var(--bf-text-primary);
}

.member-profile-badges { display: flex; gap: 8px; flex-wrap: wrap; }

.member-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: var(--bf-radius-full, 999px);
  font-size: 12px;
  font-weight: 500;
}

.member-badge--banned { background: rgba(239, 68, 68, 0.15); color: #ef4444; }
.member-badge--reputation { background: rgba(255, 107, 53, 0.15); color: var(--bf-primary); }

.member-profile-details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding: var(--bf-space-md, 16px);
  background: var(--bf-input-bg);
  border-radius: var(--bf-radius-lg, 10px);
  margin-bottom: var(--bf-space-md, 16px);
}

.member-detail-item { display: flex; flex-direction: column; gap: 2px; }
.member-detail-label { font-size: 12px; color: var(--bf-text-muted); }
.member-detail-value { font-size: 13px; color: var(--bf-text-primary); word-break: break-all; }

.member-profile-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* === 权限卡片 === */
.member-permissions-card {
  background: var(--bf-card-bg);
  padding: 0;
}

.member-grant-section {
  margin-top: var(--bf-space-md, 16px);
  padding-top: var(--bf-space-md, 16px);
  border-top: 1px solid var(--bf-border-default);
}

.member-grant-title {
  margin: 0 0 var(--bf-space-sm, 12px);
  font-size: 13px;
  font-weight: 500;
  color: var(--bf-text-muted);
}

.member-section-title {
  margin: 0 0 var(--bf-space-sm, 12px);
  font-size: 14px;
  font-weight: 600;
  color: var(--bf-text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.member-section-count { font-weight: 400; color: var(--bf-text-muted); font-size: 13px; }

.member-permissions-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.member-permission-item { display: flex; align-items: center; gap: 6px; }

.member-empty-text { color: var(--bf-text-muted); font-size: 13px; }

/* Permission colors */
.bf-permission-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: var(--bf-radius-full, 999px);
  font-size: 12px;
  font-weight: 500;
}

.bf-permission-tag--outline {
  opacity: 0.6;
  border: 1px dashed currentColor;
  background: transparent !important;
}

.bf-perm-blue { background: rgba(59, 130, 246, 0.15); color: #60a5fa; }
.bf-perm-purple { background: rgba(168, 85, 247, 0.15); color: #a78bfa; }
.bf-perm-red { background: rgba(239, 68, 68, 0.15); color: #f87171; }
.bf-perm-yellow { background: rgba(245, 158, 11, 0.15); color: #fbbf24; }
.bf-perm-red-dark { background: rgba(220, 38, 38, 0.15); color: #fca5a5; }
.bf-perm-orange { background: rgba(249, 115, 22, 0.15); color: #fb923c; }
.bf-perm-amber { background: rgba(217, 119, 6, 0.15); color: #fcd34d; }
.bf-perm-cyan { background: rgba(6, 182, 212, 0.15); color: #22d3ee; }
.bf-perm-rose { background: rgba(244, 63, 94, 0.15); color: #fb7185; }
.bf-perm-pink { background: rgba(236, 72, 153, 0.15); color: #f472b6; }
.bf-perm-teal { background: rgba(20, 184, 166, 0.15); color: #2dd4bf; }
.bf-perm-emerald { background: rgba(16, 185, 129, 0.15); color: #34d399; }
.bf-perm-indigo { background: rgba(99, 102, 241, 0.15); color: #818cf8; }
.bf-perm-gray { background: rgba(107, 114, 128, 0.15); color: #9ca3af; }

/* === 发帖历史卡片 === */
.member-posts-card { padding: 0; }

/* === 响应式 === */
@media (max-width: 1024px) {
  .member-layout {
    grid-template-columns: 1fr;
  }

  .member-detail-panel {
    max-height: none;
  }
}

@media (max-width: 640px) {
  .admin-container { padding: var(--bf-space-md, 16px); }
  .admin-toolbar { flex-direction: column; align-items: stretch; }
  .admin-search { min-width: unset; }
  .member-profile-details { grid-template-columns: 1fr; }
  .member-profile-actions { flex-direction: column; }
  .member-profile-actions .admin-btn { width: 100%; justify-content: center; }
}
</style>
