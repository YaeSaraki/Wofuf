# WofuF 前端代码编写风格指南

## 概述

WofuF 前端项目采用 Vue 3 + TypeScript + Vite 的现代化技术栈，遵循模块化架构和一致的编码规范。本文档为 AI 助手提供前端代码生成指南，确保生成的代码符合项目风格和最佳实践。

## 技术栈

- **框架**: Vue 3 (Composition API)
- **语言**: TypeScript
- **构建工具**: Vite
- **路由**: Vue Router 4
- **UI 库**: PrimeVue 4 + Tailwind CSS 4
- **HTTP 客户端**: Axios
- **图标**: FontAwesome
- **3D 渲染**: skin3d (用于玩家皮肤)
- **国际化**: 自定义 i18n 服务

## 项目结构

```
WofuF/
├── src/
│   ├── modules/          # 业务模块
│   │   ├── app/         # 主应用模块
│   │   └── players/     # 玩家模块
│   ├── shared/          # 共享代码
│   │   ├── core/        # 核心工具类 (Result.ts)
│   │   ├── services/    # 共享服务 (i18n, ImageImporter)
│   │   ├── utils/       # 工具函数
│   │   ├── components/  # 共享组件
│   │   ├── layout/      # 布局组件
│   │   ├── infra/       # 基础设施 (router, api)
│   │   └── assets/      # 静态资源
│   └── views/           # 页面视图
├── public/              # 公共静态资源
└── dist/                # 构建输出
```

## 模块结构

每个业务模块遵循以下结构：

```
modules/{moduleName}/
├── components/          # 模块组件
├── services/            # 模块服务
├── dtos/               # 数据传输对象
├── config/             # 模块配置
├── assets/             # 模块资源
└── index.ts            # 模块入口
```

## 命名规范

### 文件命名
- **组件**: `PascalCase.vue` (e.g., `PlayerProfileCard.vue`)
- **服务**: `PascalCase.ts` (e.g., `PlayerService.ts`)
- **工具函数**: `camelCase.ts` (e.g., `useAsyncLoader.ts`)
- **类型定义**: `PascalCase.ts` (e.g., `Player.ts`)
- **配置文件**: `camelCase.ts` (e.g., `translation.ts`)

### 变量命名
- **常量**: `UPPER_SNAKE_CASE`
- **组件变量**: `camelCase`
- **类型**: `PascalCase`
- **接口**: `IPascalCase` (e.g., `IPlayerService`)

### 路径别名
```typescript
'@': './src'
'@M': './src/modules'          // 模块
'@S': './src/shared'           // 共享
'@SU': './src/shared/utils'    // 共享工具
```

## 组件编写规范

### 基本结构
```vue
<script lang="ts" setup>
// 导入
import { ref, onMounted } from 'vue'
import type { Player } from '@M/players/dtos/Player.ts'

// Props
const props = defineProps<{
  player: Player | null
}>()

// Emits (如需要)
const emit = defineEmits<{
  update: [player: Player]
}>()

// 响应式数据
const playerData = ref<Player | null>(null)

// 生命周期
onMounted(() => {
  // 初始化逻辑
})

// 方法
const handleUpdate = () => {
  // 处理逻辑
}
</script>

<template>
  <div class="component-container">
    <!-- 模板内容 -->
  </div>
</template>

<style scoped>
/* 组件样式 */
</style>
```

### Composition API 模式
- 使用 `<script setup>` 语法
- 优先使用 Composition API
- 明确定义 props 和 emits 类型
- 使用 TypeScript 进行类型标注

### 组件通信
- **父子通信**: Props + Emits
- **兄弟通信**: 共享状态或事件总线
- **跨组件通信**: Vue Router 或全局状态

## 服务编写规范

### 服务接口定义
```typescript
export interface IPlayerService {
  getPlayerProfile(playerNameOrUuid: string): Promise<Result<Player>>
  getPlayerSkin(playerUuid: string): Promise<Result<PlayerSkin>>
}

export class PlayerService implements IPlayerService {
  private static readonly CACHE_MODULE = 'player_service'

  public async getPlayerProfile(
    playerNameOrUuid: string,
    options?: RequestOptions
  ): Promise<Result<Player>> {
    // 实现逻辑
  }
}
```

### HTTP 请求
```typescript
import { http } from '@S/infra/api/http.ts'
import type { ApiResponse } from '@S/infra/api/v1/models/ApiResponse.ts'

const response = await http.get<ApiResponse<Player>>('/v1/players/profile', {
  params: { name: playerName },
  signal: options?.signal
})

return response.data.isSuccess
  ? Result.success(response.data.data)
  : Result.failure(response.data.message)
```

## 工具函数规范

### 组合式函数 (Composables)
```typescript
import { ref, onUnmounted } from 'vue'

export function useAsyncLoader() {
  const isLoading = ref(false)
  const errorMsg = ref<string | null>(null)

  const executeAsync = async <T>(
    asyncFn: (signal: AbortSignal) => Promise<T>,
    errorTip = '操作失败'
  ): Promise<T | null> => {
    // 实现逻辑
  }

  return {
    isLoading,
    errorMsg,
    executeAsync
  }
}
```

### 工具函数
```typescript
export const addImagePrefixToBase64 = (base64: string): string => {
  return `data:image/png;base64,${base64}`
}
```

## 类型定义规范

### 接口和类型
```typescript
// 数据传输对象
export interface Player {
  id: string
  name: string
  skin?: PlayerSkin
}

// 枚举类型
export enum PlayerType {
  PREMIUM = 'premium',
  FREE = 'free'
}

// 联合类型
export type PlayerStatus = 'online' | 'offline' | 'away'
```

### DTO 结构
```
dtos/
├── Player.ts          # 玩家实体
├── PlayerSkin.ts      # 玩家皮肤
└── PlayerUuid.ts      # 玩家UUID
```

## 样式规范

### Tailwind CSS
- 使用 Tailwind 工具类
- 响应式设计: `sm:`, `md:`, `lg:`, `xl:`
- 暗色模式: `dark:`
- 自定义颜色: 使用 Tailwind 配置

### PrimeVue 组件
```vue
<template>
  <Button
    label="提交"
    icon="pi pi-check"
    class="p-button-primary"
    @click="handleSubmit"
  />
</template>
```

### 自定义样式
```vue
<style scoped>
.custom-component {
  @apply bg-white dark:bg-gray-800 rounded-lg shadow-md;
}
</style>
```

## 异步处理规范

### 使用 useAsyncLoader
```typescript
const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

const fetchData = async () => {
  const result = await executeAsync(async (signal) => {
    return await apiService.getData({ signal })
  }, '获取数据失败')

  if (result) {
    // 处理结果
  }
}
```

### 错误处理
```typescript
try {
  const data = await executeAsync(fetchFunction, '自定义错误信息')
} catch (error) {
  console.error('兜底异常:', error)
}
```

## 缓存策略

### 服务缓存
```typescript
export class PlayerService {
  private static readonly CACHE_MODULE = 'player_service'

  public async getPlayerProfile(name: string): Promise<Result<Player>> {
    const cacheKey = `profile_${name}`
    const cached = cacheService.get<Player>(PlayerService.CACHE_MODULE, cacheKey)
    if (cached) {
      return Result.success(cached)
    }

    // 获取数据并缓存
    const result = await this.fetchFromApi(name)
    if (result.isSuccess) {
      cacheService.set(PlayerService.CACHE_MODULE, cacheKey, result.getValue())
    }

    return result
  }
}
```

## 国际化 (i18n)

### 使用翻译
```typescript
import { translate } from '@S/services/i18n'

const message = translate('player.notFound')
```

### 翻译文件结构
```
services/i18n/
├── index.ts           # i18n 服务
├── types.ts           # 类型定义
├── useLocale.ts       # 语言切换 hook
└── translations.ts    # 翻译文件
```

## 测试规范

### 组件测试
- 使用 Vue Test Utils
- 测试用户交互和状态变化
- Mock 外部依赖

### 服务测试
- Mock HTTP 请求
- 测试成功和失败场景
- 验证缓存行为

## 性能优化

### 组件优化
- 使用 `shallowRef` 和 `triggerRef` 优化大型对象
- 合理使用 `computed` 和 `watch`
- 避免不必要的重新渲染

### 图片优化
- 使用懒加载
- 压缩图片资源
- 缓存策略

### 代码分割
- 路由懒加载
- 动态导入组件
- 按模块分割

## 代码质量

### ESLint 配置
- 使用 `@vue/eslint-config-typescript`
- 自动修复格式问题
- 强制代码风格一致性

### Prettier 配置
- 自动格式化代码
- 统一缩进和换行
- 保持代码可读性

### TypeScript 配置
- 严格模式启用
- 类型检查
- 智能提示支持

## 提交规范

### Git 提交信息
```
feat: 添加新功能
fix: 修复bug
docs: 更新文档
style: 格式化代码
refactor: 重构代码
test: 添加测试
chore: 构建工具配置
```

### 分支命名
- `feature/功能名称`
- `fix/问题描述`
- `docs/文档更新`

## AI 生成代码注意事项

### 组件生成
1. 使用 Composition API + `<script setup>`
2. 明确定义 props 和 emits 类型
3. 使用 TypeScript 进行完整类型标注
4. 遵循 Tailwind CSS 样式规范
5. 包含错误处理和加载状态

### 服务生成
1. 实现接口定义
2. 使用 Result<T> 包装返回值
3. 包含缓存逻辑
4. 处理 HTTP 错误
5. 使用 AbortController 支持取消请求

### 工具函数生成
1. 使用组合式函数模式
2. 提供清晰的 TypeScript 类型
3. 包含错误处理
4. 支持 AbortSignal

### 类型定义生成
1. 使用接口和类型别名
2. 避免 any 类型
3. 提供完整的类型覆盖
4. 与后端 DTO 保持一致

## 常见模式

### 数据获取组件
```vue
<script setup lang="ts">
import { useAsyncLoader } from '@SU/async/useAsyncLoader'

const { isLoading, errorMsg, executeAsync } = useAsyncLoader()

const data = ref(null)

const fetchData = async () => {
  const result = await executeAsync(
    async (signal) => await service.getData({ signal }),
    '获取数据失败'
  )
  if (result) {
    data.value = result
  }
}

onMounted(fetchData)
</script>

<template>
  <div v-if="isLoading">加载中...</div>
  <div v-else-if="errorMsg">{{ errorMsg }}</div>
  <div v-else>
    <!-- 数据内容 -->
  </div>
</template>
```

### 表单组件
```vue
<script setup lang="ts">
const formData = ref({
  name: '',
  email: ''
})

const submitForm = async () => {
  // 表单验证和提交逻辑
}
</script>

<template>
  <form @submit.prevent="submitForm">
    <input v-model="formData.name" type="text" />
    <input v-model="formData.email" type="email" />
    <button type="submit">提交</button>
  </form>
</template>
```

这个指南确保 AI 生成的代码与 WofuF 前端项目保持一致的风格和质量。
