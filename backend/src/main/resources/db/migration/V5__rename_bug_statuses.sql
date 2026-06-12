UPDATE bug SET status = '已修复' WHERE status = '修复完成';
UPDATE bug SET status = '修复完成' WHERE status = '关闭';
