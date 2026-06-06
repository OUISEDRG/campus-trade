package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.entity.MessageRecord;
import com.example.demo.entity.MessageTemplate;
import com.example.demo.exception.CustomException;
import com.example.demo.mapper.MessageRecordMapper;
import com.example.demo.mapper.MessageTemplateMapper;
import com.example.demo.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageTemplateMapper templateMapper;
    
    @Autowired
    private MessageRecordMapper recordMapper;

    @Override
    @Transactional
    public MessageTemplate addTemplate(MessageTemplate template) {
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        templateMapper.insert(template);
        return template;
    }

    @Override
    @Transactional
    public MessageTemplate updateTemplate(MessageTemplate template) {
        template.setUpdateTime(LocalDateTime.now());
        templateMapper.updateById(template);
        return template;
    }

    @Override
    public MessageTemplate getTemplateByCode(String templateCode) {
        return templateMapper.selectOne(new QueryWrapper<MessageTemplate>()
            .eq("template_code", templateCode)
            .eq("status", 0));
    }

    @Override
    public List<MessageTemplate> getAllTemplates() {
        return templateMapper.selectList(new QueryWrapper<MessageTemplate>()
            .eq("status", 0)
            .orderByDesc("create_time"));
    }

    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        MessageTemplate template = templateMapper.selectById(id);
        if (template != null) {
            template.setStatus(1);
            templateMapper.updateById(template);
        }
    }

    @Override
    @Transactional
    public MessageRecord sendMessage(Long userId, String templateCode, Map<String, Object> variables) {
        MessageTemplate template = getTemplateByCode(templateCode);
        if (template == null) {
            throw new CustomException("模板不存在");
        }
        
        String content = renderTemplate(template.getContent(), variables);
        String title = renderTemplate(template.getTitle(), variables);
        String pushType = template.getType();
        if (pushType == null) {
            pushType = "system";
        }
        
        return createMessageRecord(userId, templateCode, title, content, pushType);
    }

    @Override
    @Transactional
    public MessageRecord sendMessageDirect(Long userId, String title, String content) {
        return createMessageRecord(userId, "DIRECT", title, content, "system");
    }

    @Override
    @Transactional
    public MessageRecord sendMessageDirect(Long userId, String title, String content, String pushType) {
        return createMessageRecord(userId, "DIRECT", title, content, pushType);
    }

    private MessageRecord createMessageRecord(Long userId, String templateCode, String title, String content, String pushType) {
        MessageRecord record = new MessageRecord();
        record.setUserId(userId);
        record.setTemplateCode(templateCode);
        record.setTitle(title);
        record.setContent(content);
        record.setStatus(0);
        record.setPushType(pushType);
        record.setCreateTime(LocalDateTime.now());
        
        recordMapper.insert(record);
        return record;
    }

    // 保留旧方法的兼容性
    private MessageRecord createMessageRecord(Long userId, String templateCode, String title, String content) {
        return createMessageRecord(userId, templateCode, title, content, "system");
    }

    private String renderTemplate(String template, Map<String, Object> variables) {
        if (template == null || variables == null || variables.isEmpty()) {
            return template;
        }
        
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            result = result.replace(placeholder, String.valueOf(entry.getValue()));
        }
        return result;
    }

    @Override
    public List<MessageRecord> getMessagesByUserId(Long userId) {
        return recordMapper.selectList(new QueryWrapper<MessageRecord>()
            .eq("user_id", userId)
            .orderByDesc("create_time"));
    }

    @Override
    public List<MessageRecord> getUnreadMessages(Long userId) {
        return recordMapper.selectList(new QueryWrapper<MessageRecord>()
            .eq("user_id", userId)
            .eq("status", 0)
            .orderByDesc("create_time"));
    }

    @Override
    @Transactional
    public boolean markAsRead(Long recordId) {
        MessageRecord record = recordMapper.selectById(recordId);
        if (record != null) {
            record.setStatus(1);
            record.setReadTime(LocalDateTime.now());
            return recordMapper.updateById(record) > 0;
        }
        return false;
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        MessageRecord record = new MessageRecord();
        record.setStatus(1);
        record.setReadTime(LocalDateTime.now());
        
        return recordMapper.update(record, new QueryWrapper<MessageRecord>()
            .eq("user_id", userId)
            .eq("status", 0));
    }
}