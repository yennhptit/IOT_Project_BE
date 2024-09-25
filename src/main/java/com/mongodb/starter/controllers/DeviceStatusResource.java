package com.mongodb.starter.controllers;
import com.mongodb.starter.controllers.errors.BadRequestAlertException;
import com.mongodb.starter.models.DeviceStatus;
import com.mongodb.starter.repositories.DeviceStatusRepository;
import com.mongodb.starter.services.DeviceStatusService;
import com.mongodb.starter.services.MqttDeviceStatusService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/device-status")
public class DeviceStatusResource {
    @Autowired
    private MqttDeviceStatusService mqttDeviceStatusService;

    private final Logger log = LoggerFactory.getLogger(DeviceStatusResource.class);

    private static final String ENTITY_NAME = "deviceStatus";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DeviceStatusRepository deviceStatusRepository;

    private final DeviceStatusService deviceStatusService;

    public DeviceStatusResource(DeviceStatusRepository deviceStatusRepository, DeviceStatusService deviceStatusService) {
        this.deviceStatusRepository = deviceStatusRepository;
        this.deviceStatusService = deviceStatusService;
    }

    /**
     * {@code POST  /device-status} : Create a new device status.
     *
     * @param deviceStatus the device status to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new device status, or with status {@code 400 (Bad Request)} if the device status has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<DeviceStatus> createDeviceStatus(@Valid @RequestBody DeviceStatus deviceStatus) throws URISyntaxException {
        log.debug("REST request to save DeviceStatus : {}", deviceStatus);
        if (deviceStatus.getId() != null) {
            throw new BadRequestAlertException("A new deviceStatus cannot already have an ID", ENTITY_NAME, "idexists");
        }
        DeviceStatus result = deviceStatusRepository.save(deviceStatus);
        return ResponseEntity.created(new URI("/api/device-status/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId()))
                .body(result);
    }

    /**
     * {@code PUT  /device-status/:id} : Updates an existing device status.
     *
     * @param id the id of the device status to save.
     * @param deviceStatus the device status to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated device status,
     * or with status {@code 400 (Bad Request)} if the device status is not valid,
     * or with status {@code 500 (Internal Server Error)} if the device status couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DeviceStatus> updateDeviceStatus(
            @PathVariable(value = "id", required = false) final String id,
            @Valid @RequestBody DeviceStatus deviceStatus
    ) throws URISyntaxException {
        log.debug("REST request to update DeviceStatus : {}, {}", id, deviceStatus);
        if (deviceStatus.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, deviceStatus.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!deviceStatusRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        DeviceStatus result = deviceStatusRepository.save(deviceStatus);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, deviceStatus.getId()))
                .body(result);
    }

    /**
     * {@code GET  /device-status} : get all the device statuses.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of device statuses in body.
     */
    @GetMapping("")
    public List<DeviceStatus> getAllDeviceStatuses() {
        log.debug("REST request to get all DeviceStatuses");
        return deviceStatusRepository.findAll();
    }

    /**
     * {@code GET  /device-status/:id} : get the "id" device status.
     *
     * @param id the id of the device status to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the device status, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DeviceStatus> getDeviceStatus(@PathVariable String id) {
        log.debug("REST request to get DeviceStatus : {}", id);
        Optional<DeviceStatus> deviceStatus = deviceStatusRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(deviceStatus);
    }

    /**
     * {@code DELETE  /device-status/:id} : delete the "id" device status.
     *
     * @param id the id of the device status to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeviceStatus(@PathVariable String id) {
        log.debug("REST request to delete DeviceStatus : {}", id);
        deviceStatusRepository.deleteById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id)).build();
    }
//    @GetMapping("/{device}")
//    public ResponseEntity<String> getDeviceStatusForWeb(@PathVariable String device) {
//        String status = mqttDeviceStatusService.getLatestDeviceStatus();
//
//        // Nếu trạng thái là on hoặc off thì phản hồi lại
//        if ("on".equalsIgnoreCase(status) || "off".equalsIgnoreCase(status)) {
//            return ResponseEntity.ok(device + " is " + status);
//        }
//
//        // Nếu chưa nhận được trạng thái hợp lệ
//        return ResponseEntity.status(202).body("Waiting for valid status for " + device);
//    }
    @GetMapping("/latest/{device}")
    public ResponseEntity<DeviceStatus> getLatestDeviceStatus(@PathVariable String device) {
        Optional<DeviceStatus> deviceStatus = deviceStatusService.getLatestDeviceStatus(device);
        return deviceStatus.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
