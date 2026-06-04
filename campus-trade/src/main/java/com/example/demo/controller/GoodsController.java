
package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.Goods;
import com.example.demo.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @PostMapping("/publish")
    public Result publishGoods(@RequestBody Goods goods) {
        goods.setStatus(0);
        boolean success = goodsService.save(goods);
        
        if (success) {
            return Result.success("太棒了！闲置发布成功！");
        } else {
            return Result.error("发布失败，请稍后再试哦~");
        }
    }

    @PostMapping("/update")
    public Result updateGoods(@RequestBody Goods goods) {
        boolean success = goodsService.updateById(goods);
        if (success) {
            return Result.success("宝贝信息修改成功！");
        } else {
            return Result.error("修改失败，请稍微再试一下哦");
        }
    }

    // 🌟 奶奶级注释：专门给首页大门提供随机展示宝贝的接口
    @GetMapping("/carousel")
    public Result getCarouselGoods() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper();
        
        // 库管大爷的挑选规则：
        // 1. 必须是没卖出去的 (status = 0)
        // 2. 必须得有照片，不然展台上光秃秃的难看
        queryWrapper.eq("status", 0);
        queryWrapper.isNotNull("image_url");
        queryWrapper.ne("image_url", "");
        
        // 在 Java 层面随机抽取 4 条，避免 SQL 注入风险
        List<Goods> allGoods = goodsService.list(queryWrapper);
        java.util.Collections.shuffle(allGoods);
        List<Goods> carousel = allGoods.stream().limit(4).collect(java.util.stream.Collectors.toList());
        
        return Result.success(carousel);
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteGoods(@PathVariable Integer id) {
        boolean success = goodsService.removeById(id);
        if (success) {
            return Result.success("宝贝已成功删除！");
        } else {
            return Result.error("删除失败，请稍微再试一下哦");
        }
    }

    @PutMapping("/{id}/off-shelves")
    public Result<?> offShelves(@PathVariable("id") Long id) {
        try {
            Goods goods = goodsService.getById(id);
            if (goods == null) {
                return Result.error("商品不存在");
            }
            goods.setStatus(1);
            boolean success = goodsService.updateById(goods);
            if (success) {
                return Result.success("商品下架成功", null);
            }
            return Result.error("下架失败，请稍后重试");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping({"/getAll", "/list"})
    public Result getAllGoods(@RequestParam(required = false) String keyword, 
                                           @RequestParam(required = false) String categoryName,
                                           @RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "12") Integer pageSize,
                                           @RequestParam(required = false) BigDecimal minPrice,
                                           @RequestParam(required = false) BigDecimal maxPrice,
                                           @RequestParam(required = false) String sortBy,
                                           @RequestParam(required = false) Integer campusId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Goods> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        
        // 🌟 只显示上架中的商品（status = 0）
        queryWrapper.eq("status", 0);
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(w -> w.like("title", keyword).or().like("description", keyword));
        }

        if (categoryName != null && !categoryName.equals("全部好物") && !categoryName.trim().isEmpty()) {
            queryWrapper.eq("category_name", categoryName);
        }
        
        // 价格区间筛选
        if (minPrice != null) {
            queryWrapper.ge("price", minPrice);
        }
        if (maxPrice != null) {
            queryWrapper.le("price", maxPrice);
        }
        
        // 校区筛选
        if (campusId != null && campusId > 0) {
            queryWrapper.eq("campus_id", campusId);
        }
        
        // 排序
        if ("price_asc".equals(sortBy)) {
            queryWrapper.orderByAsc("price");
        } else if ("price_desc".equals(sortBy)) {
            queryWrapper.orderByDesc("price");
        } else {
            queryWrapper.orderByDesc("id"); // 默认最新
        }
        
        // 分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Goods> goodsPage = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
        com.baomidou.mybatisplus.core.metadata.IPage<Goods> result = goodsService.page(goodsPage, queryWrapper);
        
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result getGoodsById(@PathVariable Integer id) {
        Goods goods = goodsService.getById(id);
        if (goods != null) {
            goods.setViewCount(goods.getViewCount() != null ? goods.getViewCount() + 1 : 1);
            goodsService.updateById(goods);
            return Result.success(goods);
        } else {
            return Result.error("找不到该商品");
        }
    }

    @GetMapping("/user/{userId}")
    public Result getByUserId(@PathVariable Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("id");
        return Result.success(goodsService.list(queryWrapper));
    }
}
