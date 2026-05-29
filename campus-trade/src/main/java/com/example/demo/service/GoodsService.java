
package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.dto.GoodsQueryDTO;
import com.example.demo.entity.Goods;

public interface GoodsService extends IService<Goods> {
    
    boolean offShelves(Long goodsId, Long currentUserId);
    
    IPage<Goods> getHomePageGoods(Page<Goods> page, GoodsQueryDTO queryDTO);
}
