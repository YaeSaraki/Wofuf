// app模块翻译
import {registerTranslations} from '@S/services/i18n'

const translations = {
  // 导航栏
  'nav.status': {zh: '状态', en: 'Status'},
  'nav.about': {zh: '关于', en: 'About'},

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
}

// 注册app模块翻译
registerTranslations('app', translations)
