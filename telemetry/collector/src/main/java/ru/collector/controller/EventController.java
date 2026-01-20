package ru.collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.collector.model.HubEvent;
import ru.collector.model.SensorEvent;
import ru.collector.service.CollectorService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final CollectorService collectorService;

    @PostMapping(path = "/sensors", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        log.info("sensor event received: type={}, hubId={}, id={}", event.getEventType(), event.getHubId(), event.getId());
        collectorService.collectSensorEvent(event);
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/hubs", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> collectHubEvent(@Valid @RequestBody HubEvent event) {
        log.info("hub event received: type={}, hubId={}", event.getEventType(), event.getHubId());
        collectorService.collectHubEvent(event);
        return ResponseEntity.ok().build();
    }

}
