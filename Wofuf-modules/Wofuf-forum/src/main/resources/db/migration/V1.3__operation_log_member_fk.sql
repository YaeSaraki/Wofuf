-- ============================================
-- 操作日志表结构优化
-- 1. 将 operator_id 改为 operator_member_id，设置外键关联 member 表
-- 2. 删除 operator_nickname 字段（现通过 member 关联获取）
-- ============================================

-- 0. 处理可能存在的旧数据：先清理不存在的 operator_id 引用
-- 将 operator_id 为空或不在 member 表中的记录设为 NULL
UPDATE operation_log
SET operator_id = NULL
WHERE operator_id IS NULL OR operator_id = ''
   OR NOT EXISTS (SELECT 1 FROM member WHERE member_id = operation_log.operator_id);

-- 1. 添加新列 operator_member_id（允许为空）
ALTER TABLE operation_log
ADD COLUMN operator_member_id VARCHAR(36);

-- 2. 迁移数据：将 operator_id 的值复制到 operator_member_id
-- 只有有效的 operator_id 才会被迁移
UPDATE operation_log
SET operator_member_id = operator_id
WHERE operator_id IS NOT NULL AND operator_id != '';

-- 3. 删除旧列和索引
ALTER TABLE operation_log
DROP COLUMN operator_id;

ALTER TABLE operation_log
DROP COLUMN operator_nickname;

-- 4. 设置外键约束（只有非空的 operator_member_id 才会被约束）
ALTER TABLE operation_log
ADD CONSTRAINT fk_operation_log_operator_member
FOREIGN KEY (operator_member_id) REFERENCES member(member_id);

-- 5. 重建索引
DROP INDEX idx_operation_log_operator ON operation_log;
CREATE INDEX idx_operation_log_operator ON operation_log(operator_member_id);
