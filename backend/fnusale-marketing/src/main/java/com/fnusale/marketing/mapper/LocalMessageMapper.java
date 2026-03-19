package com.fnusale.marketing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.LocalMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地消息表 Mapper
 */
@Mapper
public interface LocalMessageMapper extends BaseMapper<LocalMessage> {

    /**
     * 查询待重试的消息
     */
    @Select("SELECT * FROM t_local_message WHERE status = 'PENDING' AND next_retry_time <= #{now} AND retry_count < max_retry_count ORDER BY create_time ASC LIMIT #{limit}")
    List<LocalMessage> selectPendingMessages(@Param("now") LocalDateTime now, @Param("limit") int limit);

    /**
     * 查询失败的消息（超过最大重试次数）
     */
    @Select("SELECT * FROM t_local_message WHERE status = 'FAILED' ORDER BY create_time DESC LIMIT #{limit}")
    List<LocalMessage> selectFailedMessages(@Param("limit") int limit);

    /**
     * 根据消息ID查询
     */
    @Select("SELECT * FROM t_local_message WHERE message_id = #{messageId}")
    LocalMessage selectByMessageId(@Param("messageId") String messageId);

    /**
     * 更新消息状态为已发送
     */
    @Update("UPDATE t_local_message SET status = 'SENT', update_time = NOW() WHERE id = #{id}")
    int updateToSent(@Param("id") Long id);

    /**
     * 更新消息状态为失败并增加重试次数
     */
    @Update("UPDATE t_local_message SET status = 'FAILED', retry_count = retry_count + 1, next_retry_time = #{nextRetryTime}, update_time = NOW() WHERE id = #{id}")
    int updateToFailed(@Param("id") Long id, @Param("nextRetryTime") LocalDateTime nextRetryTime);

    /**
     * 清理已发送的旧消息（保留7天）
     */
    @Update("DELETE FROM t_local_message WHERE status = 'SENT' AND create_time < #{beforeTime}")
    int cleanOldMessages(@Param("beforeTime") LocalDateTime beforeTime);
}