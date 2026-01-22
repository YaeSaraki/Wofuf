// i18n核心功能
import type { Translations, ModuleTranslations } from './types.ts'
import { getLocale, type Locale } from './useLocale.ts'

// 所有模块的翻译集合
const moduleTranslations: ModuleTranslations = {}

// 注册模块翻译
export function registerTranslations(moduleName: string, translations: Translations): void {
  if (!moduleTranslations[moduleName]) {
    moduleTranslations[moduleName] = {}
  }

  // 合并翻译，避免覆盖
  Object.assign(moduleTranslations[moduleName], translations)
}

// // 获取指定模块的翻译
// export function getModuleTranslations(moduleName: string): Translations {
//   return moduleTranslations[moduleName] || {}
// }
//
// // 获取所有翻译
// export function getAllTranslations(): ModuleTranslations {
//   return moduleTranslations
// }


export function translate(moduleName: string, key: string): string {
  const locale = getLocale()
  const defaultLocale: Locale = 'en'

  const module = moduleTranslations[moduleName]

  if (module == undefined) {
    const guessKey: string = moduleName.split('.').slice(1).join('.') || moduleName.split('.')[0] || ''
    for (const moduleName in moduleTranslations) {
      const translations = moduleTranslations[moduleName]
      if (translations == undefined) {
        continue
      }
      if (translations[guessKey] && translations[guessKey] != undefined) {
        console.log(`Translation key found but module name not match: ${moduleName}.${guessKey}`)
        return translations[guessKey][locale] || translations[guessKey][defaultLocale]
      }
    }
    // 未找到翻译
    console.warn(`Translation not found: ${moduleName}`)
    return key
  }

  if (module[key]) {
    return module[key][locale] || module[key][defaultLocale]
  }
  // 未找到翻译
  console.warn(`Translation not found: ${moduleName}.${key}`)
  return key
}
