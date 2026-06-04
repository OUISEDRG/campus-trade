package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.entity.Favorite;
import com.example.demo.entity.Goods;
import com.example.demo.service.FavoriteService;
import com.example.demo.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;
    
    @Autowired
    private GoodsService goodsService;

    @PostMapping("/add")
    public Result<?> addFavorite(@RequestParam Long userId, @RequestParam Integer goodsId) {
        LambdaQueryWrapper<Favorite> qw = new LambdaQueryWrapper<>();
        qw.eq(Favorite::getUserId, userId).eq(Favorite::getGoodsId, goodsId);
        if (favoriteService.count(qw) > 0) {
            return Result.error("已收藏该商品");
        }
        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setGoodsId(goodsId);
        fav.setCreateTime(LocalDateTime.now());
        favoriteService.save(fav);
        return Result.success("收藏成功");
    }

    @DeleteMapping("/remove")
    public Result<?> removeFavorite(@RequestParam Long userId, @RequestParam Integer goodsId) {
        LambdaQueryWrapper<Favorite> qw = new LambdaQueryWrapper<>();
        qw.eq(Favorite::getUserId, userId).eq(Favorite::getGoodsId, goodsId);
        favoriteService.remove(qw);
        return Result.success("已取消收藏");
    }

    @GetMapping("/check")
    public Result<?> checkFavorite(@RequestParam Long userId, @RequestParam Integer goodsId) {
        LambdaQueryWrapper<Favorite> qw = new LambdaQueryWrapper<>();
        qw.eq(Favorite::getUserId, userId).eq(Favorite::getGoodsId, goodsId);
        return Result.success(favoriteService.count(qw) > 0);
    }

    @GetMapping("/list")
    public Result<?> listFavorites(@RequestParam Long userId) {
        LambdaQueryWrapper<Favorite> qw = new LambdaQueryWrapper<>();
        qw.eq(Favorite::getUserId, userId).orderByDesc(Favorite::getCreateTime);
        List<Favorite> favorites = favoriteService.list(qw);
        List<Integer> goodsIds = favorites.stream().map(Favorite::getGoodsId).collect(Collectors.toList());
        if (goodsIds.isEmpty()) return Result.success(List.of());
        List<Goods> goods = goodsService.listByIds(goodsIds);
        return Result.success(goods);
    }
}