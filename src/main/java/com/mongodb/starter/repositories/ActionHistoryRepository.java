package com.mongodb.starter.repositories;

import com.mongodb.starter.models.ActionHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data MongoDB repository for the ActionHistory entity.
 */
@Repository
public interface ActionHistoryRepository extends MongoRepository<ActionHistory, String> {
    @Query(value = "{ 'device' : ?0 }", sort = "{ 'time' : -1 }")
    List<ActionHistory> findLatestActionByDevice(String device);

    List<ActionHistory> findByTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
}
