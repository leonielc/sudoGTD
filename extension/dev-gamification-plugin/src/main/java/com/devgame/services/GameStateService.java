package com.devgame.services;

import com.devgame.models.PlayerStats;
import com.google.gson.Gson;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "DevGameState",
    storages = @Storage("devgame.xml")
)
public class GameStateService implements PersistentStateComponent<GameStateService.State> {

    private PlayerStats playerStats;
    private final Gson gson = new Gson();

    public static class State {
        public String playerStatsJson = "";
    }

    public static GameStateService getInstance() {
        return ApplicationManager.getApplication().getService(GameStateService.class);
    }

    public synchronized PlayerStats getPlayerStats() {
        if (playerStats == null) {
            playerStats = new PlayerStats();
        }
        return playerStats;
    }

    // --- FIX: Added this method back so your build works ---
    public void savePlayerStats(PlayerStats stats) {
        // We just update our local reference. 
        // IntelliJ handles the disk saving automatically when you close/minimize.
        this.playerStats = stats;
    }

    @Nullable
    @Override
    public State getState() {
        State state = new State();
        if (playerStats != null) {
            state.playerStatsJson = gson.toJson(playerStats);
        }
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        if (state.playerStatsJson != null && !state.playerStatsJson.isEmpty()) {
            try {
                this.playerStats = gson.fromJson(state.playerStatsJson, PlayerStats.class);
            } catch (Exception e) {
                this.playerStats = new PlayerStats();
            }
        } else {
            this.playerStats = new PlayerStats();
        }
    }
}