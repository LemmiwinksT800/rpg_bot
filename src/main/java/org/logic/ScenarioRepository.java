package org.logic;

import org.model.Scenario;

public interface ScenarioRepository {
    Scenario getScenario(String id);

}