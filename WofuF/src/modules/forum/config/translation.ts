// 论坛翻译
import {registerTranslations} from '@S/services/i18n'

const forumTranslations = {
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
  points: {
    zh: '积分',
    en: 'Points',
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
  // 发帖表单相关
  createPostFailed: {
    zh: '创建帖子失败',
    en: 'Failed to create post',
  },
  titleRequired: {
    zh: '标题不能为空',
    en: 'Title is required',
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
}

registerTranslations('forum', forumTranslations)
