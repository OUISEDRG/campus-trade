package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exchange")
public class Exchange {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long initiatorId;
    
    private Long targetUserId;
    
    private Long initiatorGoodsId;
    
    private Long targetGoodsId;
    
    private Integer status;
    
    private String message;
    
    private LocalDateTime createTime;
    
    private LocalDateTime respondTime;
}