package org.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameLogicTest {
    @Mock private PlayerRepository playerRepo;
    @Mock private ScenarioRepository scenarioRepo;
    @InjectMocks private GameLogic gameLogic;

    private Player player;
    private String playerId = "testPlayer";

    @BeforeEach
    void setUp() {
        player = new Player("TestHero");
        lenient().when(playerRepo.loadPlayer(playerId)).thenReturn(player);  // Add lenient() here
    }

    @Test
    void testAddPlayer() {
        gameLogic.addPlayer(playerId, "NewHero");
        verify(playerRepo).savePlayer(eq(playerId), any(Player.class), eq("start"));
    }

    @Test
    void testStartGame() {
        Scenario mockScenario = new Scenario("start", "Welcome", List.of());
        when(scenarioRepo.getScenario("start")).thenReturn(mockScenario);

        GameResponse response = gameLogic.startGame(playerId);
        assertEquals(GameResponse.ResponseType.NORMAL, response.getType());
        assertEquals("Welcome", response.getMessage());
        assertNotNull(response.getPlayerStatus());
    }

    @Test
    void testProcessCommandHelp() {
        GameResponse response = gameLogic.processInput(playerId, "help");
        assertEquals(GameResponse.ResponseType.HELP, response.getType());
    }

    @Test
    void testProcessChoiceWithEffect() {
        Choice mockChoice = new Choice("Attack", "end", "hp-15");
        Scenario mockScenario = new Scenario("start", "Start", List.of(mockChoice));
        when(scenarioRepo.getScenario("start")).thenReturn(mockScenario);
        when(scenarioRepo.getScenario("end")).thenReturn(new Scenario("end", "End", List.of()));

        gameLogic.setCurrentScenarioId("start");
        GameResponse response = gameLogic.processInput(playerId, "1");

        assertEquals(85, player.getHealth());
        assertEquals("end", gameLogic.currentScenarioId);
        assertEquals(GameResponse.ResponseType.NORMAL, response.getType());
        verify(playerRepo, times(1)).savePlayer(anyString(), any(Player.class), anyString());
    }

    @Test
    void testDeadPlayer() {
        player.damage(100);  // Убить
        GameResponse response = gameLogic.processInput(playerId, "1");
        assertEquals(GameResponse.ResponseType.DEAD, response.getType());
    }

}