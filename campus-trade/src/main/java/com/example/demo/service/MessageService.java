package com.example.demo.service;

import com.example.demo.entity.MessageRecord;
import com.example.demo.entity.MessageTemplate;

import java.util.List;
import java.util.Map;

public interface MessageService {

    MessageTemplate addTemplate(MessageTemplate template);
    
    MessageTemplate updateTemplate(MessageTemplate template);
    
    MessageTemplate getTemplateByCode(String templateCode);
    
    List<MessageTemplate> getAllTemplates();
    
    void deleteTemplate(Long id);
    
    MessageRecord sendMessage(Long userId, String templateCode, Map<String, Object> variables);
    
    MessageRecord sendMessageDirect(Long userId, String title, String content);
    
    List<MessageRecord> getMessagesByUserId(Long userId);
    
    List<MessageRecord> getUnreadMessages(Long userId);
    
    boolean markAsRead(Long recordId);
    
    int markAllAsRead(Long userId);
}