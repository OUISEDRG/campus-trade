
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Orders {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer goodsId;
    private Long buyerId;
    private Long sellerId;
    private BigDecimal price;
    private LocalDateTime createTime;
    private Integer status;
    private LocalDateTime updateTime;
}
