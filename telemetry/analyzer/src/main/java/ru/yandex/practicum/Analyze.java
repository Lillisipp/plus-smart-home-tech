package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.yandex.practicum.processor.HubEventProcessor;
import ru.yandex.practicum.processor.SnapshotProcessor;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Analyze {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Analyze.class, args);

        final HubEventProcessor processorHub = context.getBean(HubEventProcessor.class);
        SnapshotProcessor processorSnapshot = context.getBean(SnapshotProcessor.class);

        Thread hubThread = new Thread(processorHub);
        hubThread.setName("HubEventHandlerThread");
        processorSnapshot.run();

    }
}