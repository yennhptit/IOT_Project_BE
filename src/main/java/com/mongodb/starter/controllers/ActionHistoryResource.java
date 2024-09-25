package com.mongodb.starter.controllers;

import com.mongodb.starter.controllers.errors.BadRequestAlertException;
import com.mongodb.starter.models.ActionHistory;
import com.mongodb.starter.models.SensorData;
import com.mongodb.starter.repositories.ActionHistoryRepository;
import com.mongodb.starter.services.ActionHistoryService;
import com.mongodb.starter.services.MqttService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST controller for managing {@link com.mongodb.starter.models.ActionHistory}.
 */
@RestController
@RequestMapping("/api/action-histories")
public class ActionHistoryResource {

    private static final Logger LOG = LoggerFactory.getLogger(ActionHistoryResource.class);

    private static final String ENTITY_NAME = "actionHistory";

    @Autowired
    private MqttService mqttService;
    // Map để lưu trạng thái thiết bị
    private final Map<String, String> deviceStatusMap = new ConcurrentHashMap<>();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    @Autowired
    private ActionHistoryService actionHistoryService;


    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ActionHistoryRepository actionHistoryRepository;

    public ActionHistoryResource(ActionHistoryRepository actionHistoryRepository) {
        this.actionHistoryRepository = actionHistoryRepository;
    }

    /**
     * {@code POST  /action-histories} : Create a new actionHistory.
     *
     * @param actionHistory the actionHistory to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new actionHistory, or with status {@code 400 (Bad Request)} if the actionHistory has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ActionHistory> createActionHistory(@Valid @RequestBody ActionHistory actionHistory) throws URISyntaxException {
        LOG.debug("REST request to save ActionHistory : {}", actionHistory);
        if (actionHistory.getId() != null) {
            throw new BadRequestAlertException("A new actionHistory cannot already have an ID", ENTITY_NAME, "idexists");
        }
        actionHistory = actionHistoryRepository.save(actionHistory);
        return ResponseEntity.created(new URI("/api/action-histories/" + actionHistory.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, actionHistory.getId()))
            .body(actionHistory);
    }

    /**
     * {@code PUT  /action-histories/:id} : Updates an existing actionHistory.
     *
     * @param id the id of the actionHistory to save.
     * @param actionHistory the actionHistory to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated actionHistory,
     * or with status {@code 400 (Bad Request)} if the actionHistory is not valid,
     * or with status {@code 500 (Internal Server Error)} if the actionHistory couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ActionHistory> updateActionHistory(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody ActionHistory actionHistory
    ) throws URISyntaxException {
        LOG.debug("REST request to update ActionHistory : {}, {}", id, actionHistory);
        if (actionHistory.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, actionHistory.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!actionHistoryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        actionHistory = actionHistoryRepository.save(actionHistory);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, actionHistory.getId()))
            .body(actionHistory);
    }

    /**
     * {@code PATCH  /action-histories/:id} : Partial updates given fields of an existing actionHistory, field will ignore if it is null
     *
     * @param id the id of the actionHistory to save.
     * @param actionHistory the actionHistory to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated actionHistory,
     * or with status {@code 400 (Bad Request)} if the actionHistory is not valid,
     * or with status {@code 404 (Not Found)} if the actionHistory is not found,
     * or with status {@code 500 (Internal Server Error)} if the actionHistory couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ActionHistory> partialUpdateActionHistory(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody ActionHistory actionHistory
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update ActionHistory partially : {}, {}", id, actionHistory);
        if (actionHistory.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, actionHistory.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!actionHistoryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ActionHistory> result = actionHistoryRepository
            .findById(actionHistory.getId())
            .map(existingActionHistory -> {
                if (actionHistory.getDevice() != null) {
                    existingActionHistory.setDevice(actionHistory.getDevice());
                }
                if (actionHistory.getAction() != null) {
                    existingActionHistory.setAction(actionHistory.getAction());
                }
                if (actionHistory.getTime() != null) {
                    existingActionHistory.setTime(actionHistory.getTime());
                }

                return existingActionHistory;
            })
            .map(actionHistoryRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, actionHistory.getId())
        );
    }

    /**
     * {@code GET  /action-histories} : get all the actionHistories.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of actionHistories in body.
     */
    @GetMapping("")
    public List<ActionHistory> getAllActionHistories() {
        LOG.debug("REST request to get all ActionHistories");
        return actionHistoryRepository.findAll();
    }

    /**
     * {@code GET  /action-histories/:id} : get the "id" actionHistory.
     *
     * @param id the id of the actionHistory to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the actionHistory, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ActionHistory> getActionHistory(@PathVariable("id") String id) {
        LOG.debug("REST request to get ActionHistory : {}", id);
        Optional<ActionHistory> actionHistory = actionHistoryRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(actionHistory);
    }

    /**
     * {@code DELETE  /action-histories/:id} : delete the "id" actionHistory.
     *
     * @param id the id of the actionHistory to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActionHistory(@PathVariable("id") String id) {
        LOG.debug("REST request to delete ActionHistory : {}", id);
        actionHistoryRepository.deleteById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id)).build();
    }
    @PostMapping("/{device}/ON")
    public ResponseEntity<String> turnOnDevice(@PathVariable String device) {
        mqttService.sendMessage("B21DCCN129/esp8266/" + device, "on");
        saveDeviceAction(device, "ON");
        return ResponseEntity.ok(device + " đã bật");
    }
//    @PostMapping("/{device}/on")
//    public ResponseEntity<String> turnOnDevice(@PathVariable String device) {
//        mqttService.sendMessage("B21DCCN129/esp8266/" + device, "on");
//        saveDeviceAction(device, "ON");
//
//        try {
//            // Wait for the status update
//            String status = mqttService.awaitDeviceStatus(device);
//            return ResponseEntity.ok(device + " đã bật. Trạng thái hiện tại: " + status);
//        } catch (InterruptedException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi khi chờ trạng thái.");
//        }
//    }

//    @PostMapping("/{device}/off")
//    public ResponseEntity<String> turnOffDevice(@PathVariable String device) {
//        mqttService.sendMessage("B21DCCN129/esp8266/" + device, "off");
//        saveDeviceAction(device, "OFF");
//        return ResponseEntity.ok(device + " đã tắt");
//    }
    @PostMapping("/{device}/OFF")
    public ResponseEntity<String> turnOffDevice(@PathVariable String device) {
        String topic = "B21DCCN129/esp8266/" + device + "/status";

        // Gửi lệnh tắt thiết bị qua MQTT
        mqttService.sendMessage("B21DCCN129/esp8266/" + device, "off");

        // Cập nhật trạng thái tạm thời của thiết bị thành "turning-off"
        deviceStatusMap.put(device, "turning-off");

        saveDeviceAction(device, "OFF");


        // Trả về thông báo là lệnh tắt đã được gửi
        return ResponseEntity.ok(device + " đang được tắt...");
    }
    public void onMessageReceived(String topic, String message) {
        String[] topicParts = topic.split("/");
        String device = topicParts[2]; // Lấy tên thiết bị từ topic

        // Nếu nhận được trạng thái thiết bị, cập nhật vào Map
        if (topic.endsWith("/status")) {
            deviceStatusMap.put(device, message);
        }
    }

    /**
     * API để client polling trạng thái thiết bị
     */
    @GetMapping("/{device}/status")
    public ResponseEntity<String> getDeviceStatus(@PathVariable String device) {
        String status = deviceStatusMap.getOrDefault(device, "unknown");
        return ResponseEntity.ok(status);
    }
//    @PostMapping("/{device}/off")
//    public ResponseEntity<String> turnOffDevice(@PathVariable String device) {
//        mqttService.sendMessage("B21DCCN129/esp8266/" + device, "off");
//        saveDeviceAction(device, "OFF");
//
//        try {
//            // Wait for the status update
//            String status = mqttService.awaitDeviceStatus(device);
//            return ResponseEntity.ok(device + " đã tắt. Trạng thái hiện tại: " + status);
//        } catch (InterruptedException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi khi chờ trạng thái.");
//        }
//    }

    private void saveDeviceAction(String device, String status) {
        // Lưu trạng thái thiết bị vào MongoDB
        if (Objects.equals(device, "fan")) {device = "Fan";}
        else if (Objects.equals(device, "light")) {device = "Light";}
        else if (Objects.equals(device, "airconditioner")) {device = "Air Conditioner";}

        // Lưu lịch sử hành động
        ActionHistory actionHistory = new ActionHistory();
        actionHistory.setDevice(device);
        actionHistory.setAction(status);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        String formattedTime = LocalDateTime.now().format(formatter);
        actionHistory.setTime(LocalDateTime.now());
        actionHistory.setTimeStr(formattedTime);
        actionHistoryRepository.save(actionHistory);
    }
    @GetMapping("/latest-actions")
    public ResponseEntity<Map<String, String>> getLatestActions() {
        Map<String, String> latestActions = actionHistoryService.getLatestActions();
        return ResponseEntity.ok(latestActions);
    }

    @GetMapping("/filterTime")
    public List<ActionHistory> filterByTime(@RequestParam String timeRange) {
        try {
            String[] ranges = timeRange.split(" - ");
            LocalDateTime startDateTime;
            LocalDateTime endDateTime;

            if (ranges.length == 1) {
                // Trường hợp 1: HH:mm:ss dd/MM/yyyy hoặc dd/MM/yyyy
                if (ranges[0].matches("\\d{2}:\\d{2}:\\d{2} \\d{2}/\\d{2}/\\d{4}")) {
                    // HH:mm:ss dd/MM/yyyy
                    startDateTime = parseDateTime(ranges[0]);
                    endDateTime = startDateTime.plusSeconds(1);
                } else {
                    // dd/MM/yyyy
                    LocalDate startDate = parseDate(ranges[0]);
                    startDateTime = startDate.atStartOfDay(); // Chuyển đổi LocalDate sang LocalDateTime
                    endDateTime = startDate.plusDays(1).atStartOfDay().minusSeconds(1); // Cuối ngày
                }
            } else if (ranges.length == 2) {
                // Trường hợp 2: HH:mm:ss dd/MM/yyyy - HH:mm:ss dd/MM/yyyy
                if (ranges[0].matches("\\d{2}:\\d{2}:\\d{2} \\d{2}/\\d{2}/\\d{4}") &&
                        ranges[1].matches("\\d{2}:\\d{2}:\\d{2} \\d{2}/\\d{2}/\\d{4}")) {
                    startDateTime = parseDateTime(ranges[0]);
                    endDateTime = parseDateTime(ranges[1]);
                } else {
                    // Trường hợp 3: dd/MM/yyyy - dd/MM/yyyy
                    LocalDate startDate = parseDate(ranges[0]);
                    LocalDate endDate = parseDate(ranges[1]);
                    startDateTime = startDate.atStartOfDay(); // Bắt đầu ngày
                    endDateTime = endDate.plusDays(1).atStartOfDay().minusSeconds(1); // Cuối ngày
                }
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format.");
            }
            return actionHistoryRepository.findByTimeBetween(startDateTime, endDateTime);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format. Please use the correct format.");
        }
    }
    private LocalDateTime parseDateTime(String dateTimeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        return LocalDateTime.parse(dateTimeStr, formatter);
    }

    private LocalDate parseDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return LocalDate.parse(dateStr, formatter);
    }

}
