-- ============================================
-- 帖子管理模块数据库迁移脚本
-- 版本: V1.0
-- 日期: 2026-04-08
-- 描述: 添加帖子管理功能所需的数据库字段和表
-- ============================================

-- ==================== 1. Posts 表扩展 ====================
-- 添加帖子状态和管理相关字段

ALTER TABLE post
ADD COLUMN status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '帖子状态: NORMAL(正常), HIDDEN(已隐藏), UNDER_REVIEW(审核中)',
ADD COLUMN is_pinned BOOLEAN DEFAULT FALSE COMMENT '是否置顶',
ADD COLUMN is_featured BOOLEAN DEFAULT FALSE COMMENT '是否加精',
ADD COLUMN pinned_at DATETIME NULL COMMENT '置顶时间',
ADD COLUMN featured_at DATETIME NULL COMMENT '加精时间',
ADD COLUMN hidden_at DATETIME NULL COMMENT '隐藏时间',
ADD COLUMN hidden_by VARCHAR(36) NULL COMMENT '隐藏操作人ID';

-- 添加索引
CREATE INDEX idx_post_status ON post(status);
CREATE INDEX idx_post_pinned ON post(is_pinned, pinned_at);
CREATE INDEX idx_post_featured ON post(is_featured, featured_at);

-- ==================== 2. Members 表扩展 ====================
-- 添加权限和封禁相关字段

ALTER TABLE member
ADD COLUMN permissions JSON NULL COMMENT '权限点列表 (JSON数组)',
ADD COLUMN is_banned BOOLEAN DEFAULT FALSE COMMENT '是否被封禁',
ADD COLUMN banned_at DATETIME NULL COMMENT '封禁时间',
ADD COLUMN banned_until DATETIME NULL COMMENT '封禁截止时间',
ADD COLUMN banned_reason VARCHAR(500) NULL COMMENT '封禁原因',
ADD COLUMN banned_by VARCHAR(36) NULL COMMENT '封禁操作人ID';

-- 添加索引
CREATE INDEX idx_member_banned ON member(is_banned, banned_until);

-- ==================== 3. Comments 表扩展 ====================
-- 添加隐藏管理相关字段

ALTER TABLE comment
ADD COLUMN is_hidden BOOLEAN DEFAULT FALSE COMMENT '是否隐藏',
ADD COLUMN hidden_at DATETIME NULL COMMENT '隐藏时间',
ADD COLUMN hidden_by VARCHAR(36) NULL COMMENT '隐藏操作人ID';

-- 添加索引
CREATE INDEX idx_comment_hidden ON comment(is_hidden);

-- ==================== 4. 帖子分类配置表 ====================
-- 支持动态管理帖子分类

CREATE TABLE IF NOT EXISTS post_category_config (
    id VARCHAR(36) PRIMARY KEY,
    category_key VARCHAR(50) NOT NULL UNIQUE COMMENT '分类键',
    display_name VARCHAR(100) NOT NULL COMMENT '显示名称',
    description VARCHAR(500) NULL COMMENT '描述',
    icon VARCHAR(100) NULL COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 添加索引
CREATE INDEX idx_category_active_sort ON post_category_config(is_active, sort_order);

-- 初始化默认分类数据
INSERT INTO post_category_config (id, category_key, display_name, description, icon, sort_order) VALUES
(UUID(), 'DISCUSSION', '讨论', '讨论交流', 'chat', 1),
(UUID(), 'QUESTION', '求助', '寻求帮助', 'help-circle', 2),
(UUID(), 'SHARE', '分享', '分享内容', 'share-2', 3),
(UUID(), 'SHOWCASE', '展示', '作品展示', 'image', 4),
(UUID(), 'ANNOUNCEMENT', '公告', '官方公告', 'megaphone', 5),
(UUID(), 'GUIDE', '指南', '教程指南', 'book-open', 6);

-- ==================== 5. 管理操作日志表（可选） ====================
-- 记录管理操作历史

CREATE TABLE IF NOT EXISTS admin_action_log (
    id VARCHAR(36) PRIMARY KEY,
    admin_member_id VARCHAR(36) NOT NULL COMMENT '操作人ID',
    action_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    target_type VARCHAR(50) NOT NULL COMMENT '目标类型: POST, COMMENT, MEMBER, CATEGORY',
    target_id VARCHAR(36) NOT NULL COMMENT '目标ID',
    action_detail JSON NULL COMMENT '操作详情',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_admin_member (admin_member_id),
    INDEX idx_target (target_type, target_id),
    INDEX idx_created_at (created_at)
);

-- ==================== 完成提示 ====================
-- 执行完成后请验证:
-- 1. 检查 post 表新增字段: DESC post;
-- 2. 检查 member 表新增字段: DESC member;
-- 3. 检查 comment 表新增字段: DESC comment;
-- 4. 检查分类表数据: SELECT * FROM post_category_config;
