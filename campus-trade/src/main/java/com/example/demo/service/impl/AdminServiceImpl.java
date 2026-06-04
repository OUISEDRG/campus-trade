package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.OrderInfoDTO;
import com.example.demo.dto.UserDetailsDTO;
import com.example.demo.entity.Goods;
import com.example.demo.entity.Orders;
import com.example.demo.entity.User;
import com.example.demo.mapper.GoodsMapper;
import com.example.demo.mapper.OrdersMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.AdminService;
import com.example.demo.service.GoodsService;
import com.example.demo.service.OrdersService;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminServiceImpl extends ServiceImpl<UserMapper, User> implements AdminService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private GoodsMapper goodsMapper;
    
    @Autowired
    private OrdersMapper ordersMapper;
    
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
        
        if (orders.isEmpty()) return List.of();
        
        // 批量收集所有 sellerId 和 goodsId
        Set<Long> sellerIds = orders.stream().map(Orders::getSellerId).collect(Collectors.toSet());
        Set<Integer> goodsIds = orders.stream().map(Orders::getGoodsId).collect(Collectors.toSet());
        
        // 一次性批量查询
        Map<Long, User> sellerMap = sellerIds.isEmpty() ? Map.of() :
            userService.listByIds(sellerIds).stream().collect(Collectors.toMap(User::getId, u -> u));
        Map<Integer, Goods> goodsMap = goodsIds.isEmpty() ? Map.of() :
            goodsService.listByIds(goodsIds).stream().collect(Collectors.toMap(Goods::getId, g -> g));
        
        // 买家信息只查一次
        User buyer = userService.getById(buyerId);
        
        return orders.stream().map(order -> {
            OrderInfoDTO dto = new OrderInfoDTO();
            dto.setOrderId(order.getId());
            dto.setGoodsId(order.getGoodsId());
            dto.setPrice(order.getPrice());
            dto.setCreateTime(order.getCreateTime());
            dto.setBuyerId(order.getBuyerId());
            dto.setSellerId(order.getSellerId());
            
            if (buyer != null) {
                dto.setBuyerName(buyer.getName());
                dto.setBuyerUsername(buyer.getUsername());
                dto.setBuyerPhone(buyer.getPhone());
                dto.setBuyerCollege(buyer.getCollege());
            }
            
            User seller = sellerMap.get(order.getSellerId());
            if (seller != null) {
                dto.setSellerName(seller.getName());
                dto.setSellerUsername(seller.getUsername());
                dto.setSellerPhone(seller.getPhone());
                dto.setSellerCollege(seller.getCollege());
            }
            
            Goods goods = goodsMap.get(order.getGoodsId());
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
        
        if (orders.isEmpty()) return List.of();
        
        // 批量收集所有 buyerId 和 goodsId
        Set<Long> buyerIds = orders.stream().map(Orders::getBuyerId).collect(Collectors.toSet());
        Set<Integer> goodsIds = orders.stream().map(Orders::getGoodsId).collect(Collectors.toSet());
        
        // 一次性批量查询
        Map<Long, User> buyerMap = buyerIds.isEmpty() ? Map.of() :
            userService.listByIds(buyerIds).stream().collect(Collectors.toMap(User::getId, u -> u));
        Map<Integer, Goods> goodsMap = goodsIds.isEmpty() ? Map.of() :
            goodsService.listByIds(goodsIds).stream().collect(Collectors.toMap(Goods::getId, g -> g));
        
        User seller = userService.getById(sellerId);
        
        return orders.stream().map(order -> {
            OrderInfoDTO dto = new OrderInfoDTO();
            dto.setOrderId(order.getId());
            dto.setGoodsId(order.getGoodsId());
            dto.setPrice(order.getPrice());
            dto.setCreateTime(order.getCreateTime());
            dto.setBuyerId(order.getBuyerId());
            dto.setSellerId(order.getSellerId());
            
            User buyer = buyerMap.get(order.getBuyerId());
            if (buyer != null) {
                dto.setBuyerName(buyer.getName());
                dto.setBuyerUsername(buyer.getUsername());
                dto.setBuyerPhone(buyer.getPhone());
                dto.setBuyerCollege(buyer.getCollege());
            }
            
            if (seller != null) {
                dto.setSellerName(seller.getName());
                dto.setSellerUsername(seller.getUsername());
                dto.setSellerPhone(seller.getPhone());
                dto.setSellerCollege(seller.getCollege());
            }
            
            Goods goods = goodsMap.get(order.getGoodsId());
            if (goods != null) {
                dto.setGoodsTitle(goods.getTitle());
                dto.setGoodsImageUrl(goods.getImageUrl());
                dto.setOrderStatus(goods.getStatus() == 0 ? "交易中" : "已完成");
                dto.setStatusCode(goods.getStatus());
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long userCount = userMapper.selectCount(null);
        long goodsCount = goodsMapper.selectCount(null);
        long orderCount = ordersMapper.selectCount(null);
        
        QueryWrapper<Orders> todayOrders = new QueryWrapper<>();
        todayOrders.ge("create_time", LocalDate.now().atStartOfDay());
        long todayOrderCount = ordersMapper.selectCount(todayOrders);
        
        QueryWrapper<Orders> totalAmount = new QueryWrapper<>();
        totalAmount.select("SUM(price) as total");
        List<Map<String, Object>> amountResult = ordersMapper.selectMaps(totalAmount);
        BigDecimal totalRevenue = amountResult.isEmpty() ? BigDecimal.ZERO : 
            (BigDecimal) amountResult.get(0).get("total");
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        
        QueryWrapper<User> todayUsers = new QueryWrapper<>();
        todayUsers.ge("create_time", LocalDate.now().atStartOfDay());
        long todayUserCount = userMapper.selectCount(todayUsers);
        
        stats.put("totalUsers", userCount);
        stats.put("totalGoods", goodsCount);
        stats.put("totalOrders", orderCount);
        stats.put("todayOrders", todayOrderCount);
        stats.put("totalRevenue", totalRevenue);
        stats.put("todayUsers", todayUserCount);
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getWeeklyStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            
            QueryWrapper<Orders> qw = new QueryWrapper<>();
            qw.between("create_time", start, end)
              .select("COUNT(*) as count", "SUM(price) as amount");
            
            List<Map<String, Object>> dayStats = ordersMapper.selectMaps(qw);
            long count = dayStats.isEmpty() ? 0 : ((Number) dayStats.get(0).get("count")).longValue();
            BigDecimal amount = dayStats.isEmpty() ? BigDecimal.ZERO : 
                (BigDecimal) dayStats.get(0).get("amount");
            if (amount == null) amount = BigDecimal.ZERO;
            
            Map<String, Object> day = new HashMap<>();
            day.put("date", date.format(DateTimeFormatter.ofPattern("MM-dd")));
            day.put("count", count);
            day.put("amount", amount);
            result.add(day);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getMonthlyStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 11; i >= 0; i--) {
            LocalDate date = today.minusMonths(i);
            int year = date.getYear();
            int month = date.getMonthValue();
            
            QueryWrapper<Orders> qw = new QueryWrapper<>();
            qw.apply("YEAR(create_time) = {0} AND MONTH(create_time) = {1}", year, month)
              .select("COUNT(*) as count", "SUM(price) as amount");
            
            List<Map<String, Object>> monthStats = ordersMapper.selectMaps(qw);
            long count = monthStats.isEmpty() ? 0 : ((Number) monthStats.get(0).get("count")).longValue();
            BigDecimal amount = monthStats.isEmpty() ? BigDecimal.ZERO : 
                (BigDecimal) monthStats.get(0).get("amount");
            if (amount == null) amount = BigDecimal.ZERO;
            
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", year + "-" + String.format("%02d", month));
            monthData.put("count", count);
            monthData.put("amount", amount);
            result.add(monthData);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getCategoryStats() {
        QueryWrapper<Goods> qw = new QueryWrapper<>();
        qw.select("category_name as category", "COUNT(*) as count")
          .groupBy("category_name")
          .orderByDesc("count");
        
        return goodsMapper.selectMaps(qw);
    }

    @Override
    public List<Map<String, Object>> getTopSellers() {
        QueryWrapper<Orders> qw = new QueryWrapper<>();
        qw.select("seller_id", "COUNT(*) as sales_count", "SUM(price) as total_sales")
          .groupBy("seller_id")
          .orderByDesc("total_sales")
          .last("LIMIT 10");
        
        List<Map<String, Object>> sellerStats = ordersMapper.selectMaps(qw);
        
        for (Map<String, Object> stat : sellerStats) {
            Long sellerId = ((Number) stat.get("seller_id")).longValue();
            User seller = userMapper.selectById(sellerId);
            if (seller != null) {
                stat.put("username", seller.getUsername());
                stat.put("name", seller.getName());
            }
        }
        
        return sellerStats;
    }

    @Override
    public List<Map<String, Object>> getRecentOrders() {
        QueryWrapper<Orders> qw = new QueryWrapper<>();
        qw.orderByDesc("create_time")
          .last("LIMIT 20");
        
        List<Orders> orders = ordersMapper.selectList(qw);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Orders order : orders) {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.getId());
            orderMap.put("goodsId", order.getGoodsId());
            orderMap.put("price", order.getPrice());
            orderMap.put("createTime", order.getCreateTime());
            orderMap.put("status", order.getStatus());
            
            Goods goods = goodsMapper.selectById(order.getGoodsId());
            if (goods != null) {
                orderMap.put("goodsTitle", goods.getTitle());
            }
            
            User buyer = userMapper.selectById(order.getBuyerId());
            if (buyer != null) {
                orderMap.put("buyerName", buyer.getName());
            }
            
            User seller = userMapper.selectById(order.getSellerId());
            if (seller != null) {
                orderMap.put("sellerName", seller.getName());
            }
            
            result.add(orderMap);
        }
        
        return result;
    }
}
