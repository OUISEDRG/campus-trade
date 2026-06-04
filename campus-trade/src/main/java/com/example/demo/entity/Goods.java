
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private Integer goodsCondition;
    private Integer viewCount;
    private Integer campusId;

    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
    
    private LocalDateTime createTime;
}
