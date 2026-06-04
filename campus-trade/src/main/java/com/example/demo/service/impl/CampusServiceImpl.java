package com.example.demo.service.impl;

import com.example.demo.entity.Campus;
import com.example.demo.mapper.CampusMapper;
import com.example.demo.service.CampusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CampusServiceImpl implements CampusService {

    @Autowired
    private CampusMapper campusMapper;

    @Override
    public List<Campus> getAllCampus() {
        return campusMapper.selectList(null);
    }

    @Override
    public Campus getCampusById(Integer id) {
        return campusMapper.selectById(id);
    }
}