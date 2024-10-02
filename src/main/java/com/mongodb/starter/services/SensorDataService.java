package com.mongodb.starter.services;

import com.mongodb.starter.models.SensorData;
import com.mongodb.starter.repositories.SensorDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.bson.Document; // For Document class if needed
import com.mongodb.client.model.Filters;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class SensorDataService {

    @Autowired
    private SensorDataRepository sensorDataRepository;

    private final MongoTemplate mongoTemplate;

    @Autowired
    public SensorDataService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<SensorData> getLatestSensorData() {
        return sensorDataRepository.findTop11ByOrderByTimeDesc();
    }

    public long countWindLessThan40Today() {
        LocalDate today = LocalDate.now();
        Date startDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        return mongoTemplate.getCollection("sensor_data") // Replace "sensorData" with your actual collection name
                .countDocuments(Filters.and(
                        Filters.lt("wind", 40),
                        Filters.gte("time", startDate),
                        Filters.lt("time", endDate)
                ));
    }

    public long countWindLessThan40ByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<SensorData> sensorDataList = sensorDataRepository.findByTimeAndWindLessThan(startOfDay, endOfDay);
        return sensorDataList.size();
    }
}
