package ru.yandex.practicum.processor;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerShutdown {

    private final HubEventProcessor hubEventProcessor;
    private final SnapshotProcessor snapshotProcessor;

    @PreDestroy
    public void onShutdown(){
        log.info("AnalyzerShutdown: stopping processors");
        hubEventProcessor.stop();
        snapshotProcessor.stop();
        log.info("AnalyzerShutdown: stop signals sent");
    }
}
