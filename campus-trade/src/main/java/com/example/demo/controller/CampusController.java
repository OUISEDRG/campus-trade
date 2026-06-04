package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.Campus;
import com.example.demo.service.CampusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/campus")
public class CampusController {

    @Autowired
    private CampusService campusService;

    @GetMapping("/list")
    public Result getAllCampus() {
        List<Campus> campusList = campusService.getAllCampus();
        return Result.success(campusList);
    }
}