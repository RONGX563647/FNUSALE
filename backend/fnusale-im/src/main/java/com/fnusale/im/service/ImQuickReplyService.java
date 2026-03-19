package com.fnusale.im.service;

import com.fnusale.common.vo.im.QuickReplyListVO;
import com.fnusale.im.dto.QuickReplyCreateDTO;

/**
 * 快捷回复服务接口
 */
public interface ImQuickReplyService {

    /**
     * 获取快捷回复列表
     */
    QuickReplyListVO getQuickReplyList();

    /**
     * 添加自定义快捷回复
     */
    void addQuickReply(QuickReplyCreateDTO dto);

    /**
     * 删除自定义快捷回复
     */
    void deleteQuickReply(Long id);
}