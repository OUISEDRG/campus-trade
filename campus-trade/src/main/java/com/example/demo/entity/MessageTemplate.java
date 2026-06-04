package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message_template")
public class MessageTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String templateCode;
    
    private String templateName;
    
    private String title;
    
    private String content;
    
    private String type;
    
    private Integer status;
    
    private String variables;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}