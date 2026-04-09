-- ============================================
-- 初始管理员设置脚本
-- ============================================
-- 使用方法：
-- 1. 先查询你的用户 ID：
--    SELECT id, username FROM users WHERE username = '你的用户名';
-- 
-- 2. 查询对应的 member ID：
--    SELECT member_id, user_id, nickname FROM member WHERE user_id = '你的用户ID';
--
-- 3. 执行下面的授权语句（替换 YOUR_MEMBER_ID 为实际的 member_id）
-- ============================================

-- 方式一：设置为超级管理员（推荐）
-- 超级管理员自动拥有所有权限，无需单独授予
UPDATE member 
SET is_admin_user = 1 
WHERE member_id = 'YOUR_MEMBER_ID';

-- 方式二：授予特定的权限点（更细粒度控制）
-- UPDATE member 
-- SET permissions = '["ADMIN_ACCESS","PERMISSION_GRANT","POST_PIN","POST_FEATURE","POST_HIDE","POST_REVIEW","POST_DELETE_ANY","COMMENT_DELETE_ANY","COMMENT_VIEW_HIDDEN","CATEGORY_MANAGE","USER_BAN","USER_VIEW_BANNED"]'
-- WHERE member_id = 'YOUR_MEMBER_ID';

-- 验证结果
SELECT 
    m.member_id,
    m.nickname,
    m.is_admin_user,
    m.permissions,
    u.username
FROM member m
JOIN users u ON m.user_id = u.id
WHERE m.member_id = 'YOUR_MEMBER_ID';
