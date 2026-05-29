
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Orders {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer goodsId;
    private Long buyerId;
    private Long sellerId;
    private BigDecimal price;
    private LocalDateTime createTime;
}
