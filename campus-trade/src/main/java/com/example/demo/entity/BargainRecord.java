package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("bargain_record")
public class BargainRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long bargainId;
    
    private Long userId;
    
    private BigDecimal cutAmount;
    
    private BigDecimal afterPrice;
    
    private LocalDateTime createTime;
}