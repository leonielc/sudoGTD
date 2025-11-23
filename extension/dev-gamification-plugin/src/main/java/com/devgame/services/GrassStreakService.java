package com.devgame.services;

import com.devgame.models.Achievement;
import com.devgame.models.PlayerStats;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class GrassStreakService {
    private final Project project;
    private final GameStateService gameState;
    
    public GrassStreakService(Project project) {
        this.project = project;
        this.gameState = GameStateService.getInstance();
    }
    
    public static GrassStreakService getInstance(Project project) {
        return new GrassStreakService(project);
    }
    
    public void touchGrass() {
        PlayerStats stats = gameState.getPlayerStats();
        LocalDate today = LocalDate.now();
        String todayStr = today.toString();
        
        if (!stats.canTouchGrassToday()) {
            showNotification("Already Touched Grass!", 
                "You've already touched grass today! Come back tomorrow 🌱", 
                NotificationType.INFORMATION);
            return;
        }
        
        if (stats.getLastGrassTouchDate() != null) {
            LocalDate lastTouch = LocalDate.parse(stats.getLastGrassTouchDate());
            long daysBetween = ChronoUnit.DAYS.between(lastTouch, today);
            
            if (daysBetween == 1) {
                stats.setGrassStreak(stats.getGrassStreak() + 1);
            } else if (daysBetween > 1) {
                showNotification("Streak Lost!", 
                    "Your " + stats.getGrassStreak() + " day streak was broken! Starting fresh.", 
                    NotificationType.WARNING);
                stats.setGrassStreak(1);
            }
        } else {
            stats.setGrassStreak(1);
        }
        
        stats.setLastGrassTouchDate(todayStr);
        
        if (stats.getGrassStreak() > stats.getMaxGrassStreak()) {
            stats.setMaxGrassStreak(stats.getGrassStreak());
        }
        
        int xpReward = 50 * stats.getGrassStreak();
        LevelingService.getInstance(project).addXp(xpReward, "Touched grass!");
        
        checkGrassAchievements(stats);
        
        gameState.savePlayerStats(stats);
        
        showNotification("Grass Touched!", 
            "Streak: " + stats.getGrassStreak() + " days | +" + xpReward + " XP", 
            NotificationType.INFORMATION);
    }
    
    private void checkGrassAchievements(PlayerStats stats) {
        LevelingService levelingService = LevelingService.getInstance(project);
        
        if (stats.getGrassStreak() >= 3 && 
            stats.getAchievements().stream().noneMatch(a -> a.getId().equals("grass_toucher"))) {
            Achievement achievement = new Achievement(
                "grass_toucher", 
                "Grass Toucher", 
                "Touch grass 3 days in a row", 
                200
            );
            stats.addAchievement(achievement);
            levelingService.addXp(achievement.getXpReward(), "Achievement unlocked");
        }
        
        if (stats.getGrassStreak() >= 7 && 
            stats.getAchievements().stream().noneMatch(a -> a.getId().equals("nature_enthusiast"))) {
            Achievement achievement = new Achievement(
                "nature_enthusiast", 
                "Nature Enthusiast", 
                "Touch grass 7 days in a row", 
                500
            );
            stats.addAchievement(achievement);
            levelingService.addXp(achievement.getXpReward(), "Achievement unlocked");
        }
        
        if (stats.getGrassStreak() >= 30 && 
            stats.getAchievements().stream().noneMatch(a -> a.getId().equals("outdoor_legend"))) {
            Achievement achievement = new Achievement(
                "outdoor_legend", 
                "Outdoor Legend", 
                "Touch grass 30 days in a row", 
                2000
            );
            stats.addAchievement(achievement);
            levelingService.addXp(achievement.getXpReward(), "Achievement unlocked");
        }
    }
    
    private void showNotification(String title, String content, NotificationType type) {
        Notification notification = new Notification(
            "DevGame",
            title,
            content,
            type
        );
        Notifications.Bus.notify(notification, project);
    }
}
