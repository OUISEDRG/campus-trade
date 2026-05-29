package com.example.demo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderInfoDTO {
    private Integer orderId;
    private Integer goodsId;
    private String goodsTitle;
    private String goodsImageUrl;
    private BigDecimal price;
    private LocalDateTime createTime;
    
    private Long buyerId;
    private String buyerName;
    private String buyerUsername;
    private String buyerPhone;
    private String buyerCollege;
    
    private Long sellerId;
    private String sellerName;
    private String sellerUsername;
    private String sellerPhone;
    private String sellerCollege;
    
    private String orderStatus;
    private Integer statusCode;
}
