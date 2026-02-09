package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.ScenarioCondition;
import ru.yandex.practicum.model.ScenarioConditionId;

public interface ScenarioConditionLinkRepository extends JpaRepository<ScenarioCondition, ScenarioConditionId> {
    void deleteBySensorId(String sensorId);

    void deleteByScenarioId(Long id);

    @Modifying
    @Query(value = """
                        INSERT INTO scenario_conditions (scenario_id, sensor_id, condition_id) 
                        VALUES (:scenarioId, :sensorId, :conditionId)
            
            """, nativeQuery = true
    )
    void saveLink(@Param("scenarioId") Long scenarioId,
                  @Param("sensorId") String sensorId,
                  @Param("conditionId") Long conditionId);

}
