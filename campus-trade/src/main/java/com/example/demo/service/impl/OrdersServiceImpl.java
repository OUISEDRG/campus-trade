
package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.Goods;
import com.example.demo.entity.Orders;
import com.example.demo.mapper.OrdersMapper;
import com.example.demo.service.GoodsService;
import com.example.demo.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private GoodsService goodsService;

    @Override
    @Transactional
    public void buy(Integer goodsId, Long buyerId) {
        Goods goods = goodsService.getById(goodsId);
        if (goods == null || goods.getStatus() == 1) {
            throw new RuntimeException("手慢了！商品已被抢购或不存在");
        }
        Orders order = new Orders();
        order.setGoodsId(goodsId);
        order.setBuyerId(buyerId);
        order.setSellerId(goods.getUserId());
        order.setPrice(goods.getPrice());
        order.setCreateTime(LocalDateTime.now());
        this.save(order);

        goods.setStatus(1);
        goodsService.updateById(goods);
    }
}
