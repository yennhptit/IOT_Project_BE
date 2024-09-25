package com.mongodb.starter.controllers;

import com.mongodb.starter.controllers.errors.BadRequestAlertException;
import com.mongodb.starter.models.SensorData;
import com.mongodb.starter.repositories.SensorDataRepository;
import com.mongodb.starter.services.SensorDataService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
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
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * REST controller for managing {@link com.mongodb.starter.models.SensorData}.
 */
@RestController

@RequestMapping("/api/sensor-data")
public class SensorDataResource {

    private static final Logger LOG = LoggerFactory.getLogger(SensorDataResource.class);

    private static final String ENTITY_NAME = "sensorData";

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");


    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SensorDataRepository sensorDataRepository;

    private final SensorDataService sensorDataService;

    public SensorDataResource(SensorDataRepository sensorDataRepository, SensorDataService sensorDataService) {
        this.sensorDataRepository = sensorDataRepository;
        this.sensorDataService = sensorDataService;
    }
    private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy - HH:mm:ss dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy - dd/MM/yyyy")
    );


    /**
     * {@code POST  /sensor-data} : Create a new sensorData.
     *
     * @param sensorData the sensorData to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new sensorData, or with status {@code 400 (Bad Request)} if the sensorData has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<SensorData> createSensorData(@Valid @RequestBody SensorData sensorData) throws URISyntaxException {
        LOG.debug("REST request to save SensorData : {}", sensorData);
        if (sensorData.getId() != null) {
            throw new BadRequestAlertException("A new sensorData cannot already have an ID", ENTITY_NAME, "idexists");
        }
        sensorData = sensorDataRepository.save(sensorData);
        return ResponseEntity.created(new URI("/api/sensor-data/" + sensorData.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, sensorData.getId()))
            .body(sensorData);
    }

    /**
     * {@code PUT  /sensor-data/:id} : Updates an existing sensorData.
     *
     * @param id the id of the sensorData to save.
     * @param sensorData the sensorData to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated sensorData,
     * or with status {@code 400 (Bad Request)} if the sensorData is not valid,
     * or with status {@code 500 (Internal Server Error)} if the sensorData couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SensorData> updateSensorData(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody SensorData sensorData
    ) throws URISyntaxException {
        LOG.debug("REST request to update SensorData : {}, {}", id, sensorData);
        if (sensorData.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, sensorData.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!sensorDataRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        sensorData = sensorDataRepository.save(sensorData);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, sensorData.getId()))
            .body(sensorData);
    }

    /**
     * {@code PATCH  /sensor-data/:id} : Partial updates given fields of an existing sensorData, field will ignore if it is null
     *
     * @param id the id of the sensorData to save.
     * @param sensorData the sensorData to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated sensorData,
     * or with status {@code 400 (Bad Request)} if the sensorData is not valid,
     * or with status {@code 404 (Not Found)} if the sensorData is not found,
     * or with status {@code 500 (Internal Server Error)} if the sensorData couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<SensorData> partialUpdateSensorData(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody SensorData sensorData
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update SensorData partially : {}, {}", id, sensorData);
        if (sensorData.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, sensorData.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!sensorDataRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SensorData> result = sensorDataRepository
            .findById(sensorData.getId())
            .map(existingSensorData -> {
                if (sensorData.getTemperature() != null) {
                    existingSensorData.setTemperature(sensorData.getTemperature());
                }
                if (sensorData.getHumidity() != null) {
                    existingSensorData.setHumidity(sensorData.getHumidity());
                }
                if (sensorData.getLight() != null) {
                    existingSensorData.setLight(sensorData.getLight());
                }
                if (sensorData.getTime() != null) {
                    existingSensorData.setTime(sensorData.getTime());
                }
                if (sensorData.getTimeStr() != null) {
                    existingSensorData.setTimeStr(sensorData.getTimeStr());
                }

                return existingSensorData;
            })
            .map(sensorDataRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, sensorData.getId())
        );
    }

    /**
     * {@code GET  /sensor-data} : get all the sensorData.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of sensorData in body.
     */
    @GetMapping("/sortByTime")
    public List<SensorData> getAllSensorDataSortedByTime() {
        LOG.debug("REST request to get all SensorData sorted by time descending");
        return sensorDataRepository.findAll(Sort.by(Sort.Direction.DESC, "time"));
    }
    @GetMapping("")
    public List<SensorData> getAllSensorData() {
        LOG.debug("REST request to get all SensorData");
        return sensorDataRepository.findAll();
    }
    @GetMapping("/latest")
    public ResponseEntity<SensorData> getLatestSensorData() {
        LOG.debug("REST request to get latest SensorData");
        List<SensorData> sensorDataList = sensorDataRepository.findLatest();
        if (sensorDataList.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        SensorData latestSensorData = sensorDataList.get(0); // Lấy bản ghi mới nhất
        return ResponseEntity.ok(latestSensorData);
    }



    /**
     * {@code GET  /sensor-data/:id} : get the "id" sensorData.
     *
     * @param id the id of the sensorData to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the sensorData, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SensorData> getSensorData(@PathVariable("id") String id) {
        LOG.debug("REST request to get SensorData : {}", id);
        Optional<SensorData> sensorData = sensorDataRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(sensorData);
    }
    @GetMapping("/top11latest")
    public List<SensorData> getTop11LatestSensorData() {
        return sensorDataService.getLatestSensorData();
    }

    /**
     * {@code DELETE  /sensor-data/:id} : delete the "id" sensorData.
     *
     * @param id the id of the sensorData to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSensorData(@PathVariable("id") String id) {
        LOG.debug("REST request to delete SensorData : {}", id);
        sensorDataRepository.deleteById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id)).build();
    }


    @GetMapping("/filterTime")
    public List<SensorData> filterByTime(@RequestParam String timeRange) {
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
            return sensorDataRepository.findByTimeBetween(startDateTime, endDateTime);
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

    @GetMapping("/filterTemp")
    public ResponseEntity<List<SensorData>> filterByTemperature(
            @RequestParam(value = "operator") String operator) {

        List<SensorData> result = new ArrayList<>();

        // Kiểm tra chuỗi operator và xử lý các phép so sánh
        if (operator.matches("(>=|<=|>|<)\\s*\\d+(\\.\\d+)?")) {
            // Tìm vị trí ký tự đầu tiên là chữ số
            int index = findFirstDigitIndex(operator);
            String op = operator.substring(0, index); // Phép so sánh (e.g., >=, <=, >, <)
            int value = Integer.parseInt(operator.substring(index).trim()); // Giá trị ánh sáng

            // Lựa chọn phương thức tìm kiếm dựa trên phép so sánh
            switch (op) {
                case ">":
                    result = sensorDataRepository.findByTemperatureGreaterThan(value);
                    break;
                case ">=":
                    result = sensorDataRepository.findByTemperatureGreaterThanEqual(value);
                    break;
                case "<":
                    result = sensorDataRepository.findByTemperatureLessThan(value);
                    break;
                case "<=":
                    result = sensorDataRepository.findByTemperatureLessThanEqual(value);
                    break;
                default:
                    return ResponseEntity.badRequest().body(result);
            }
        } else if (operator.matches("\\d+(\\.\\d+)?")) {
            // Nếu chỉ là một giá trị cụ thể
            int exactTemp = Integer.parseInt(operator);
            result = sensorDataRepository.findByTemperature(exactTemp);
        } else {
            // Trường hợp không hợp lệ
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }

    // Phương thức tìm vị trí ký tự đầu tiên là chữ số
    private int findFirstDigitIndex(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                return i;
            }
        }
        return -1; // Nếu không tìm thấy chữ số nào
    }

    @GetMapping("/filterHumidity")
    public ResponseEntity<List<SensorData>> filterByHumidity(
            @RequestParam(value = "operator") String operator) {

        List<SensorData> result = new ArrayList<>();

        // Kiểm tra chuỗi operator và xử lý các phép so sánh
        if (operator.matches("(>=|<=|>|<)\\s*\\d+(\\.\\d+)?")) {
            // Tìm vị trí ký tự đầu tiên là chữ số
            int index = findFirstDigitIndex(operator);
            String op = operator.substring(0, index); // Phép so sánh (e.g., >=, <=, >, <)
            int value = Integer.parseInt(operator.substring(index).trim()); // Giá trị ánh sáng

            // Lựa chọn phương thức tìm kiếm dựa trên phép so sánh
            switch (op) {
                case ">":
                    result = sensorDataRepository.findByHumidityGreaterThan(value);
                    break;
                case ">=":
                    result = sensorDataRepository.findByHumidityGreaterThanEqual(value);
                    break;
                case "<":
                    result = sensorDataRepository.findByHumidityLessThan(value);
                    break;
                case "<=":
                    result = sensorDataRepository.findByHumidityLessThanEqual(value);
                    break;
                default:
                    return ResponseEntity.badRequest().body(result);
            }
        } else if (operator.matches("\\d+(\\.\\d+)?")) {
            // Nếu chỉ là một giá trị cụ thể
            int exactHumidity = Integer.parseInt(operator);
            result = sensorDataRepository.findByHumidity(exactHumidity);
        } else {
            // Trường hợp không hợp lệ
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }
    @GetMapping("/filterLight")
    public ResponseEntity<List<SensorData>> filterByLight(
            @RequestParam(value = "operator") String operator) {

        List<SensorData> result = new ArrayList<>();

        // Kiểm tra chuỗi operator và xử lý các phép so sánh
        if (operator.matches("(>=|<=|>|<)\\s*\\d+(\\.\\d+)?")) {
            // Tìm vị trí ký tự đầu tiên là chữ số
            int index = findFirstDigitIndex(operator);
            String op = operator.substring(0, index); // Phép so sánh (e.g., >=, <=, >, <)
            int value = Integer.parseInt(operator.substring(index).trim()); // Giá trị ánh sáng

            // Lựa chọn phương thức tìm kiếm dựa trên phép so sánh
            switch (op) {
                case ">":
                    result = sensorDataRepository.findByLightGreaterThan(value);
                    break;
                case ">=":
                    result = sensorDataRepository.findByLightGreaterThanEqual(value);
                    break;
                case "<":
                    result = sensorDataRepository.findByLightLessThan(value);
                    break;
                case "<=":
                    result = sensorDataRepository.findByLightLessThanEqual(value);
                    break;
                default:
                    return ResponseEntity.badRequest().body(result);
            }
        } else if (operator.matches("\\d+(\\.\\d+)?")) {
            // Nếu chỉ là một giá trị cụ thể
            int exactLight = Integer.parseInt(operator);
            result = sensorDataRepository.findByLight(exactLight);
        } else {
            // Trường hợp không hợp lệ
            return ResponseEntity.badRequest().body(result);
        }

        return ResponseEntity.ok(result);
    }
}
