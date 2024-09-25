package com.mongodb.starter.services;

import com.mongodb.starter.models.SensorData;
import com.mongodb.starter.repositories.SensorDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SensorDataService {

    @Autowired
    private SensorDataRepository sensorDataRepository;


    public List<SensorData> getLatestSensorData() {
        return sensorDataRepository.findTop11ByOrderByTimeDesc();
    }

}