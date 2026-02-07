package ru.yandex.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.yandex.practicum.processor.HubEventProcessor;
import ru.yandex.practicum.processor.SnapshotProcessor;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan
public class Analyze {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Analyze.class, args);

        final HubEventProcessor processorHub = context.getBean(HubEventProcessor.class);
        final SnapshotProcessor processorSnapshot = context.getBean(SnapshotProcessor.class);

        Thread hubThread = new Thread(processorHub);
        hubThread.setName("HubEventHandlerThread");
        Thread snapshotThread = new Thread(processorSnapshot);
        snapshotThread.setName("SnapshotProcessorThread");

        snapshotThread.start();
        hubThread.start();
        log.info("Analyze: threads started: hubThread={}, snapshotThread={}",
                hubThread.getName(), snapshotThread.getName());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            context.close();
        }, "ShutdownHook"));
    }
}