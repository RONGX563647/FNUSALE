package com.fnusale.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.ImUserQuickReply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户自定义快捷回复Mapper
 */
@Mapper
public interface ImUserQuickReplyMapper extends BaseMapper<ImUserQuickReply> {

    /**
     * 查询用户的自定义快捷回复
     */
    @Select("SELECT * FROM t_im_user_quick_reply WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<ImUserQuickReply> selectByUserId(@Param("userId") Long userId);

    /**
     * 统计用户自定义快捷回复数量
     */
    @Select("SELECT COUNT(*) FROM t_im_user_quick_reply WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
}