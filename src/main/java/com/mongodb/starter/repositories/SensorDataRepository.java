package com.mongodb.starter.repositories;

import com.mongodb.starter.models.SensorData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for the SensorData entity.
 */
@Repository
public interface SensorDataRepository extends MongoRepository<SensorData, String> {
    List<SensorData> findAll(Sort sort);


    @Query(value = "{}", sort = "{ 'time': -1 }")
    List<SensorData> findLatest();

    List<SensorData> findTop11ByOrderByTimeDesc();

    List<SensorData> findByTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    List<SensorData> findByTemperature(int temperature);

    List<SensorData> findByTemperatureBetween(int startTemp, int endTemp);

    List<SensorData> findByTemperatureGreaterThanEqual(int temperature);

    List<SensorData> findByTemperatureLessThanEqual(int temperature);

    List<SensorData> findByTemperatureGreaterThan(int temperature);

    List<SensorData> findByTemperatureLessThan(int temperature);

    List<SensorData> findByHumidity(int humidity);
    List<SensorData> findByHumidityBetween(int startHumidity, int endHumidity);
    List<SensorData> findByHumidityGreaterThanEqual(int humidity);
    List<SensorData> findByHumidityLessThanEqual(int humidity);
    List<SensorData> findByHumidityGreaterThan(int humidity);
    List<SensorData> findByHumidityLessThan(int humidity);

    List<SensorData> findByLight(int light);
    List<SensorData> findByLightBetween(int startLight, int endLight);
    List<SensorData> findByLightGreaterThanEqual(int light);
    List<SensorData> findByLightLessThanEqual(int light);
    List<SensorData> findByLightGreaterThan(int light);
    List<SensorData> findByLightLessThan(int light);


    @Query("{ 'wind': { $lt: ?0 }, 'timeStr': { $regex: ?1 } }")
    long countWindLessThanAndDateContains(int windSpeed, String date);



}
