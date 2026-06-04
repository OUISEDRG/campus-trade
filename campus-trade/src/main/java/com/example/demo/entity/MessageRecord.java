package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message_record")
public class MessageRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    
    private String templateCode;
    
    private String title;
    
    private String content;
    
    private Integer status;
    
    private String pushType;
    
    private LocalDateTime createTime;
    
    private LocalDateTime readTime;
}