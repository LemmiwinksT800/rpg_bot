package org.logic;

import org.model.Scenario;

import java.util.List;

public interface ScenarioRepository {
    Scenario getScenario(String id);
    List<String> getAllScenarioIds();  // NEW: Для списка сценариев
}