package com.devgame.models;

import java.time.LocalDateTime;
import java.util.*;

public class RewardPack {
    public enum PackType {
        CLASH_ROYALE("Clash Royale Chest", "🏆"),
        POKEMON_POCKET("Pokemon Pack", "⚡"),
        GENERIC("Dev Pack", "🎁");
        
        private final String displayName;
        private final String emoji;
        
        PackType(String displayName, String emoji) {
            this.displayName = displayName;
            this.emoji = emoji;
        }
        
        public String getDisplayName() { return displayName; }
        public String getEmoji() { return emoji; }
    }
    
    private PackType type;
    private int gems;
    private List<String> items;
    private LocalDateTime earnedAt;
    private boolean opened;
    
    public RewardPack(PackType type, int gems) {
        this.type = type;
        this.gems = gems;
        this.items = generateRandomItems();
        this.earnedAt = LocalDateTime.now();
        this.opened = false;
    }
    
    private List<String> generateRandomItems() {
        List<String> items = new ArrayList<>();
        Random random = new Random();
        
        switch (type) {
            case CLASH_ROYALE:
                items.add(gems + " Gems");
                items.add(random.nextInt(1000) + " Gold");
                items.add("Epic Card x" + (random.nextInt(3) + 1));
                break;
            case POKEMON_POCKET:
                items.add(gems + " PokeGold");
                items.add("Rare Card x" + (random.nextInt(3) + 1));
                items.add("Booster Pack");
                break;
            case GENERIC:
                items.add(gems + " Dev Coins");
                items.add("XP Boost x" + (random.nextInt(5) + 1));
                items.add("Theme Unlock");
                break;
        }
        
        return items;
    }
    
    public PackType getType() { return type; }
    public int getGems() { return gems; }
    public List<String> getItems() { return items; }
    public LocalDateTime getEarnedAt() { return earnedAt; }
    public boolean isOpened() { return opened; }
    public void setOpened(boolean opened) { this.opened = opened; }
}
