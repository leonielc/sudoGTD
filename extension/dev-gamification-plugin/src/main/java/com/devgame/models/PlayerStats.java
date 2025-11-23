package com.devgame.models;

import java.time.LocalDate;
import java.util.*;

public class PlayerStats {
    private int level;
    private long xp;
    private long linesWritten;
    private int testsRun;
    private int testsPassed;
    private int testsFailed;
    private int bossesDefeated;
    private List<Achievement> achievements;
    private List<RewardPack> rewardPacks;
    
    private int grassStreak;
    private int maxGrassStreak;
    private String lastGrassTouchDate;
    
    public PlayerStats() {
        this.level = 1;
        this.xp = 0;
        this.linesWritten = 0;
        this.testsRun = 0;
        this.testsPassed = 0;
        this.testsFailed = 0;
        this.bossesDefeated = 0;
        this.achievements = new ArrayList<>();
        this.rewardPacks = new ArrayList<>();
        this.grassStreak = 0;
        this.maxGrassStreak = 0;
        this.lastGrassTouchDate = null;
    }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public long getXp() { return xp; }
    public void setXp(long xp) { this.xp = xp; }
    
    public long getLinesWritten() { return linesWritten; }
    public void setLinesWritten(long lines) { this.linesWritten = lines; }
    
    public int getTestsRun() { return testsRun; }
    public void setTestsRun(int tests) { this.testsRun = tests; }
    
    public int getTestsPassed() { return testsPassed; }
    public void setTestsPassed(int tests) { this.testsPassed = tests; }
    
    public int getTestsFailed() { return testsFailed; }
    public void setTestsFailed(int tests) { this.testsFailed = tests; }
    
    public int getBossesDefeated() { return bossesDefeated; }
    public void setBossesDefeated(int bosses) { this.bossesDefeated = bosses; }
    
    public List<Achievement> getAchievements() { return achievements; }
    public void addAchievement(Achievement achievement) { 
        this.achievements.add(achievement); 
    }
    
    public List<RewardPack> getRewardPacks() { return rewardPacks; }
    public void addRewardPack(RewardPack pack) { 
        this.rewardPacks.add(pack); 
    }
    
    public int getGrassStreak() { return grassStreak; }
    public void setGrassStreak(int streak) { this.grassStreak = streak; }
    
    public int getMaxGrassStreak() { return maxGrassStreak; }
    public void setMaxGrassStreak(int streak) { this.maxGrassStreak = streak; }
    
    public String getLastGrassTouchDate() { return lastGrassTouchDate; }
    public void setLastGrassTouchDate(String date) { this.lastGrassTouchDate = date; }
    
    public long getXpForNextLevel() {
        return level * 1000L;
    }
    
    public double getXpProgress() {
        return (double) xp / getXpForNextLevel();
    }
    
    public boolean canTouchGrassToday() {
        if (lastGrassTouchDate == null) return true;
        return !lastGrassTouchDate.equals(LocalDate.now().toString());
    }
}
