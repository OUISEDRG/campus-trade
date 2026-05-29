package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.dto.ChatSessionDTO;
import com.example.demo.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    
    List<ChatMessage> getMessagesBetweenUsers(Long userId1, Long userId2);
    
    void markMessagesAsRead(Long senderId, Long receiverId);
    
    List<ChatSessionDTO> selectChatSessions(@Param("userId") Long userId);
    
    ChatMessage selectLastMessage(@Param("userId") Long userId, @Param("targetUserId") Long targetUserId);
    
    void batchMarkAsRead(@Param("userId") Long userId, @Param("senderIds") List<Long> senderIds);
}
