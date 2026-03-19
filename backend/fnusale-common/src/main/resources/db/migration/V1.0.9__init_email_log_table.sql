-- 邮件发送日志表
CREATE TABLE IF NOT EXISTS t_email_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    to_email VARCHAR(255) NOT NULL COMMENT '收件人邮箱',
    subject VARCHAR(255) NOT NULL COMMENT '邮件主题',
    content TEXT COMMENT '邮件内容',
    send_status VARCHAR(20) NOT NULL COMMENT '发送状态：SUCCESS-成功，FAILED-失败',
    error_message TEXT COMMENT '错误信息',
    send_time DATETIME NOT NULL COMMENT '发送时间',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    INDEX idx_to_email (to_email),
    INDEX idx_send_time (send_time),
    INDEX idx_send_status (send_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件发送日志表';
