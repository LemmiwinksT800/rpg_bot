package org.logic;

import org.model.Player;

public interface PlayerRepository {
    void savePlayer(String playerId, Player player, String currentScenarioId);
    Player loadPlayer(String playerId);
}