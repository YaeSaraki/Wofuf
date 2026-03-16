// app模块翻译
import {registerTranslations} from '@S/services/i18n'

const translations = {
  // 导航栏
  'nav.status': {zh: '状态', en: 'Status'},
  'nav.about': {zh: '关于', en: 'About'},
  'nav.forum': {zh: '论坛', en: 'Forum'},
  'nav.settings': {zh: '设置', en: 'Settings'},

  // 页脚
  'footer.copyright': {
    zh: '© 2026 WofuF · Minecraft Server',
    en: '© 2026 WofuF · Minecraft Server',
  },

  // 页面标题
  'page.about': {zh: '关于页面', en: 'About Page'},
  'page.home': {zh: '首页', en: 'Home Page'},

  // 操作按钮
  'actions.retry': {zh: '重试', en: 'Retry'},

  // 设置
  'settings.language': {zh: '语言', en: 'Language'},
  'settings.theme': {zh: '主题', en: 'Theme'},
  'settings.themeLight': {zh: '日间', en: 'Light'},
  'settings.themeDark': {zh: '夜间', en: 'Dark'},
  'settings.themeSystem': {zh: '自动', en: 'Auto'},
  'settings.themeFollowSystem': {zh: '主题跟随系统', en: 'Theme follows system'},

  // 通用
  'common.close': {zh: '关闭', en: 'Close'},
}

// 注册app模块翻译
registerTranslations('app', translations)
