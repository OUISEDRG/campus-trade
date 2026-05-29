
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("goods")
public class Goods {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Long userId;
    private String title;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Integer status;
    private String categoryName;
}
