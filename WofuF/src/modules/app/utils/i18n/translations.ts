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

  'page.home': {zh: '首页', en: 'Home Page'},

  // 操作按钮
  'actions.retry': {zh: '重试', en: 'Retry'},
  'actions.view': {zh: '查看', en: 'View'},
  'actions.join': {zh: '加入', en: 'Join'},

  // 设置
  'settings.language': {zh: '语言', en: 'Language'},
  'settings.theme': {zh: '主题', en: 'Theme'},
  'settings.themeFollowSystem': {zh: '主题跟随系统', en: 'Theme follows system'},

  // 通用
  'common.close': {zh: '关闭', en: 'Close'},
  'common.loading': {zh: '加载中...', en: 'Loading...'},
  'common.error': {zh: '出错了', en: 'Error'},
  'common.success': {zh: '成功', en: 'Success'},

  // 状态页
  'status.description': {zh: '欢迎来到 WofuF 服务器状态页面', en: 'Welcome to WofuF Server Status Page'},
  'status.serverOnline': {zh: '服务器在线', en: 'Server Online'},
  'status.serverOffline': {zh: '服务器离线', en: 'Server Offline'},
  'status.online': {zh: '在线', en: 'Online'},
  'status.offline': {zh: '离线', en: 'Offline'},
  'status.serverInfo': {zh: '服务器信息', en: 'Server Information'},
  'status.version': {zh: '版本', en: 'Version'},
  'status.uptime': {zh: '在线时间', en: 'Uptime'},
  'status.tps': {zh: 'TPS', en: 'TPS'},
  'status.ping': {zh: '延迟', en: 'Ping'},
  'status.players': {zh: '在线玩家', en: 'Online Players'},
  'status.system': {zh: '系统状态', en: 'System Status'},
  'status.stats': {zh: '服务器统计', en: 'Server Statistics'},
  'status.info.title': {zh: '服务器信息', en: 'Server Information'},
  'status.info.version': {zh: '服务器版本', en: 'Server Version'},
  'status.info.uptime': {zh: '在线时间', en: 'Uptime'},
  'status.info.tps': {zh: 'TPS', en: 'TPS'},
  'status.info.latency': {zh: '延迟', en: 'Latency'},
  'status.players.title': {zh: '在线玩家', en: 'Online Players'},
  'status.players.count': {zh: '玩家数量', en: 'Player Count'},
  'status.players.name': {zh: '玩家名称', en: 'Player Name'},
  'status.players.level': {zh: '等级', en: 'Level'},
  'status.players.joined': {zh: '加入时间', en: 'Joined'},
  'status.system.title': {zh: '系统状态', en: 'System Status'},
  'status.system.server': {zh: '服务器', en: 'Server'},
  'status.system.database': {zh: '数据库', en: 'Database'},
  'status.system.discord': {zh: 'Discord机器人', en: 'Discord Bot'},
  'status.system.website': {zh: '网站', en: 'Website'},
  'status.system.online': {zh: '在线', en: 'Online'},
  'status.system.offline': {zh: '离线', en: 'Offline'},
  'status.system.unknown': {zh: '未知', en: 'Unknown'},
  'status.stats.title': {zh: '服务器统计', en: 'Server Statistics'},
  'status.stats.totalPlayers': {zh: '总玩家数', en: 'Total Players'},
  'status.stats.worldSize': {zh: '世界大小', en: 'World Size'},
  'status.stats.totalPosts': {zh: '论坛帖子', en: 'Forum Posts'},
  'status.stats.totalComments': {zh: '评论数', en: 'Comments'},
  'status.yesterday.title': {zh: '昨日在线', en: 'Yesterday Online'},
  'status.quickLinks.title': {zh: '快速链接', en: 'Quick Links'},
  'status.quickLinks.forum': {zh: '访问论坛', en: 'Visit Forum'},
  'status.quickLinks.rules': {zh: '服务器规则', en: 'Server Rules'},
  'status.quickLinks.store': {zh: '服务器商店', en: 'Server Store'},

  // 关于页
  'about.description': {zh: '了解 WofuF 服务器的故事和团队', en: 'Learn about WofuF Servers story and team'},
  'about.serverIntro': {zh: '服务器介绍', en: 'Server Introduction'},
  'about.serverDesc1': {zh: 'WofuF 是一个专注于提供优质游戏体验的 Minecraft 服务器，成立于 2024 年。', en: 'WofuF is a Minecraft server dedicated to providing high-quality gaming experiences, established in 2024.'},
  'about.serverDesc2': {zh: '我们致力于打造一个友好、公平、有趣的游戏环境，让每一位玩家都能在这里找到属于自己的乐趣。', en: 'We are committed to creating a friendly, fair, and fun gaming environment where every player can find their own joy.'},
  'about.serverDesc3': {zh: '服务器定期举办各种活动，拥有完善的插件系统和管理团队，为玩家提供最好的游戏体验。', en: 'The server regularly hosts various events, has a complete plugin system and management team, providing players with the best gaming experience.'},
  'about.features.title': {zh: '服务器特点', en: 'Server Features'},
  'about.features.survival': {zh: '生存模式', en: 'Survival Mode'},
  'about.features.survivalDesc': {zh: '经典的生存玩法，体验原汁原味的 Minecraft', en: 'Classic survival gameplay, experience the original Minecraft'},
  'about.features.custom': {zh: '自定义内容', en: 'Custom Content'},
  'about.features.customDesc': {zh: '丰富的自定义插件和内容，增加游戏趣味性', en: 'Rich custom plugins and content to increase game fun'},
  'about.features.events': {zh: '定期活动', en: 'Regular Events'},
  'about.features.eventsDesc': {zh: '各种有趣的服务器活动，赢取丰厚奖励', en: 'Various interesting server events to win rich rewards'},
  'about.features.community': {zh: '活跃社区', en: 'Active Community'},
  'about.features.communityDesc': {zh: '友好的社区氛围，认识更多游戏伙伴', en: 'Friendly community atmosphere, meet more gaming partners'},
  'about.features.antiCheat': {zh: '反作弊系统', en: 'Anti-Cheat System'},
  'about.features.antiCheatDesc': {zh: '严格的反作弊措施，保证游戏公平性', en: 'Strict anti-cheat measures to ensure game fairness'},
  'about.features.multiverse': {zh: '多世界系统', en: 'Multi-world System'},
  'about.features.multiverseDesc': {zh: '多个不同主题的世界，探索更多可能性', en: 'Multiple worlds with different themes, explore more possibilities'},
  'about.stats.title': {zh: '服务器统计', en: 'Server Statistics'},
  'about.team.title': {zh: '团队成员', en: 'Team Members'},
  'about.contact.title': {zh: '联系我们', en: 'Contact Us'},
  'about.contact.description': {zh: '有任何问题或建议，欢迎联系我们', en: 'If you have any questions or suggestions, please feel free to contact us'},
}

// 注册app模块翻译
registerTranslations('app', translations)
