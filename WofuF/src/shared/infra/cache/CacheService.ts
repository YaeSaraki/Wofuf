// src/infra/cache/CacheService.ts

export type CacheKey = string

/**
 * 分类缓存服务 - 按功能模块管理缓存
 */
export class CacheService {
  // 单例实例
  private static instance: CacheService

  // 按模块存储Map：Map<模块名, Map<key, value>>
  private moduleCaches = new Map<string, Map<CacheKey, unknown>>()

  /**
   * 设置缓存值
   * @param module 功能模块名称
   * @param key 缓存键
   * @param value 缓存值
   */
  set<V>(module: string, key: CacheKey, value: V): void {
    let moduleCache = this.moduleCaches.get(module)
    if (!moduleCache) {
      moduleCache = new Map<CacheKey, unknown>()
      this.moduleCaches.set(module, moduleCache)
    }
    moduleCache.set(key, value)
  }

  /**
   * 获取缓存值
   * @param module 功能模块名称
   * @param key 缓存键
   * @returns 缓存值或undefined
   */
  get<V>(module: string, key: CacheKey): V | undefined {
    const moduleCache = this.moduleCaches.get(module)
    if (!moduleCache) return undefined
    return moduleCache.get(key) as V | undefined
  }

  /**
   * 检查缓存是否存在
   * @param module 功能模块名称
   * @param key 缓存键
   */
  has(module: string, key: CacheKey): boolean {
    const moduleCache = this.moduleCaches.get(module)
    return moduleCache ? moduleCache.has(key) : false
  }

  /**
   * 删除缓存
   * @param module 功能模块名称
   * @param key 缓存键
   */
  delete(module: string, key: CacheKey): boolean {
    const moduleCache = this.moduleCaches.get(module)
    return moduleCache ? moduleCache.delete(key) : false
  }

  // ============ 模块级别操作 ============

  /**
   * 获取整个模块的Map
   */
  getModule<V>(module: string): Map<CacheKey, V> | undefined {
    const moduleCache = this.moduleCaches.get(module)
    return moduleCache as Map<CacheKey, V> | undefined
  }

  /**
   * 获取或创建模块Map
   */
  getOrCreateModule<V>(module: string): Map<CacheKey, V> {
    let moduleCache = this.moduleCaches.get(module) as Map<CacheKey, V> | undefined
    if (!moduleCache) {
      moduleCache = new Map<CacheKey, V>()
      this.moduleCaches.set(module, moduleCache as Map<CacheKey, unknown>)
    }
    return moduleCache
  }

  /**
   * 清空模块所有缓存
   */
  clearModule(module: string): boolean {
    const moduleCache = this.moduleCaches.get(module)
    if (moduleCache) {
      moduleCache.clear()
      return true
    }
    return false
  }

  /**
   * 删除整个模块
   */
  deleteModule(module: string): boolean {
    return this.clearModule(module) && this.moduleCaches.delete(module)
  }

  // ============ 批量操作 ============

  /**
   * 批量设置缓存
   */
  setBatch<V>(module: string, items: Array<[CacheKey, V]>): void {
    const moduleCache = this.getOrCreateModule<V>(module)
    items.forEach(([key, value]) => {
      moduleCache.set(key, value)
    })
  }

  /**
   * 批量获取缓存
   */
  getBatch<V>(module: string, keys: CacheKey[]): Map<CacheKey, V | undefined> {
    const result = new Map<CacheKey, V | undefined>()
    const moduleCache = this.moduleCaches.get(module)

    if (moduleCache) {
      keys.forEach(key => {
        result.set(key, moduleCache.get(key) as V | undefined)
      })
    } else {
      keys.forEach(key => {
        result.set(key, undefined)
      })
    }

    return result
  }

  // ============ 全局操作 ============

  /**
   * 清空所有模块的所有缓存
   */
  clearAll(): void {
    this.moduleCaches.forEach(moduleCache => {
      moduleCache.clear()
    })
  }

  /**
   * 删除所有模块
   */
  deleteAll(): void {
    this.clearAll()
    this.moduleCaches.clear()
  }

  // ============ 查询和统计 ============

  /**
   * 获取所有模块名称
   */
  getAllModules(): string[] {
    return Array.from(this.moduleCaches.keys())
  }

  /**
   * 获取模块大小（缓存数量）
   */
  getModuleSize(module: string): number {
    const moduleCache = this.moduleCaches.get(module)
    return moduleCache ? moduleCache.size : 0
  }

  /**
   * 获取所有模块的统计信息
   */
  getStats(): Array<{
    module: string
    size: number
    keys: CacheKey[]
  }> {
    const stats: Array<{
      module: string
      size: number
      keys: CacheKey[]
    }> = []

    this.moduleCaches.forEach((moduleCache, module) => {
      stats.push({
        module,
        size: moduleCache.size,
        keys: Array.from(moduleCache.keys())
      })
    })

    return stats
  }

  /**
   * 获取模块的所有键
   */
  getModuleKeys(module: string): CacheKey[] {
    const moduleCache = this.moduleCaches.get(module)
    return moduleCache ? Array.from(moduleCache.keys()) : []
  }

  /**
   * 获取模块的所有值
   */
  getModuleValues<V>(module: string): V[] {
    const moduleCache = this.moduleCaches.get(module)
    if (!moduleCache) return []

    return Array.from(moduleCache.values()) as V[]
  }

  /**
   * 获取模块的所有条目
   */
  getModuleEntries<V>(module: string): Array<[CacheKey, V]> {
    const moduleCache = this.moduleCaches.get(module)
    if (!moduleCache) return []

    return Array.from(moduleCache.entries()) as Array<[CacheKey, V]>
  }

  // ============ 搜索功能 ============

  /**
   * 搜索模块中符合条件的缓存
   */
  search<V>(
    module: string,
    predicate: (value: V, key: CacheKey) => boolean
  ): Array<[CacheKey, V]> {
    const entries = this.getModuleEntries<V>(module)
    return entries.filter(([key, value]) => predicate(value, key))
  }
}
