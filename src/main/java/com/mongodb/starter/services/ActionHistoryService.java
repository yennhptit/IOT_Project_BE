package com.mongodb.starter.services;

import com.mongodb.starter.models.ActionHistory;
import com.mongodb.starter.repositories.ActionHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ActionHistoryService {

    @Autowired
    private ActionHistoryRepository actionHistoryRepository;

    public Map<String, String> getLatestActions() {
        Map<String, String> latestActions = new HashMap<>();

        // Lấy hành động mới nhất cho từng thiết bị
        latestActions.put("Fan", getLatestAction("Fan"));
        latestActions.put("Light", getLatestAction("Light"));
        latestActions.put("Air Conditioner", getLatestAction("Air Conditioner"));

        return latestActions;
    }

    private String getLatestAction(String device) {
        return actionHistoryRepository.findLatestActionByDevice(device)
                .stream()
                .findFirst()
                .map(ActionHistory::getAction)
                .orElse("No actions available");
    }
}
