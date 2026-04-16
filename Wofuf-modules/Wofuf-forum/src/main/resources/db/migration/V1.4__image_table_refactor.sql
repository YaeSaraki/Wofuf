-- ============================================
-- 图片表结构优化
-- 1. 将 forum_image 表重命名为 image
-- 2. 将 uploader_id 改为 uploader_member_id，设置外键关联 member 表
-- 3. 删除冗余的 uploader_nickname 字段（现通过 member 关联获取）
-- ============================================

-- 0. 处理可能存在的旧数据：先清理不存在的 uploader_id 引用
-- 将 uploader_id 为空或不在 member 表中的记录设为 NULL
UPDATE forum_image
SET uploader_id = NULL
WHERE uploader_id IS NULL OR uploader_id = ''
   OR NOT EXISTS (SELECT 1 FROM member WHERE member_id = forum_image.uploader_id);

-- 1. 重命名表
RENAME TABLE forum_image TO image;

-- 2. 添加新列 uploader_member_id（允许为空）
ALTER TABLE image
ADD COLUMN uploader_member_id VARCHAR(36);

-- 3. 迁移数据：将 uploader_id 的值复制到 uploader_member_id
-- 只有有效的 uploader_id 才会被迁移
UPDATE image
SET uploader_member_id = uploader_id
WHERE uploader_id IS NOT NULL AND uploader_id != '';

-- 4. 删除旧列
ALTER TABLE image
DROP COLUMN uploader_id;

-- 5. 设置外键约束
ALTER TABLE image
ADD CONSTRAINT fk_image_uploader_member
FOREIGN KEY (uploader_member_id) REFERENCES member(member_id);

-- 6. 重建索引
DROP INDEX idx_image_uploader ON image;
CREATE INDEX idx_image_uploader_member ON image(uploader_member_id);
