-- ============================================
-- 权限系统设计说明
-- ============================================
-- 
-- 权限架构：
-- 1. User.is_admin_user - 系统级超级管理员
--    - 存储在 users 表
--    - 超级管理员自动拥有所有权限
--    - 设置方式: UPDATE users SET is_admin_user = TRUE WHERE username = 'xxx';
--
-- 2. Member.permissions - 论坛模块细粒度权限
--    - 存储在 member 表的 permissions 字段 (JSON 数组)
--    - 用于非超级管理员的特定权限控制
--    - 设置方式: UPDATE member SET permissions = '["POST_PIN","POST_HIDE"]' WHERE member_id = 'xxx';
--
-- 权限检查顺序：
-- 1. 先检查 User.is_admin_user，如果是超级管理员直接放行
-- 2. 如果不是超级管理员，检查 Member.permissions
--
-- ============================================

-- 可用的权限点 (PermissionPoint):
-- ADMIN_ACCESS - 管理后台访问
-- PERMISSION_GRANT - 授予权限
-- POST_PIN - 置顶帖子
-- POST_FEATURE - 加精帖子
-- POST_HIDE - 隐藏帖子
-- POST_REVIEW - 审核帖子
-- POST_DELETE_ANY - 删除任意帖子
-- COMMENT_DELETE_ANY - 删除任意评论
-- COMMENT_VIEW_HIDDEN - 查看隐藏评论
-- CATEGORY_MANAGE - 管理分类
-- USER_BAN - 封禁用户
-- USER_VIEW_BANNED - 查看封禁用户

-- ============================================
-- 设置 YaeSaraki 为超级管理员
-- ============================================

-- 方式一：设置为超级管理员（推荐 - 自动拥有所有权限）
-- 找到用户 ID 后执行：
-- UPDATE users SET is_admin_user = TRUE WHERE username = 'YaeSaraki';

-- 方式二：仅授予论坛特定权限（不设置超级管理员）
-- 找到 member_id 后执行：
-- UPDATE member 
-- SET permissions = '["ADMIN_ACCESS","POST_PIN","POST_FEATURE","POST_HIDE","POST_REVIEW","POST_DELETE_ANY","COMMENT_DELETE_ANY","COMMENT_VIEW_HIDDEN","CATEGORY_MANAGE","USER_BAN","USER_VIEW_BANNED"]'
-- WHERE nickname = 'YaeSaraki';

-- ============================================
-- 查询当前权限状态
-- ============================================
SELECT 
    u.id as user_id,
    u.username,
    u.is_admin_user as is_super_admin,
    m.member_id,
    m.nickname,
    m.permissions as forum_permissions
FROM users u
LEFT JOIN member m ON m.user_id = u.id
WHERE u.username = 'YaeSaraki';
