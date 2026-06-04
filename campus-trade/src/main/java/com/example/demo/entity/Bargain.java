package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("bargain")
public class Bargain {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long goodsId;
    
    private Long userId;
    
    private BigDecimal originalPrice;
    
    private BigDecimal currentPrice;
    
    private BigDecimal targetPrice;
    
    private Integer status;
    
    private Integer maxParticipants;
    
    private Integer currentParticipants;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private LocalDateTime createTime;
    
    @TableField("is_deleted")
    private Integer isDeleted;
}