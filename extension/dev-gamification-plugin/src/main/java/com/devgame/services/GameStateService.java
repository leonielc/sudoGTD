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
    
    private State state = new State();
    private final Gson gson = new Gson();
    
    public static class State {
        public String playerStatsJson = "";
    }
    
    public static GameStateService getInstance() {
        return ApplicationManager.getApplication().getService(GameStateService.class);
    }
    
    public PlayerStats getPlayerStats() {
        if (state.playerStatsJson == null || state.playerStatsJson.isEmpty()) {
            return new PlayerStats();
        }
        return gson.fromJson(state.playerStatsJson, PlayerStats.class);
    }
    
    public void savePlayerStats(PlayerStats stats) {
        state.playerStatsJson = gson.toJson(stats);
    }
    
    @Nullable
    @Override
    public State getState() {
        return state;
    }
    
    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }
}
