
package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.Orders;

public interface OrdersService extends IService<Orders> {
    void buy(Integer goodsId, Long buyerId);
}
