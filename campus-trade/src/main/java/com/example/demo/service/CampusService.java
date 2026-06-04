package com.example.demo.service;

import com.example.demo.entity.Campus;

import java.util.List;

public interface CampusService {
    List<Campus> getAllCampus();
    Campus getCampusById(Integer id);
}