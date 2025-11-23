package com.devgame.ui;

import com.devgame.models.Achievement;
import com.devgame.models.PlayerStats;
import com.devgame.services.GameStateService;
import com.devgame.services.GrassStreakService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class StatsToolWindow implements ToolWindowFactory {
    
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        StatsPanel panel = new StatsPanel(project);
        Content content = ContentFactory.getInstance().createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }
    
    private static class StatsPanel extends JPanel {
        private final Project project;
        private final Timer updateTimer;
        
        public StatsPanel(Project project) {
            this.project = project;
            setLayout(new BorderLayout());
            
            updateTimer = new Timer(1000, e -> refreshStats());
            updateTimer.start();
            
            refreshStats();
        }
        
        private void refreshStats() {
            removeAll();
            
            PlayerStats stats = GameStateService.getInstance().getPlayerStats();
            
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            JLabel title = new JLabel("🎮 Dev Game Stats");
            title.setFont(new Font("Arial", Font.BOLD, 24));
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(title);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            
            // Grass Streak Section - MOVED TO TOP
            try {
                JPanel grassPanel = new JPanel();
                grassPanel.setLayout(new BoxLayout(grassPanel, BoxLayout.Y_AXIS));
                grassPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
                grassPanel.setMaximumSize(new Dimension(400, 150));
                
                JLabel grassTitle = new JLabel("🌱 Touch Grass Streak");
                grassTitle.setFont(new Font("Arial", Font.BOLD, 16));
                grassTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
                grassPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                grassPanel.add(grassTitle);
                
                JLabel streakLabel = new JLabel("Current Streak: " + stats.getGrassStreak() + " days 🔥");
                streakLabel.setFont(new Font("Arial", Font.BOLD, 14));
                streakLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                grassPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                grassPanel.add(streakLabel);
                
                JLabel maxStreakLabel = new JLabel("Best Streak: " + stats.getMaxGrassStreak() + " days");
                maxStreakLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                grassPanel.add(maxStreakLabel);
                
                grassPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                
                JButton touchGrassButton = new JButton("Touch Grass Today! 🌱");
                touchGrassButton.setFont(new Font("Arial", Font.BOLD, 14));
                touchGrassButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                touchGrassButton.setBackground(new Color(76, 175, 80));
                touchGrassButton.setForeground(Color.WHITE);
                touchGrassButton.setFocusPainted(false);
                touchGrassButton.setEnabled(stats.canTouchGrassToday());
                
                if (!stats.canTouchGrassToday()) {
                    touchGrassButton.setText("Already Touched Grass Today ✅");
                    touchGrassButton.setBackground(Color.GRAY);
                }
                
                touchGrassButton.addActionListener(e -> {
                    GrassStreakService.getInstance(project).touchGrass();
                });
                
                grassPanel.add(touchGrassButton);
                grassPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                
                mainPanel.add(grassPanel);
                mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
                
                System.out.println("DevGame: Grass panel added successfully!");
            } catch (Exception e) {
                System.err.println("DevGame: Error creating grass panel: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Level and XP
            JPanel levelPanel = createInfoPanel("Level", String.valueOf(stats.getLevel()));
            mainPanel.add(levelPanel);
            
            JProgressBar xpBar = new JProgressBar(0, 100);
            xpBar.setValue((int)(stats.getXpProgress() * 100));
            xpBar.setString(stats.getXp() + " / " + stats.getXpForNextLevel() + " XP");
            xpBar.setStringPainted(true);
            xpBar.setPreferredSize(new Dimension(300, 30));
            mainPanel.add(xpBar);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            
            // Stats
            mainPanel.add(createInfoPanel("Lines Writtenn", String.valueOf(stats.getLinesWritten())));
            mainPanel.add(createInfoPanel("Tests Run", String.valueOf(stats.getTestsRun())));
            mainPanel.add(createInfoPanel("Tests Passed", String.valueOf(stats.getTestsPassed())));
            mainPanel.add(createInfoPanel("Tests Failed", String.valueOf(stats.getTestsFailed())));
            mainPanel.add(createInfoPanel("Bosses Defeated", String.valueOf(stats.getBossesDefeated())));
            mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            
            // Achievements
            if (!stats.getAchievements().isEmpty()) {
                JLabel achievementTitle = new JLabel("🏆 Achievements");
                achievementTitle.setFont(new Font("Arial", Font.BOLD, 18));
                mainPanel.add(achievementTitle);
                mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                
                for (Achievement achievement : stats.getAchievements()) {
                    JLabel achLabel = new JLabel("• " + achievement.getName() + " - " + achievement.getDescription());
                    mainPanel.add(achLabel);
                }
                mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            }
            
            // Reward Packs
            if (!stats.getRewardPacks().isEmpty()) {
                JLabel packTitle = new JLabel("🎁 Reward Packs");
                packTitle.setFont(new Font("Arial", Font.BOLD, 18));
                mainPanel.add(packTitle);
                mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                
                long unopened = stats.getRewardPacks().stream().filter(p -> !p.isOpened()).count();
                JLabel packCount = new JLabel("Unopened: " + unopened + " / Total: " + stats.getRewardPacks().size());
                mainPanel.add(packCount);
            }
            
            JScrollPane scrollPane = new JScrollPane(mainPanel);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            add(scrollPane, BorderLayout.CENTER);
            
            revalidate();
            repaint();
        }
        
        private JPanel createInfoPanel(String label, String value) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel labelComp = new JLabel(label + ": ");
            labelComp.setFont(new Font("Arial", Font.BOLD, 14));
            JLabel valueComp = new JLabel(value);
            valueComp.setFont(new Font("Arial", Font.PLAIN, 14));
            panel.add(labelComp);
            panel.add(valueComp);
            return panel;
        }
    }
}
