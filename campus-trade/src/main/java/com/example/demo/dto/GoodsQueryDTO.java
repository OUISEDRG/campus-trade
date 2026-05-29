package com.example.demo.dto;

import lombok.Data;

@Data
public class GoodsQueryDTO {
    
    private String keyword;
    
    private Long categoryId;
    
    private String categoryName;
}
