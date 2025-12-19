package org.logic;

import org.model.*;

import java.util.*;

public class GameLogic {
    private final PlayerRepository playerRepository;
    private final ScenarioRepository scenarioRepository;
    public String currentScenarioId;

    public GameLogic(PlayerRepository playerRepository, ScenarioRepository scenarioRepository) {
        this.playerRepository = playerRepository;
        this.scenarioRepository = scenarioRepository;
        this.currentScenarioId = "start";
    }

    public void addPlayer(String playerId, String playerName) {
        Player player = new Player(playerName);
        playerRepository.savePlayer(playerId, player, "start");
    }

    public GameResponse startGame(String playerId) {
        Player player = playerRepository.loadPlayer(playerId);
        if (player == null) {
            return new GameResponse(GameResponse.ResponseType.ERROR, "player_not_found", false);
        }
        Scenario start = scenarioRepository.getScenario(currentScenarioId);
        return buildResponse(start, player, null, GameResponse.ResponseType.NORMAL);
    }

    public GameResponse processInput(String playerId, String input) {
        Player player = playerRepository.loadPlayer(playerId);
        if (player == null) {
            return new GameResponse(GameResponse.ResponseType.ERROR, "player_not_found", false);
        }

        if (!player.isAlive()) {
            return new GameResponse(GameResponse.ResponseType.DEAD, false);
        }

        GameResponse.ResponseType commandType = getCommandType(input);
        if (commandType != null) {
            return new GameResponse(commandType, true, player.getStatus());
        }

        Scenario current = scenarioRepository.getScenario(currentScenarioId);
        if (current == null || current.getChoices() == null) {
            return new GameResponse(GameResponse.ResponseType.END, true);
        }

        try {
            int choiceIdx = Integer.parseInt(input) - 1;
            if (choiceIdx < 0 || choiceIdx >= current.getChoices().size()) {
                return buildResponse(current, player, null, GameResponse.ResponseType.ERROR, "invalid_choice");
            }

            Choice choice = current.getChoices().get(choiceIdx);
            applyEffect(player, choice.getEffect());

            if (!player.isAlive()) {
                playerRepository.savePlayer(playerId, player, currentScenarioId);
                return new GameResponse(GameResponse.ResponseType.DEAD, false);
            }

            currentScenarioId = choice.getNextScenarioId();
            playerRepository.savePlayer(playerId, player, currentScenarioId);

            Scenario next = scenarioRepository.getScenario(currentScenarioId);
            return buildResponse(next, player, null, GameResponse.ResponseType.NORMAL);

        } catch (NumberFormatException e) {
            return buildResponse(scenarioRepository.getScenario(currentScenarioId), player, null, GameResponse.ResponseType.ERROR, "invalid_input");
        }
    }

    private GameResponse.ResponseType getCommandType(String input) {
        return switch (input.toLowerCase()) {
            case "help" -> GameResponse.ResponseType.HELP;
            case "status" -> GameResponse.ResponseType.STATUS;
            case "inventory" -> GameResponse.ResponseType.INVENTORY;
            case "exit" -> GameResponse.ResponseType.EXIT;
            default -> null;
        };
    }

    private void applyEffect(Player player, Effect effect) {
        if (effect == null) return;

        switch (effect.getType()) {
            case DAMAGE -> player.damage(effect.getValue());
            case HEAL -> player.heal(effect.getValue());
            case ADD_ITEM -> Item.predefinedItems().stream()
                    .filter(i -> i.getId().equals(effect.getTarget()))
                    .findFirst()
                    .ifPresent(player::addItem);
            case CHECK_STAT -> {
                int required = effect.getValue();
                int playerStat = player.getStats().getOrDefault(effect.getTarget(), 0);
                if (playerStat < required) {
                    player.damage(20); // провал проверки
                }
            }
        }
    }

    private GameResponse buildResponse(Scenario scenario, Player player, String overrideMessage, GameResponse.ResponseType type) {
        return buildResponse(scenario, player, overrideMessage, type, null);
    }

    private GameResponse buildResponse(Scenario scenario, Player player, String overrideMessage, GameResponse.ResponseType type, String errorKey) {
        String message = overrideMessage != null ? overrideMessage : (scenario != null ? scenario.getDescription() : "");
        List<Choice> choices = scenario != null ? scenario.getChoices() : null;
        return new GameResponse(message, choices, true, player != null ? player.getStatus() : null, type, errorKey);
    }

    public Player loadPlayer(String playerId) {
        return playerRepository.loadPlayer(playerId);
    }

    public void setCurrentScenarioId(String id) {
        this.currentScenarioId = id;
    }

    public void saveCurrentState(String playerId) {
        Player player = loadPlayer(playerId);
        if (player != null) {
            playerRepository.savePlayer(playerId, player, currentScenarioId);
        }
    }

    public List<String> getAllScenarioIds() {
        return scenarioRepository.getAllScenarioIds();
    }

    public boolean scenarioExists(String id) {
        return scenarioRepository.getScenario(id) != null;
    }

    public void chooseCampaign(String playerId, String campaignId) {
        DatabaseManager dbManager = (DatabaseManager) playerRepository;
        Campaign campaign = dbManager.getCampaignById(campaignId);
        if (campaign == null) return;

        Player player = loadPlayer(playerId);
        if (player != null) {
            player.setFaction(campaign.getFaction());
            player.setStats(dbManager.stringToMap(campaign.getStartStats()));
            setCurrentScenarioId(campaign.getStartScenarioId());
            saveCurrentState(playerId);
        }
    }

    public List<Campaign> getAllCampaigns() {
        return ((DatabaseManager) playerRepository).getAllCampaigns();
    }

    public void resetForNewCampaign(String playerId) {
        Player player = loadPlayer(playerId);
        if (player != null) {
            player.setHealth(100);
            player.setMaxHealth(100);
            player.setLevel(1);
            player.setFaction(null);
            player.setStats(new HashMap<>() {{
                put("stealth", 10);
                put("strength", 10);
            }});
            player.getInventory().clear();
            setCurrentScenarioId("start");
            saveCurrentState(playerId);
        }
    }
}