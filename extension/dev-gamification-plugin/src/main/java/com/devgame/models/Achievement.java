package com.devgame.models;

import java.time.LocalDateTime;

public class Achievement {
    private String id;
    private String name;
    private String description;
    private int xpReward;
    private LocalDateTime unlockedAt;
    
    public Achievement(String id, String name, String description, int xpReward) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.xpReward = xpReward;
        this.unlockedAt = LocalDateTime.now();
    }
    
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getXpReward() { return xpReward; }
    public LocalDateTime getUnlockedAt() { return unlockedAt; }
}
