
package com.example.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.entity.Comment;
import com.example.demo.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @PostMapping("/add")
    public Result add(@RequestBody Comment comment) {
        commentService.save(comment);
        return Result.success("留言成功！");
    }

    @GetMapping("/list")
    public Result list(@RequestParam Integer goodsId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("goods_id", goodsId);
        queryWrapper.orderByDesc("id");
        return Result.success(commentService.list(queryWrapper));
    }
}
