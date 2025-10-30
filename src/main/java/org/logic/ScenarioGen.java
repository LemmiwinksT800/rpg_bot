package org.logic;

import org.model.Scenario;
import org.model.Choice;
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
                new Choice("Проверить снаряжение", "end", null)
        );
        scenarios.put("start", new Scenario("start",
                "Вы стоите на перепутье. Перед вами три пути...", startChoices));


        List<Choice> forestChoices = Arrays.asList(
                new Choice("Атаковать волка", "end", "hp-15"),
                new Choice("Убежать", "start", "check:stealth:12"),
                new Choice("Использовать зелье", "forest_healed", "add:health_potion")
        );

        scenarios.put("forest", new Scenario("forest",
                "Вы встретили голодного волка! Он рычит и готовится к прыжку...", forestChoices));

        scenarios.put("forest_healed", new Scenario("forest_healed",
                "Вы выпили зелье и чувствуете прилив сил! Волк убегает.",
                List.of(new Choice("Вернуться на развилку", "start", null))));

    }

    public Scenario getScenario(String scenarioId) {
        return scenarios.get(scenarioId);
    }

    public boolean scenarioExists(String scenarioId) {
        return scenarios.containsKey(scenarioId);
    }
}