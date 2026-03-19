package com.fnusale.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.ImSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天会话Mapper
 */
@Mapper
public interface ImSessionMapper extends BaseMapper<ImSession> {

    /**
     * 根据用户和商品查询会话
     */
    @Select("SELECT * FROM t_im_session WHERE ((user1_id = #{userId} AND user2_id = #{targetUserId}) " +
            "OR (user1_id = #{targetUserId} AND user2_id = #{userId})) " +
            "AND product_id = #{productId} AND session_status = 'NORMAL'")
    ImSession selectByUsersAndProduct(@Param("userId") Long userId,
                                      @Param("targetUserId") Long targetUserId,
                                      @Param("productId") Long productId);

    /**
     * 查询用户的所有会话（包含置顶信息，排除已删除的会话）
     */
    @Select("SELECT * FROM t_im_session WHERE (user1_id = #{userId} OR user2_id = #{userId}) " +
            "AND session_status = 'NORMAL' " +
            "AND (user1_id = #{userId} AND is_deleted_u1 = 0 OR user2_id = #{userId} AND is_deleted_u2 = 0) " +
            "ORDER BY " +
            "CASE WHEN user1_id = #{userId} THEN is_pinned_u1 ELSE is_pinned_u2 END DESC, " +
            "CASE WHEN user1_id = #{userId} THEN pinned_time_u1 ELSE pinned_time_u2 END DESC, " +
            "last_message_time DESC")
    List<ImSession> selectByUserId(@Param("userId") Long userId);

    /**
     * 统计用户置顶会话数量
     */
    @Select("SELECT COUNT(*) FROM t_im_session WHERE " +
            "(user1_id = #{userId} AND is_pinned_u1 = 1) OR " +
            "(user2_id = #{userId} AND is_pinned_u2 = 1)")
    int countPinnedByUserId(@Param("userId") Long userId);

    /**
     * 原子更新：增加用户1的未读数并更新最后消息
     */
    @Update("UPDATE t_im_session SET unread_count_u1 = unread_count_u1 + 1, " +
            "last_message_content = #{lastMessage}, last_message_time = #{lastMessageTime}, " +
            "update_time = NOW() WHERE id = #{sessionId}")
    int incrementUnreadU1(@Param("sessionId") Long sessionId,
                          @Param("lastMessage") String lastMessage,
                          @Param("lastMessageTime") LocalDateTime lastMessageTime);

    /**
     * 原子更新：增加用户2的未读数并更新最后消息
     */
    @Update("UPDATE t_im_session SET unread_count_u2 = unread_count_u2 + 1, " +
            "last_message_content = #{lastMessage}, last_message_time = #{lastMessageTime}, " +
            "update_time = NOW() WHERE id = #{sessionId}")
    int incrementUnreadU2(@Param("sessionId") Long sessionId,
                          @Param("lastMessage") String lastMessage,
                          @Param("lastMessageTime") LocalDateTime lastMessageTime);

    /**
     * 原子更新：重置用户1的未读数为0
     */
    @Update("UPDATE t_im_session SET unread_count_u1 = 0, update_time = NOW() WHERE id = #{sessionId}")
    int resetUnreadU1(@Param("sessionId") Long sessionId);

    /**
     * 原子更新：重置用户2的未读数为0
     */
    @Update("UPDATE t_im_session SET unread_count_u2 = 0, update_time = NOW() WHERE id = #{sessionId}")
    int resetUnreadU2(@Param("sessionId") Long sessionId);

    /**
     * 软删除会话（用户1删除）
     */
    @Update("UPDATE t_im_session SET is_deleted_u1 = 1, update_time = NOW() WHERE id = #{sessionId}")
    int softDeleteU1(@Param("sessionId") Long sessionId);

    /**
     * 软删除会话（用户2删除）
     */
    @Update("UPDATE t_im_session SET is_deleted_u2 = 1, update_time = NOW() WHERE id = #{sessionId}")
    int softDeleteU2(@Param("sessionId") Long sessionId);

    /**
     * 恢复会话（用户1收到新消息时恢复）
     */
    @Update("UPDATE t_im_session SET is_deleted_u1 = 0, update_time = NOW() WHERE id = #{sessionId}")
    int restoreU1(@Param("sessionId") Long sessionId);

    /**
     * 恢复会话（用户2收到新消息时恢复）
     */
    @Update("UPDATE t_im_session SET is_deleted_u2 = 0, update_time = NOW() WHERE id = #{sessionId}")
    int restoreU2(@Param("sessionId") Long sessionId);
}