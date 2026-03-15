# WofuF 前端项目结构记录

## 技术栈概览

| 技术 | 版本/说明 |
|------|----------|
| 框架 | Vue 3 (Composition API) |
| 语言 | TypeScript |
| 构建工具 | Vite |
| 路由 | Vue Router 4 |
| UI 库 | PrimeVue 4 + Tailwind CSS 4 |
| HTTP 客户端 | Axios |
| 国际化 | 自定义 i18n 服务 |

## 目录结构

```
WofuF/src/
├── modules/                    # 业务模块
│   ├── app/                   # 主应用模块
│   │   ├── App.vue           # 根组件
│   │   ├── components/       # 应用级组件 (AppNavbar, AppFooter)
│   │   ├── utils/i18n/       # 应用翻译
│   │   └── index.ts          # 模块入口
│   │
│   ├── players/              # 玩家模块
│   │   ├── components/       # 组件目录
│   │   │   ├── playerProfile/    # 玩家资料面板
│   │   │   ├── statistics/       # 统计面板
│   │   │   ├── advancements/     # 成就面板
│   │   │   └── yesterdayOnlineList/  # 昨日在线列表
│   │   ├── services/         # 服务层
│   │   │   ├── PlayerService.ts
│   │   │   ├── StatisticService.ts
│   │   │   └── AdvancementService.ts
│   │   ├── dtos/             # 数据传输对象
│   │   │   ├── Player.ts
│   │   │   ├── PlayerSkin.ts
│   │   │   ├── PlayerUuid.ts
│   │   │   ├── PlayerName.ts
│   │   │   ├── PlayerStatistic.ts
│   │   │   └── PlayerAdvancement.ts
│   │   ├── config/           # 配置文件
│   │   │   ├── translation/  # 翻译配置
│   │   │   ├── statisticGroups.ts
│   │   │   └── AdvancementGroups.ts
│   │   └── index.ts          # 模块入口
│   │
│   └── forum/                # 论坛模块
│       ├── components/       # 组件目录
│       │   ├── createPost/       # 创建帖子
│       │   ├── postDetail/       # 帖子详情
│       │   ├── postList/         # 帖子列表
│       │   └── replyToComment/   # 回复评论
│       ├── services/         # 服务层
│       │   └── ForumService.ts
│       ├── dtos/             # 数据传输对象
│       │   └── Post.ts
│       ├── config/           # 配置文件
│       │   └── translation.ts
│       └── index.ts          # 模块入口
│
├── shared/                    # 共享代码
│   ├── core/                 # 核心工具
│   │   ├── Result.ts         # 结果包装类
│   │   └── index.ts
│   │
│   ├── services/             # 共享服务
│   │   ├── i18n/             # 国际化服务
│   │   │   ├── index.ts
│   │   │   ├── types.ts
│   │   │   ├── useLocale.ts
│   │   │   └── translations.ts
│   │   ├── ImageImporter/    # 图片导入服务
│   │   └── index.ts
│   │
│   ├── utils/                # 工具函数
│   │   ├── async/            # 异步工具
│   │   │   ├── useAsyncLoader.ts   # 异步加载 Hook
│   │   │   └── RequestOptions.ts
│   │   ├── renderUTil.ts     # 渲染工具
│   │   └── Base64Util.ts     # Base64 工具
│   │
│   ├── infra/                # 基础设施
│   │   ├── api/              # API 配置
│   │   │   ├── http.ts       # Axios 实例
│   │   │   └── v1/           # V1 版本 API
│   │   │       ├── ApiErrorMessage.ts
│   │   │       └── models/ApiResponse.ts
│   │   ├── cache/            # 缓存服务
│   │   │   ├── CacheService.ts
│   │   │   └── index.ts
│   │   └── router/           # 路由配置
│   │       └── index.ts
│   │
│   ├── layout/               # 布局组件
│   ├── components/           # 共享组件
│   │   └── DraggablePopup.vue
│   ├── assets/               # 静态资源
│   │   ├── main.css
│   │   ├── base.css
│   │   └── primevue.css
│   └── main.ts               # 应用入口
│
└── views/                     # 页面视图
    ├── ServerStatsView.vue   # 首页/服务器统计
    ├── PlayerView.vue        # 玩家详情页
    ├── ForumView.vue         # 论坛首页
    ├── CreatePostView.vue    # 创建帖子页
    ├── PostView.vue          # 帖子详情页
    └── AboutView.vue         # 关于页面
```

## 路径别名配置

```typescript
'@': './src'                    // 源码根目录
'@M': './src/modules'           // 业务模块
'@S': './src/shared'            // 共享代码
'@SU': './src/shared/utils'     // 共享工具
```

## 核心模式

### 1. Result 模式
用于统一处理成功/失败结果：

```typescript
// 使用示例
const result = await playerService.getPlayerProfile(name)
if (result.isSuccess) {
  const player = result.getValue()
} else {
  console.error(result.error) // 错误信息
}

// 创建结果
Result.success(data)
Result.failure('错误信息')
```

### 2. Service 模式
服务类统一结构：

```typescript
export interface IPlayerService {
  getPlayerProfile(name: string, options?: RequestOptions): Promise<Result<Player>>
}

export class PlayerService implements IPlayerService {
  private static readonly CACHE_MODULE = 'player_service'

  public async getPlayerProfile(name: string, options?: RequestOptions): Promise<Result<Player>> {
    // 1. 检查缓存
    const cached = cacheService.get<Player>(PlayerService.CACHE_MODULE, `profile_${name}`)
    if (cached) return Result.success(cached)

    // 2. 发起请求
    try {
      const response = await http.get<ApiResponse<Player>>(`/api/v1/players/${name}`, {
        signal: options?.signal
      })

      if (response.data.success) {
        cacheService.set(PlayerService.CACHE_MODULE, `profile_${name}`, response.data.data)
        return Result.success(response.data.data)
      }
      return Result.failure(response.data.message)
    } catch (error) {
      return Result.failure(error instanceof Error ? error.message : '请求失败')
    }
  }
}
```

### 3. useAsyncLoader Hook
异步加载状态管理：

```typescript
const { isLoading, errorMsg, executeAsync, cancelAsync } = useAsyncLoader()

const fetchData = async () => {
  const result = await executeAsync(
    async (signal) => await service.getData({ signal }),
    '获取数据失败'
  )
  if (result) {
    // 处理结果
  }
}
```

### 4. 组件结构模板

```vue
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useAsyncLoader } from '@SU/async/useAsyncLoader'
import type { DataType } from '@M/module/dtos/DataType.ts'
import { Service } from '@M/module/services/Service.ts'

// Props
const props = defineProps<{
  dataId: string
}>()

// Emits
const emit = defineEmits<{
  update: [data: DataType]
}>()

// 状态
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()
const data = ref<DataType | null>(null)

// 方法
const fetchData = async () => {
  const result = await executeAsync(
    async (signal) => await Service.getData(props.dataId, { signal }),
    '获取数据失败'
  )
  if (result) {
    data.value = result
  }
}

// 生命周期
onMounted(fetchData)
</script>

<template>
  <div class="component-container">
    <div v-if="isLoading">加载中...</div>
    <div v-else-if="errorMsg">{{ errorMsg }}</div>
    <div v-else-if="data">
      <!-- 内容 -->
    </div>
  </div>
</template>

<style scoped>
/* 样式 */
</style>
```

## 路由配置

```typescript
// 当前路由
const routes = [
  { path: '/', name: 'home', component: () => import('@/views/ServerStatsView.vue') },
  { path: '/players/:name', name: 'player', component: () => import('@/views/PlayerView.vue') },
  { path: '/forum', name: 'forum', component: () => import('@/views/ForumView.vue') },
  { path: '/forum/create', name: 'createPost', component: () => import('@/views/CreatePostView.vue') },
  { path: '/forum/posts/:slug', name: 'post', component: () => import('@/views/PostView.vue') },
  { path: '/about', name: 'about', component: () => import('@/views/AboutView.vue') },
]
```

## 缓存策略

使用 `CacheService` 按模块管理缓存：

```typescript
// 设置缓存
cacheService.set('module_name', 'cache_key', data)

// 获取缓存
const cached = cacheService.get<DataType>('module_name', 'cache_key')

// 清除模块缓存
cacheService.clearModule('module_name')

// 清除所有缓存
cacheService.clearAll()
```

## API 调用规范

```typescript
// HTTP 实例配置
export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10_000,
})

// 响应结构
interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
}

// 调用示例
const response = await http.get<ApiResponse<Player>>('/api/v1/players/profile', {
  params: { name },
  signal: options?.signal  // 支持取消请求
})
```

## 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 组件文件 | PascalCase.vue | `PlayerProfileCard.vue` |
| 服务文件 | PascalCase.ts | `PlayerService.ts` |
| DTO 文件 | PascalCase.ts | `Player.ts` |
| 工具函数 | camelCase.ts | `useAsyncLoader.ts` |
| 配置文件 | camelCase.ts | `translation.ts` |
| 变量 | camelCase | `playerData` |
| 常量 | UPPER_SNAKE_CASE | `CACHE_MODULE` |
| 类型/接口 | PascalCase | `IPlayerService` |

## 模块开发流程

### 1. 创建新模块

```
modules/{moduleName}/
├── components/          # 组件
├── services/            # 服务
├── dtos/                # 数据类型
├── config/              # 配置
└── index.ts             # 入口
```

### 2. 定义 DTO

```typescript
// dtos/DataType.ts
export interface DataType {
  id: string
  name: string
  // ...
}
```

### 3. 创建服务

```typescript
// services/ModuleService.ts
export interface IModuleService {
  getData(id: string, options?: RequestOptions): Promise<Result<DataType>>
}

export class ModuleService implements IModuleService {
  private static readonly CACHE_MODULE = 'module_service'
  // 实现方法...
}
```

### 4. 创建组件

```typescript
// components/ComponentName.vue
// 使用 useAsyncLoader 处理异步
// 使用 Props/Emits 通信
// 使用 Tailwind CSS 样式
```

### 5. 注册模块

```typescript
// index.ts
export * from './services/ModuleService'
export * from './dtos/DataType'
```

```typescript
// shared/main.ts
import '@M/moduleName/index.ts'
```

### 6. 添加路由

```typescript
// shared/infra/router/index.ts
{
  path: '/module',
  name: 'module',
  component: () => import('@/views/ModuleView.vue')
}
```

## 待开发功能

基于后端 API，前端需要补充以下功能：

### Forum 模块
- [ ] 帖子列表获取 (getRecentPosts, getPopularPosts)
- [ ] 帖子投票 (upvotePost, downvotePost)
- [ ] 评论功能 (getCommentsByPostSlug, replyToPost, replyToComment)
- [ ] 帖子编辑/删除 (editPost, deletePost)
- [ ] 成员信息 (getCurrentMember, getMemberByUserName)

### API 端点对接
后端 API 路径格式：
- Posts: `/api/v1/forum/posts/*`
- Comments: `/api/v1/forum/comments/*`
- Members: `/api/v1/forum/members/*`
