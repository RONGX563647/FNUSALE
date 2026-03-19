package com.fnusale.im.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fnusale.common.entity.ImQuickReply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 快捷回复模板Mapper
 */
@Mapper
public interface ImQuickReplyMapper extends BaseMapper<ImQuickReply> {

    /**
     * 查询启用的系统快捷回复
     */
    @Select("SELECT * FROM t_im_quick_reply WHERE enable_status = 1 ORDER BY sort ASC")
    List<ImQuickReply> selectEnabled();
}