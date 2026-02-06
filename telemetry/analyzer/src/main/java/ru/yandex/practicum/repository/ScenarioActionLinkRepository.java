package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.ScenarioAction;
import ru.yandex.practicum.model.ScenarioActionId;

public interface ScenarioActionLinkRepository extends JpaRepository<ScenarioAction, ScenarioActionId> {
    void deleteBySensorId(String sensorId);

    void deleteByScenarioId(Long id);

    @Modifying
    @Query(value = """
            INSERT INTO scenario_actions (scenario_id, sensor_id, action_id)
            VALUES (:scenarioId, :sensorId, :actionId)
            """, nativeQuery = true)
    void saveLink(@Param("scenarioId") Long scenarioId,
                  @Param("sensorId") String sensorId,
                  @Param("actionId") Long actionId);
}
