package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_email_log")
public class EmailLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String toEmail;

    private String subject;

    private String content;

    private String sendStatus;

    private String errorMessage;

    private LocalDateTime sendTime;

    private Integer retryCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
