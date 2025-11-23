package com.devgame.services;

import com.devgame.models.Achievement;
import com.devgame.models.PlayerStats;
import com.devgame.models.RewardPack;
import com.devgame.ui.LevelUpNotification;
import com.devgame.ui.RewardPackDialog;
import com.intellij.openapi.project.Project;

public class LevelingService {
    private final Project project;
    private final GameStateService gameState;
    
    public LevelingService(Project project) {
        this.project = project;
        this.gameState = GameStateService.getInstance();
    }
    
    public static LevelingService getInstance(Project project) {
        return project.getService(LevelingService.class);
    }
    
    public void addXp(long xp, String reason) {
        PlayerStats stats = gameState.getPlayerStats();
        long oldXp = stats.getXp();
        stats.setXp(oldXp + xp);
        
        checkLevelUp(stats);
        gameState.savePlayerStats(stats);
    }
    
    public void addLinesWritten(int lines) {
        PlayerStats stats = gameState.getPlayerStats();
        stats.setLinesWritten(stats.getLinesWritten() + lines);
        
        addXp(lines * 10L, "Lines written");
        
        checkAchievements(stats);
    }
    
    public void onTestPassed(int testCount) {
        PlayerStats stats = gameState.getPlayerStats();
        stats.setTestsRun(stats.getTestsRun() + testCount);
        stats.setTestsPassed(stats.getTestsPassed() + testCount);
        stats.setBossesDefeated(stats.getBossesDefeated() + 1);
        
        addXp(testCount * 100L, "Tests passed");
        
        if (stats.getTestsPassed() % 10 == 0) {
            grantRewardPack(stats);
        }
        
        checkAchievements(stats);
        gameState.savePlayerStats(stats);
    }
    
    public void onTestFailed(int testCount) {
        PlayerStats stats = gameState.getPlayerStats();
        stats.setTestsRun(stats.getTestsRun() + testCount);
        stats.setTestsFailed(stats.getTestsFailed() + testCount);
        
        addXp(testCount * 10L, "Tests attempted");
        
        gameState.savePlayerStats(stats);
    }
    
    private void checkLevelUp(PlayerStats stats) {
        while (stats.getXp() >= stats.getXpForNextLevel()) {
            stats.setXp(stats.getXp() - stats.getXpForNextLevel());
            stats.setLevel(stats.getLevel() + 1);
            
            LevelUpNotification.show(project, stats.getLevel());
        }
    }
    
    private void grantRewardPack(PlayerStats stats) {
        RewardPack.PackType type;
        int gems;
        
        int packIndex = (stats.getTestsPassed() / 10) % 3;
        switch (packIndex) {
            case 0:
                type = RewardPack.PackType.CLASH_ROYALE;
                gems = 50;
                break;
            case 1:
                type = RewardPack.PackType.POKEMON_POCKET;
                gems = 100;
                break;
            default:
                type = RewardPack.PackType.GENERIC;
                gems = 75;
                break;
        }
        
        RewardPack pack = new RewardPack(type, gems);
        stats.addRewardPack(pack);
        
        RewardPackDialog.show(project, pack);
    }
    
    private void checkAchievements(PlayerStats stats) {
        if (stats.getLinesWritten() >= 100 && 
            stats.getAchievements().stream().noneMatch(a -> a.getId().equals("first_100"))) {
            Achievement achievement = new Achievement(
                "first_100", 
                "First Steps", 
                "Write 100 lines of code", 
                500
            );
            stats.addAchievement(achievement);
            addXp(achievement.getXpReward(), "Achievement unlocked");
        }
        
        if (stats.getTestsPassed() >= 50 && 
            stats.getAchievements().stream().noneMatch(a -> a.getId().equals("test_master"))) {
            Achievement achievement = new Achievement(
                "test_master", 
                "Test Master", 
                "Pass 50 unit tests", 
                1000
            );
            stats.addAchievement(achievement);
            addXp(achievement.getXpReward(), "Achievement unlocked");
        }
        
        if (stats.getBossesDefeated() >= 10 && 
            stats.getAchievements().stream().noneMatch(a -> a.getId().equals("boss_slayer"))) {
            Achievement achievement = new Achievement(
                "boss_slayer", 
                "Boss Slayer", 
                "Defeat 10 bosses", 
                750
            );
            stats.addAchievement(achievement);
            addXp(achievement.getXpReward(), "Achievement unlocked");
        }
    }
}
