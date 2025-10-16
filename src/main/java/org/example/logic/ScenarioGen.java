package org.example.logic;

import org.example.model.Scenario;
import org.example.model.Choice;
import java.util.*;

public class ScenarioGen {
    private final Map<String, Scenario> scenarios;

    public ScenarioGen() {
        this.scenarios = new HashMap<>();
        initializeScenarios();
    }

    private void initializeScenarios() {
        // Начальная сцена
        List<Choice> startChoices = Arrays.asList(
                new Choice("Исследовать лес", "forest", null),
                new Choice("Пойти в город", "city", null),
                new Choice("Проверить снаряжение", "inventory", null)
        );
        scenarios.put("start", new Scenario("start",
                "Вы стоите на перепутье. Перед вами три пути...", startChoices));


        List<Choice> forestChoices = Arrays.asList(
                new Choice("Вариант 1", "v1", "hp-15"),
                new Choice("Вариант 2", "v2", null),
                new Choice("Вариант 3", "v3", "stealth-check")
        );
        scenarios.put("forest", new Scenario("forest",
                "forest работает успешно!", forestChoices));
    }

    public Scenario getScenario(String scenarioId) {
        return scenarios.get(scenarioId);
    }

    public boolean scenarioExists(String scenarioId) {
        return scenarios.containsKey(scenarioId);
    }
}