package com.fnusale.im.service.impl;

import com.fnusale.common.entity.ImQuickReply;
import com.fnusale.common.entity.ImUserQuickReply;
import com.fnusale.common.exception.BusinessException;
import com.fnusale.common.util.UserContext;
import com.fnusale.common.vo.im.QuickReplyListVO;
import com.fnusale.common.vo.im.QuickReplyVO;
import com.fnusale.im.dto.QuickReplyCreateDTO;
import com.fnusale.im.mapper.ImQuickReplyMapper;
import com.fnusale.im.mapper.ImUserQuickReplyMapper;
import com.fnusale.im.service.ImQuickReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImQuickReplyServiceImpl implements ImQuickReplyService {

    private final ImQuickReplyMapper quickReplyMapper;
    private final ImUserQuickReplyMapper userQuickReplyMapper;

    private static final int MAX_USER_QUICK_REPLIES = 10;

    @Override
    public QuickReplyListVO getQuickReplyList() {
        Long userId = UserContext.getUserIdOrThrow();

        // 获取系统预设快捷回复
        List<ImQuickReply> systemReplies = quickReplyMapper.selectEnabled();
        List<QuickReplyVO> systemVOList = systemReplies.stream()
                .map(this::buildSystemQuickReplyVO)
                .collect(Collectors.toList());

        // 获取用户自定义快捷回复
        List<ImUserQuickReply> userReplies = userQuickReplyMapper.selectByUserId(userId);
        List<QuickReplyVO> userVOList = userReplies.stream()
                .map(this::buildUserQuickReplyVO)
                .collect(Collectors.toList());

        return QuickReplyListVO.builder()
                .systemReplies(systemVOList)
                .userReplies(userVOList)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addQuickReply(QuickReplyCreateDTO dto) {
        Long userId = UserContext.getUserIdOrThrow();

        // 检查数量限制
        int count = userQuickReplyMapper.countByUserId(userId);
        if (count >= MAX_USER_QUICK_REPLIES) {
            throw new BusinessException("自定义快捷回复最多" + MAX_USER_QUICK_REPLIES + "条");
        }

        // 创建快捷回复
        ImUserQuickReply quickReply = new ImUserQuickReply();
        quickReply.setUserId(userId);
        quickReply.setReplyContent(dto.getContent());
        quickReply.setCreateTime(LocalDateTime.now());

        userQuickReplyMapper.insert(quickReply);
        log.info("添加自定义快捷回复成功，userId: {}, id: {}", userId, quickReply.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuickReply(Long id) {
        Long userId = UserContext.getUserIdOrThrow();

        // 检查是否存在
        ImUserQuickReply quickReply = userQuickReplyMapper.selectById(id);
        if (quickReply == null) {
            throw new BusinessException("快捷回复不存在");
        }

        // 检查是否是用户自己的
        if (!userId.equals(quickReply.getUserId())) {
            throw new BusinessException("无权删除此快捷回复");
        }

        userQuickReplyMapper.deleteById(id);
        log.info("删除自定义快捷回复成功，userId: {}, id: {}", userId, id);
    }

    private QuickReplyVO buildSystemQuickReplyVO(ImQuickReply reply) {
        QuickReplyVO vo = new QuickReplyVO();
        vo.setId(reply.getId());
        vo.setContent(reply.getReplyContent());
        vo.setIsSystem(true);
        return vo;
    }

    private QuickReplyVO buildUserQuickReplyVO(ImUserQuickReply reply) {
        QuickReplyVO vo = new QuickReplyVO();
        vo.setId(reply.getId());
        vo.setContent(reply.getReplyContent());
        vo.setIsSystem(false);
        return vo;
    }
}