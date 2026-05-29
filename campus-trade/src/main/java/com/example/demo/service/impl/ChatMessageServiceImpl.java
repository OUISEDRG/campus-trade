package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.ChatSessionDTO;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.User;
import com.example.demo.mapper.ChatMessageMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.ChatMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<ChatMessage> getChatHistory(Long userId1, Long userId2) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(ChatMessage::getSenderId, userId1).eq(ChatMessage::getReceiverId, userId2))
               .or(w -> w.eq(ChatMessage::getSenderId, userId2).eq(ChatMessage::getReceiverId, userId1))
               .orderByAsc(ChatMessage::getCreateTime);
        
        markAsRead(userId1, userId2);
        
        return list(wrapper);
    }

    @Override
    public void saveMessage(ChatMessage message) {
        try {
            save(message);
            log.info("【Service层-消息保存成功】ID: {}, 发送方: {}, 接收方: {}, 内容: {}", 
                    message.getId(), message.getSenderId(), message.getReceiverId(), message.getContent());
        } catch (Exception e) {
            log.error("【Service层-消息保存失败】发送方: {}, 接收方: {}, 错误: {}", 
                    message.getSenderId(), message.getReceiverId(), e.getMessage());
            throw e;
        }
    }

    @Override
    public void markAsRead(Long senderId, Long receiverId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSenderId, senderId)
               .eq(ChatMessage::getReceiverId, receiverId)
               .eq(ChatMessage::getStatus, 0);
        
        ChatMessage updateMsg = new ChatMessage();
        updateMsg.setStatus(1);
        update(updateMsg, wrapper);
    }

    @Override
    public List<ChatMessage> getUnreadMessages(Long userId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getReceiverId, userId)
               .eq(ChatMessage::getStatus, 0)
               .orderByDesc(ChatMessage::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<ChatSessionDTO> getChatSessions(Long userId) {
        log.info("【Service层-查询会话列表】用户ID: {}", userId);
        
        List<ChatSessionDTO> sessions = baseMapper.selectChatSessions(userId);
        
        log.info("【Service层-会话列表查询结果】用户ID: {}, 查询到 {} 个会话", userId, sessions.size());
        
        for (ChatSessionDTO session : sessions) {
            User targetUser = userMapper.selectById(session.getTargetUserId());
            if (targetUser != null) {
                session.setTargetUserName(targetUser.getName());
                session.setTargetUserAvatar(targetUser.getAvatar());
            }
            
            ChatMessage lastMsg = baseMapper.selectLastMessage(userId, session.getTargetUserId());
            if (lastMsg != null) {
                session.setLastMessage(lastMsg.getContent());
                session.setLatestGoodsId(lastMsg.getGoodsId());
            }
        }
        
        return sessions;
    }

    @Override
    public void batchMarkAsRead(Long userId, List<Long> senderIds) {
        baseMapper.batchMarkAsRead(userId, senderIds);
        log.info("【批量标记已读】用户: {}, 已标记 {} 个联系人的消息", userId, senderIds.size());
    }
}
