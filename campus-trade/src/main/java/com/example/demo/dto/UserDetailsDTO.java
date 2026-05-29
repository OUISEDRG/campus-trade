package com.example.demo.dto;

import com.example.demo.entity.Goods;
import com.example.demo.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class UserDetailsDTO {
    private User basicInfo;
    private List<Goods> publishedGoods;
    private List<OrderInfoDTO> boughtOrders;
    private List<OrderInfoDTO> soldOrders;
    private Integer totalPublishedGoods;
    private Integer totalBoughtOrders;
    private Integer totalSoldOrders;
}
