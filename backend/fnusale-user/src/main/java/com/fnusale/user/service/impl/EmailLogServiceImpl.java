package com.fnusale.user.service.impl;

import com.fnusale.common.entity.EmailLog;
import com.fnusale.user.mapper.EmailLogMapper;
import com.fnusale.user.service.EmailLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailLogServiceImpl implements EmailLogService {

    private final EmailLogMapper emailLogMapper;

    @Override
    public void recordEmailLog(EmailLog emailLog) {
        try {
            emailLogMapper.insert(emailLog);
            log.debug("邮件日志记录成功 - toEmail: {}, status: {}", 
                emailLog.getToEmail(), emailLog.getSendStatus());
        } catch (Exception e) {
            log.error("邮件日志记录失败 - toEmail: {}, error: {}", 
                emailLog.getToEmail(), e.getMessage(), e);
        }
    }
}
