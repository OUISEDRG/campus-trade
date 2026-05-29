package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.GoodsQueryDTO;
import com.example.demo.entity.Goods;
import com.example.demo.mapper.GoodsMapper;
import com.example.demo.service.GoodsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean offShelves(Long goodsId, Long currentUserId) {
        Goods goods = this.getById(goodsId);
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!goods.getUserId().equals(currentUserId)) {
            throw new RuntimeException("无权操作他人商品");
        }

        Goods updateObj = new Goods();
        updateObj.setId(goodsId.intValue());
        updateObj.setStatus(1);
        
        return this.updateById(updateObj);
    }

    @Override
    public IPage<Goods> getHomePageGoods(Page<Goods> page, GoodsQueryDTO queryDTO) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(Goods::getStatus, 0);

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.like(Goods::getTitle, queryDTO.getKeyword())
                   .or()
                   .like(Goods::getDescription, queryDTO.getKeyword());
        }
        if (queryDTO.getCategoryName() != null && !queryDTO.getCategoryName().equals("全部好物") && !queryDTO.getCategoryName().trim().isEmpty()) {
            wrapper.eq(Goods::getCategoryName, queryDTO.getCategoryName());
        }

        wrapper.orderByDesc(Goods::getId);

        return this.page(page, wrapper);
    }
}
