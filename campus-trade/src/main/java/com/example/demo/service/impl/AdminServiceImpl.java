package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.OrderInfoDTO;
import com.example.demo.dto.UserDetailsDTO;
import com.example.demo.entity.Goods;
import com.example.demo.entity.Orders;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.AdminService;
import com.example.demo.service.GoodsService;
import com.example.demo.service.OrdersService;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminServiceImpl extends ServiceImpl<UserMapper, User> implements AdminService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private GoodsService goodsService;
    
    @Autowired
    private OrdersService ordersService;

    @Override
    public IPage<User> searchUsers(Page<User> page, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                     .or().like(User::getName, keyword)
                     .or().eq(User::getId, keyword)
                     .or().like(User::getEmail, keyword)
                     .or().like(User::getPhone, keyword)
                     .or().like(User::getStudentId, keyword));
        }
        wrapper.orderByDesc(User::getId);
        return userMapper.selectPage(page, wrapper);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        User user = new User();
        user.setId(userId);
        user.setStatus(status);
        userMapper.updateById(user);
        log.info("【管理员操作】账号状态更新 - 用户ID: {}, 新状态: {}", userId, status == 0 ? "正常" : "封禁");
    }

    @Override
    public UserDetailsDTO getUserCompleteDetails(Long userId) {
        UserDetailsDTO details = new UserDetailsDTO();
        
        User user = userMapper.selectById(userId);
        details.setBasicInfo(user);
        
        List<Goods> publishedGoods = goodsService.list(
            new LambdaQueryWrapper<Goods>().eq(Goods::getUserId, userId)
        );
        details.setPublishedGoods(publishedGoods);
        details.setTotalPublishedGoods(publishedGoods.size());
        
        List<OrderInfoDTO> boughtOrders = getDetailedOrdersByBuyerId(userId);
        details.setBoughtOrders(boughtOrders);
        details.setTotalBoughtOrders(boughtOrders.size());
        
        List<OrderInfoDTO> soldOrders = getDetailedOrdersBySellerId(userId);
        details.setSoldOrders(soldOrders);
        details.setTotalSoldOrders(soldOrders.size());
        
        return details;
    }

    private List<OrderInfoDTO> getDetailedOrdersByBuyerId(Long buyerId) {
        List<Orders> orders = ordersService.list(
            new LambdaQueryWrapper<Orders>().eq(Orders::getBuyerId, buyerId)
        );
        
        return orders.stream().map(order -> {
            OrderInfoDTO dto = new OrderInfoDTO();
            dto.setOrderId(order.getId());
            dto.setGoodsId(order.getGoodsId());
            dto.setPrice(order.getPrice());
            dto.setCreateTime(order.getCreateTime());
            dto.setBuyerId(order.getBuyerId());
            dto.setSellerId(order.getSellerId());
            
            User buyer = userService.getById(buyerId);
            if (buyer != null) {
                dto.setBuyerName(buyer.getName());
                dto.setBuyerUsername(buyer.getUsername());
                dto.setBuyerPhone(buyer.getPhone());
                dto.setBuyerCollege(buyer.getCollege());
            }
            
            User seller = userService.getById(order.getSellerId());
            if (seller != null) {
                dto.setSellerName(seller.getName());
                dto.setSellerUsername(seller.getUsername());
                dto.setSellerPhone(seller.getPhone());
                dto.setSellerCollege(seller.getCollege());
            }
            
            Goods goods = goodsService.getById(order.getGoodsId());
            if (goods != null) {
                dto.setGoodsTitle(goods.getTitle());
                dto.setGoodsImageUrl(goods.getImageUrl());
                dto.setOrderStatus(goods.getStatus() == 0 ? "交易中" : "已完成");
                dto.setStatusCode(goods.getStatus());
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

    private List<OrderInfoDTO> getDetailedOrdersBySellerId(Long sellerId) {
        List<Orders> orders = ordersService.list(
            new LambdaQueryWrapper<Orders>().eq(Orders::getSellerId, sellerId)
        );
        
        return orders.stream().map(order -> {
            OrderInfoDTO dto = new OrderInfoDTO();
            dto.setOrderId(order.getId());
            dto.setGoodsId(order.getGoodsId());
            dto.setPrice(order.getPrice());
            dto.setCreateTime(order.getCreateTime());
            dto.setBuyerId(order.getBuyerId());
            dto.setSellerId(order.getSellerId());
            
            User buyer = userService.getById(order.getBuyerId());
            if (buyer != null) {
                dto.setBuyerName(buyer.getName());
                dto.setBuyerUsername(buyer.getUsername());
                dto.setBuyerPhone(buyer.getPhone());
                dto.setBuyerCollege(buyer.getCollege());
            }
            
            User seller = userService.getById(sellerId);
            if (seller != null) {
                dto.setSellerName(seller.getName());
                dto.setSellerUsername(seller.getUsername());
                dto.setSellerPhone(seller.getPhone());
                dto.setSellerCollege(seller.getCollege());
            }
            
            Goods goods = goodsService.getById(order.getGoodsId());
            if (goods != null) {
                dto.setGoodsTitle(goods.getTitle());
                dto.setGoodsImageUrl(goods.getImageUrl());
                dto.setOrderStatus(goods.getStatus() == 0 ? "交易中" : "已完成");
                dto.setStatusCode(goods.getStatus());
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
}
