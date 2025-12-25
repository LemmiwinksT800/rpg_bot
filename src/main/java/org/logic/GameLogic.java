package org.logic;
import org.model.*;

import java.util.*;

public class GameLogic {
    private final PlayerRepository playerRepository;
    private final ScenarioRepository scenarioRepository;
    private final DatabaseManager dbManager;
    public String currentScenarioId;

    public GameLogic(PlayerRepository playerRepository, ScenarioRepository scenarioRepository) {
        this.playerRepository = playerRepository;
        this.scenarioRepository = scenarioRepository;
        this.dbManager = (DatabaseManager) playerRepository;
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


        Integer partyId = player.getPartyId();
        if (partyId != null) {
            return processPartyInput(playerId, input, partyId);
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

    private GameResponse processPartyInput(String playerId, String input, int partyId) {
        Party party = dbManager.getParty(partyId);
        if (party == null || !party.getStatus().equals("active")) {
            return new GameResponse(GameResponse.ResponseType.ERROR, "party_not_active", false);
        }

        if (!party.getCurrentTurnPlayerId().equals(playerId)) {
            return new GameResponse(GameResponse.ResponseType.ERROR, "not_your_turn", false);
        }

        GameResponse.ResponseType commandType = getCommandType(input);
        if (commandType != null) {
            return new GameResponse(commandType, true, getPartyStatus(party));
        }

        Scenario current = scenarioRepository.getScenario(party.getCurrentScenarioId());
        if (current == null || current.getChoices() == null) {
            return new GameResponse(GameResponse.ResponseType.END, true);
        }

        try {
            int choiceIdx = Integer.parseInt(input) - 1;
            if (choiceIdx < 0 || choiceIdx >= current.getChoices().size()) {
                return buildResponse(current, null, null, GameResponse.ResponseType.ERROR, "invalid_choice");
            }

            Choice choice = current.getChoices().get(choiceIdx);

            for (String pid : party.getPlayerIds()) {
                Player p = loadPlayer(pid);
                applyEffect(p, choice.getEffect());
                if (!p.isAlive()) {

                }
                playerRepository.savePlayer(pid, p, choice.getNextScenarioId());
            }


            boolean allDead = party.getPlayerIds().stream().allMatch(pid -> !loadPlayer(pid).isAlive());
            if (allDead) {
                party.setStatus("ended");
                dbManager.updateParty(party);
                return new GameResponse(GameResponse.ResponseType.DEAD, false);
            }

            // Переходим к следующему игроку
            int currentIndex = party.getPlayerIds().indexOf(playerId);
            String nextTurnId = party.getPlayerIds().get((currentIndex + 1) % party.getPlayerIds().size());
            party.setCurrentTurnPlayerId(nextTurnId);
            party.setCurrentScenarioId(choice.getNextScenarioId());
            dbManager.updateParty(party);

            Scenario next = scenarioRepository.getScenario(party.getCurrentScenarioId());
            return buildResponse(next, null, null, GameResponse.ResponseType.NORMAL);

        } catch (NumberFormatException e) {
            return buildResponse(scenarioRepository.getScenario(party.getCurrentScenarioId()), null, null, GameResponse.ResponseType.ERROR, "invalid_input");
        }
    }

    public String getPartyStatus(Party party) {
        StringBuilder status = new StringBuilder("Статус партии:\n");
        for (String pid : party.getPlayerIds()) {
            Player p = loadPlayer(pid);
            status.append(p.getStatus()).append("\n");
        }
        return status.toString();
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
        return dbManager.getAllCampaigns();
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

    // Мультиплеерные методы
    public int createParty(String playerId, String partyName, String campaignId) {
        Campaign campaign = dbManager.getCampaignById(campaignId);
        if (campaign == null) return -1;
        int partyId = dbManager.createParty(partyName, playerId, campaign.getStartScenarioId());
        if (partyId != -1) {
            Player player = loadPlayer(playerId);
            player.setFaction(campaign.getFaction());
            player.setStats(dbManager.stringToMap(campaign.getStartStats()));
            player.setCurrentScenarioId(campaign.getStartScenarioId());
            player.setPartyId(partyId);
            savePlayer(playerId, player, campaign.getStartScenarioId());
        }
        return partyId;
    }

    private void savePlayer(String playerId, Player player, String currentScenarioId) {
        playerRepository.savePlayer(playerId, player, currentScenarioId);
    }

    public void inviteToParty(int partyId, String invitedPlayerId) {
        dbManager.invitePlayer(partyId, invitedPlayerId);
    }

    public boolean acceptInvitation(String playerId, int partyId) {
        dbManager.updateInvitationStatus(partyId, playerId, "accepted");
        Party party = dbManager.getParty(partyId);
        if (party != null) {
            party.addPlayer(playerId);
            dbManager.updateParty(party);
            Player player = loadPlayer(playerId);
            player.setPartyId(partyId);
            savePlayer(playerId, player, party.getCurrentScenarioId());
            return true;
        }
        return false;
    }

    public void declineInvitation(String playerId, int partyId) {
        dbManager.updateInvitationStatus(partyId, playerId, "declined");
    }

    public GameResponse startPartyGame(int partyId) {
        Party party = dbManager.getParty(partyId);
        if (party == null) return new GameResponse(GameResponse.ResponseType.ERROR, "party_not_found", false);
        Scenario start = scenarioRepository.getScenario(party.getCurrentScenarioId());
        return buildResponse(start, null, null, GameResponse.ResponseType.NORMAL);
    }
}