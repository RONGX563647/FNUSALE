package com.fnusale.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fnusale.common.entity.ImMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 聊天消息Mapper
 */
@Mapper
public interface ImMessageMapper extends BaseMapper<ImMessage> {

    /**
     * 分页查询会话消息
     */
    @Select("SELECT * FROM t_im_message WHERE session_id = #{sessionId} AND is_deleted = 0 " +
            "ORDER BY send_time DESC")
    IPage<ImMessage> selectBySessionId(Page<ImMessage> page, @Param("sessionId") Long sessionId);

    /**
     * 查询会话最新消息
     */
    @Select("SELECT * FROM t_im_message WHERE session_id = #{sessionId} AND is_deleted = 0 " +
            "ORDER BY send_time DESC LIMIT 1")
    ImMessage selectLatestBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 标记会话消息已读
     */
    @Update("UPDATE t_im_message SET is_read = 1 WHERE session_id = #{sessionId} " +
            "AND receiver_id = #{userId} AND is_read = 0")
    int markAsReadBySession(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    /**
     * 统计未读消息数
     */
    @Select("SELECT COUNT(*) FROM t_im_message WHERE receiver_id = #{userId} " +
            "AND is_read = 0 AND is_deleted = 0")
    int countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 逻辑删除消息（撤回）
     */
    @Update("UPDATE t_im_message SET is_deleted = 1 WHERE id = #{messageId} AND sender_id = #{userId}")
    int logicDeleteByIdAndSender(@Param("messageId") Long messageId, @Param("userId") Long userId);

    /**
     * 查询消息发送时间
     */
    @Select("SELECT send_time FROM t_im_message WHERE id = #{messageId}")
    LocalDateTime selectSendTimeById(@Param("messageId") Long messageId);
}