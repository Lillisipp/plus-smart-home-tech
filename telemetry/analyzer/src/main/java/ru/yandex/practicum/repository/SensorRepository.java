package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.Sensor;

public interface SensorRepository extends JpaRepository<Sensor, String> {

    boolean existsByIdInAndHubId(java.util.Collection<String> ids, String hubId);

    java.util.Optional<Sensor> findByIdAndHubId(String id, String hubId);
}

