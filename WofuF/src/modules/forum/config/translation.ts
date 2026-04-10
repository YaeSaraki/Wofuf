// 论坛翻译
import {registerTranslations} from '@S/services/i18n'

const forumTranslations = {
  // 页面标题
  forumTitle: {
    zh: '论坛',
    en: 'Forum',
  },
  forumSubtitle: {
    zh: '分享你的游戏体验，与其他玩家交流',
    en: 'Share your gaming experience, connect with other players',
  },
  community: {
    zh: '社区',
    en: 'Community',
  },
  'forum.title': {
    zh: '论坛',
    en: 'Forum',
  },
  'createPost.button': {
    zh: '创建帖子',
    en: 'Create Post',
  },
  'login.title': {
    zh: '登录',
    en: 'Login',
  },
  'tabs.recent': {
    zh: '最新',
    en: 'Recent',
  },
  'tabs.popular': {
    zh: '热门',
    en: 'Popular',
  },
  'post.toc.title': {
    zh: '目录',
    en: 'Table of Contents',
  },
  // 帖子相关
  post: {
    zh: '帖子',
    en: 'Post',
  },
  posts: {
    zh: '帖子列表',
    en: 'Posts',
  },
  create_post: {
    zh: '发帖',
    en: 'Create Post',
  },
  noPosts: {
    zh: '暂无帖子',
    en: 'No posts yet',
  },
  noPostsDesc: {
    zh: '成为第一个发帖的人吧！',
    en: 'Be the first to post!',
  },
  points: {
    zh: '积分',
    en: 'Points',
  },
  // 排序
  sortRecent: {
    zh: '最新',
    en: 'Recent',
  },
  sortPopular: {
    zh: '热门',
    en: 'Popular',
  },
  // 投票
  upvote: {
    zh: '点赞',
    en: 'Upvote',
  },
  downvote: {
    zh: '点踩',
    en: 'Downvote',
  },
  // 评论相关
  comment: {
    zh: '评论',
    en: 'Comment',
  },
  comments: {
    zh: '评论',
    en: 'Comments',
  },
  reply: {
    zh: '回复',
    en: 'Reply',
  },
  noComments: {
    zh: '暂无评论',
    en: 'No comments yet',
  },
  // 其他
  loading: {
    zh: '加载中...',
    en: 'Loading...',
  },
  error: {
    zh: '错误',
    en: 'Error',
  },
  retry: {
    zh: '重试',
    en: 'Retry',
  },
  refresh: {
    zh: '刷新',
    en: 'Refresh',
  },
  dismiss: {
    zh: '关闭',
    en: 'Dismiss',
  },
  prevPage: {
    zh: '上一页',
    en: 'Previous',
  },
  nextPage: {
    zh: '下一页',
    en: 'Next',
  },
  searchPosts: {
    zh: '搜索帖子标题...',
    en: 'Search posts...',
  },
  searchComments: {
    zh: '搜索评论作者...',
    en: 'Search by author...',
  },
  // 发帖表单相关
  createPostFailed: {
    zh: '创建帖子失败',
    en: 'Failed to create post',
  },
  titleRequired: {
    zh: '标题不能为空',
    en: 'Title is required',
  },
  contentRequired: {
    zh: '内容不能为空',
    en: 'Content is required',
  },
  linkRequired: {
    zh: '链接不能为空',
    en: 'Link is required',
  },
  title: {
    zh: '标题',
    en: 'Title',
  },
  enterTitle: {
    zh: '请输入标题',
    en: 'Enter title',
  },
  postType: {
    zh: '帖子类型',
    en: 'Post Type',
  },
  textPost: {
    zh: '文本帖子',
    en: 'Text Post',
  },
  linkPost: {
    zh: '链接帖子',
    en: 'Link Post',
  },
  content: {
    zh: '内容',
    en: 'Content',
  },
  enterContent: {
    zh: '请输入内容',
    en: 'Enter content',
  },
  link: {
    zh: '链接',
    en: 'Link',
  },
  enterLink: {
    zh: '请输入链接',
    en: 'Enter link',
  },
  create: {
    zh: '创建',
    en: 'Create',
  },
  creating: {
    zh: '创建中...',
    en: 'Creating...',
  },
  reset: {
    zh: '重置',
    en: 'Reset',
  },
  // 回复相关
  cancel: {
    zh: '取消',
    en: 'Cancel',
  },
  replyRequired: {
    zh: '回复内容不能为空',
    en: 'Reply content is required',
  },
  replyFailed: {
    zh: '回复失败',
    en: 'Failed to reply',
  },
  enterReply: {
    zh: '请输入回复内容',
    en: 'Enter your reply',
  },
  replying: {
    zh: '回复中...',
    en: 'Replying...',
  },
  submitReply: {
    zh: '提交回复',
    en: 'Submit Reply',
  },
  // 时间相关
  postedAt: {
    zh: '发布于',
    en: 'Posted',
  },
  by: {
    zh: '由',
    en: 'by',
  },
  // 链接帖子
  visitLink: {
    zh: '访问链接',
    en: 'Visit Link',
  },
  // 认证提示
  loginToPost: {
    zh: '登录后发帖',
    en: 'Login to Post',
  },
  loginToVote: {
    zh: '登录后投票',
    en: 'Login to Vote',
  },
  // 导航
  back: {
    zh: '返回',
    en: 'Back',
  },
  // 时间
  'time.justNow': {
    zh: '刚刚',
    en: 'Just now',
  },
  'time.minutesAgo': {
    zh: '分钟前',
    en: 'minutes ago',
  },
  'time.hoursAgo': {
    zh: '小时前',
    en: 'hours ago',
  },
  'time.daysAgo': {
    zh: '天前',
    en: 'days ago',
  },
  // 管理模块
  'admin.management': {
    zh: '帖子管理',
    en: 'Post Management',
  },
  'admin.searchPlaceholder': {
    zh: '搜索帖子标题...',
    en: 'Search posts...',
  },
  'admin.filterStatus': {
    zh: '状态筛选：',
    en: 'Status: ',
  },
  'admin.statusAll': {
    zh: '全部',
    en: 'All',
  },
  'admin.statusNormal': {
    zh: '正常',
    en: 'Normal',
  },
  'admin.statusHidden': {
    zh: '已隐藏',
    en: 'Hidden',
  },
  'admin.statusReview': {
    zh: '待审核',
    en: 'Under Review',
  },
  'admin.refresh': {
    zh: '刷新',
    en: 'Refresh',
  },
  'admin.noPosts': {
    zh: '暂无帖子',
    en: 'No posts',
  },
  'admin.loading': {
    zh: '加载中...',
    en: 'Loading...',
  },
  'admin.comments': {
    zh: '评论',
    en: 'comments',
  },
  'admin.points': {
    zh: '点',
    en: 'points',
  },
  'admin.pinned': {
    zh: '置顶',
    en: 'Pinned',
  },
  'admin.featured': {
    zh: '精华',
    en: 'Featured',
  },
  'admin.hidden': {
    zh: '已隐藏',
    en: 'Hidden',
  },
  'admin.underReview': {
    zh: '待审核',
    en: 'Under Review',
  },
  'admin.prevPage': {
    zh: '上一页',
    en: 'Previous',
  },
  'admin.nextPage': {
    zh: '下一页',
    en: 'Next',
  },
  'admin.pageInfo': {
    zh: '第 {current} / {total} 页',
    en: 'Page {current} of {total}',
  },
  'admin.pin': {
    zh: '置顶',
    en: 'Pin',
  },
  'admin.unpin': {
    zh: '取消置顶',
    en: 'Unpin',
  },
  'admin.feature': {
    zh: '加精',
    en: 'Feature',
  },
  'admin.unfeature': {
    zh: '取消加精',
    en: 'Unfeature',
  },
  'admin.hide': {
    zh: '隐藏',
    en: 'Hide',
  },
  'admin.show': {
    zh: '显示',
    en: 'Show',
  },
  'admin.setToReview': {
    zh: '设为待审核',
    en: 'Set Under Review',
  },
  'admin.approve': {
    zh: '审核通过',
    en: 'Approve',
  },
  'admin.categoryFilter': {
    zh: '分类筛选：',
    en: 'Category: ',
  },
  'admin.categoryAll': {
    zh: '全部分类',
    en: 'All Categories',
  },
  // 操作成功提示
  'admin.pinSuccess': {
    zh: '置顶成功',
    en: 'Post pinned',
  },
  'admin.unpinSuccess': {
    zh: '已取消置顶',
    en: 'Post unpinned',
  },
  'admin.featureSuccess': {
    zh: '加精成功',
    en: 'Post featured',
  },
  'admin.unfeatureSuccess': {
    zh: '已取消加精',
    en: 'Post unfeatured',
  },
  'admin.hideSuccess': {
    zh: '帖子已隐藏',
    en: 'Post hidden',
  },
  'admin.showSuccess': {
    zh: '帖子已显示',
    en: 'Post shown',
  },
  'admin.setReviewSuccess': {
    zh: '已设为待审核',
    en: 'Set to pending review',
  },
  'admin.approveSuccess': {
    zh: '审核通过',
    en: 'Post approved',
  },
  'admin.operationFailed': {
    zh: '操作失败',
    en: 'Operation failed',
  },
  'admin.operationSuccess': {
    zh: '操作成功',
    en: 'Operation successful',
  },
  // 操作进行中提示
  'admin.pinning': {
    zh: '正在置顶...',
    en: 'Pinning...',
  },
  'admin.unpinning': {
    zh: '正在取消置顶...',
    en: 'Unpinning...',
  },
  'admin.featuring': {
    zh: '正在加精...',
    en: 'Featuring...',
  },
  'admin.unfeaturing': {
    zh: '正在取消加精...',
    en: 'Unfeaturing...',
  },
  'admin.hiding': {
    zh: '正在隐藏...',
    en: 'Hiding...',
  },
  'admin.showing': {
    zh: '正在显示...',
    en: 'Showing...',
  },
  'admin.normal': {
    zh: '正常',
    en: 'Normal',
  },
  // 分类
  'category.discussion': {
    zh: '讨论',
    en: 'Discussion',
  },
  'category.question': {
    zh: '问答',
    en: 'Question',
  },
  'category.showcase': {
    zh: '展示',
    en: 'Showcase',
  },
  'category.news': {
    zh: '新闻',
    en: 'News',
  },
  'category.guide': {
    zh: '教程',
    en: 'Guide',
  },
  // 管理后台页面
  'admin.title': {
    zh: '管理后台',
    en: 'Admin Panel',
  },
  'admin.nav.overview': {
    zh: '概览',
    en: 'Overview',
  },
  'admin.nav.posts': {
    zh: '帖子管理',
    en: 'Posts',
  },
  'admin.nav.comments': {
    zh: '评论管理',
    en: 'Comments',
  },
  'admin.nav.members': {
    zh: '成员管理',
    en: 'Members',
  },
  'admin.verifying': {
    zh: '正在验证权限...',
    en: 'Verifying permissions...',
  },
  'admin.accessDenied': {
    zh: '访问受限',
    en: 'Access Denied',
  },
  'admin.accessDeniedDesc': {
    zh: '您没有权限访问管理后台',
    en: 'You do not have permission to access the admin panel',
  },
  'admin.backToForum': {
    zh: '返回论坛',
    en: 'Back to Forum',
  },
  'admin.overview': {
    zh: '管理概览',
    en: 'Overview',
  },
  'admin.commentsManagement': {
    zh: '评论管理',
    en: 'Comments Management',
  },
  'admin.membersManagement': {
    zh: '成员管理',
    en: 'Members Management',
  },
  'admin.permissions': {
    zh: '权限说明',
    en: 'Permissions',
  },
  'admin.comingSoon': {
    zh: '功能开发中...',
    en: 'Coming Soon...',
  },
  'admin.commentsComingSoonDesc': {
    zh: '将支持：查看隐藏评论、恢复评论等操作',
    en: 'Will support: view hidden comments, restore comments, etc.',
  },
  'admin.membersComingSoonDesc': {
    zh: '将支持：封禁用户、权限授予等操作',
    en: 'Will support: ban users, grant permissions, etc.',
  },
  // 统计
  'admin.stats.pendingReview': {
    zh: '待审核帖子',
    en: 'Pending Review',
  },
  'admin.stats.hiddenComments': {
    zh: '隐藏评论',
    en: 'Hidden Comments',
  },
  'admin.stats.bannedUsers': {
    zh: '封禁用户',
    en: 'Banned Users',
  },
  'admin.stats.totalMembers': {
    zh: '总成员数',
    en: 'Total Members',
  },
  // 权限名称
  'admin.perm.pin': {
    zh: '置顶帖子',
    en: 'Pin Posts',
  },
  'admin.perm.feature': {
    zh: '加精帖子',
    en: 'Feature Posts',
  },
  'admin.perm.hide': {
    zh: '隐藏帖子',
    en: 'Hide Posts',
  },
  'admin.perm.review': {
    zh: '审核帖子',
    en: 'Review Posts',
  },
  'admin.perm.deletePost': {
    zh: '删除帖子',
    en: 'Delete Posts',
  },
  'admin.perm.deleteComment': {
    zh: '删除评论',
    en: 'Delete Comments',
  },
  'admin.perm.viewHidden': {
    zh: '查看隐藏评论',
    en: 'View Hidden Comments',
  },
  'admin.perm.manageCategory': {
    zh: '管理分类',
    en: 'Manage Categories',
  },
  'admin.perm.banUser': {
    zh: '封禁用户',
    en: 'Ban Users',
  },
  'admin.perm.viewBanned': {
    zh: '查看封禁用户',
    en: 'View Banned Users',
  },
  'admin.perm.adminAccess': {
    zh: '管理后台访问',
    en: 'Admin Access',
  },
  'admin.perm.grantPermission': {
    zh: '授予权限',
    en: 'Grant Permissions',
  },
}

registerTranslations('forum', forumTranslations)
