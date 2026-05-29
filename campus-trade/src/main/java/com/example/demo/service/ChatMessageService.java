package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.dto.ChatSessionDTO;
import com.example.demo.entity.ChatMessage;

import java.util.List;

public interface ChatMessageService extends IService<ChatMessage> {
    
    List<ChatMessage> getChatHistory(Long userId1, Long userId2);
    
    void saveMessage(ChatMessage message);
    
    void markAsRead(Long senderId, Long receiverId);
    
    List<ChatMessage> getUnreadMessages(Long userId);
    
    List<ChatSessionDTO> getChatSessions(Long userId);
    
    void batchMarkAsRead(Long userId, List<Long> senderIds);
}
