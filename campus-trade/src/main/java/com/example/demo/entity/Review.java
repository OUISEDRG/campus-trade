package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer orderId;      // 关联订单ID
    private Long reviewerId;      // 评价人ID
    private Long targetUserId;    // 被评价人ID
    private Integer rating;       // 评分 1-5
    private String content;       // 评价内容
    private LocalDateTime createTime;
}