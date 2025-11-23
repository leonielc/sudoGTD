package com.devgame.ui;

import com.devgame.models.Achievement;
import com.devgame.models.PlayerStats;
import com.devgame.models.RewardPack;
import com.devgame.services.GameStateService;
import com.devgame.services.GrassStreakService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;

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

        // UI Components (Created once, updated often)
        private final JLabel streakLabel;
        private final JLabel maxStreakLabel;
        private final JButton touchGrassButton;
        private final JLabel levelValueLabel;
        private final JProgressBar xpBar;
        
        // Stat Labels
        private final JLabel linesLabel;
        private final JLabel testsRunLabel;
        private final JLabel testsPassedLabel;
        private final JLabel testsFailedLabel;
        private final JLabel bossesLabel;
        
        // Containers for dynamic lists
        private final JPanel achievementsContainer;
        private final JPanel packsContainer;
        private int lastAchievementCount = -1; // To track if we need to rebuild lists

        public StatsPanel(Project project) {
            this.project = project;
            setLayout(new BorderLayout());

            // --- 1. SETUP UI STRUCTURE (Run Once) ---
            
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Title
            JLabel title = new JLabel("🎮 Dev Game Stats");
            title.setFont(new Font("Arial", Font.BOLD, 24));
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(title);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

            // Grass Section
            JPanel grassPanel = new JPanel();
            grassPanel.setLayout(new BoxLayout(grassPanel, BoxLayout.Y_AXIS));
            grassPanel.setBorder(BorderFactory.createLineBorder(new Color(76, 175, 80), 2));
            grassPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel grassTitle = new JLabel("🌱 Touch Grass Streak");
            grassTitle.setFont(new Font("Arial", Font.BOLD, 16));
            grassTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            grassPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            grassPanel.add(grassTitle);

            streakLabel = new JLabel("Current Streak: 0 days");
            streakLabel.setFont(new Font("Arial", Font.BOLD, 14));
            streakLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            grassPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            grassPanel.add(streakLabel);

            maxStreakLabel = new JLabel("Best Streak: 0 days");
            maxStreakLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            grassPanel.add(maxStreakLabel);
            grassPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            touchGrassButton = new JButton("Touch Grass Today! 🌱");
            touchGrassButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            touchGrassButton.setBackground(new Color(76, 175, 80));
            touchGrassButton.setForeground(Color.WHITE);
            // Button Action
            touchGrassButton.addActionListener(e -> {
                GrassStreakService.getInstance(project).touchGrass();
                updateStatsDisplay(); // Update immediately on click
            });
            grassPanel.add(touchGrassButton);
            grassPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            mainPanel.add(grassPanel);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

            // Level Section
            JPanel levelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            levelPanel.add(new JLabel("Level: "));
            levelValueLabel = new JLabel("1");
            levelValueLabel.setFont(new Font("Arial", Font.BOLD, 14));
            levelPanel.add(levelValueLabel);
            mainPanel.add(levelPanel);

            xpBar = new JProgressBar(0, 100);
            xpBar.setStringPainted(true);
            xpBar.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(xpBar);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

            // Stats Section
            linesLabel = createStatRow(mainPanel, "Lines Written");
            testsRunLabel = createStatRow(mainPanel, "Tests Run");
            testsPassedLabel = createStatRow(mainPanel, "Tests Passed");
            testsFailedLabel = createStatRow(mainPanel, "Tests Failed");
            bossesLabel = createStatRow(mainPanel, "Bosses Defeated");
            
            mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

            // Dynamic Containers (Achievements/Packs)
            achievementsContainer = new JPanel();
            achievementsContainer.setLayout(new BoxLayout(achievementsContainer, BoxLayout.Y_AXIS));
            achievementsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
            mainPanel.add(achievementsContainer);
            
            packsContainer = new JPanel();
            packsContainer.setLayout(new BoxLayout(packsContainer, BoxLayout.Y_AXIS));
            packsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
            mainPanel.add(packsContainer);

            // Add to Scroll Pane
            JBScrollPane scrollPane = new JBScrollPane(mainPanel);
            add(scrollPane, BorderLayout.CENTER);

            // --- 2. START TIMER ---
            updateStatsDisplay(); // Initial load
            updateTimer = new Timer(1000, e -> updateStatsDisplay());
            updateTimer.start();
        }

        private JLabel createStatRow(JPanel parent, String title) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel titleLbl = new JLabel(title + ": ");
            titleLbl.setFont(new Font("Arial", Font.BOLD, 12));
            JLabel valueLbl = new JLabel("0");
            panel.add(titleLbl);
            panel.add(valueLbl);
            parent.add(panel);
            return valueLbl;
        }

        // --- 3. UPDATE LOGIC (Repeatedly called) ---
        private void updateStatsDisplay() {
            PlayerStats stats = GameStateService.getInstance().getPlayerStats();
            if (stats == null) return;

            // Update Simple Text Fields
            streakLabel.setText("Current Streak: " + stats.getGrassStreak() + " days 🔥");
            maxStreakLabel.setText("Best Streak: " + stats.getMaxGrassStreak() + " days");
            
            if (stats.canTouchGrassToday()) {
                touchGrassButton.setText("Touch Grass Today! 🌱");
                touchGrassButton.setEnabled(true);
                touchGrassButton.setBackground(new Color(76, 175, 80));
            } else {
                touchGrassButton.setText("Already Touched Grass Today ✅");
                touchGrassButton.setEnabled(false);
                touchGrassButton.setBackground(Color.GRAY);
            }

            levelValueLabel.setText(String.valueOf(stats.getLevel()));
            
            xpBar.setValue((int)(stats.getXpProgress() * 100));
            xpBar.setString(stats.getXp() + " / " + stats.getXpForNextLevel() + " XP");

            linesLabel.setText(String.valueOf(stats.getLinesWritten()));
            testsRunLabel.setText(String.valueOf(stats.getTestsRun()));
            testsPassedLabel.setText(String.valueOf(stats.getTestsPassed()));
            testsFailedLabel.setText(String.valueOf(stats.getTestsFailed()));
            bossesLabel.setText(String.valueOf(stats.getBossesDefeated()));

            // Update Lists only if size changed (to prevent flickering)
            updateAchievements(stats.getAchievements());
            updatePacks(stats.getRewardPacks());
        }
        
        private void updateAchievements(List<Achievement> achievements) {
            if (achievements.size() == lastAchievementCount) return;
            
            lastAchievementCount = achievements.size();
            achievementsContainer.removeAll();
            
            if (!achievements.isEmpty()) {
                JLabel title = new JLabel("🏆 Achievements");
                title.setFont(new Font("Arial", Font.BOLD, 16));
                achievementsContainer.add(title);
                achievementsContainer.add(Box.createRigidArea(new Dimension(0, 5)));
                
                for (Achievement a : achievements) {
                    JLabel lbl = new JLabel("• " + a.getName() + " - " + a.getDescription());
                    achievementsContainer.add(lbl);
                }
                achievementsContainer.add(Box.createRigidArea(new Dimension(0, 15)));
            }
            achievementsContainer.revalidate();
            achievementsContainer.repaint();
        }
        
        private void updatePacks(List<RewardPack> packs) {
           // Simplified update for packs to save space
            packsContainer.removeAll();
            if (!packs.isEmpty()) {
                long unopened = packs.stream().filter(p -> !p.isOpened()).count();
                JLabel packCount = new JLabel("🎁 Reward Packs: " + unopened + " unopened / " + packs.size() + " total");
                packCount.setFont(new Font("Arial", Font.BOLD, 14));
                packsContainer.add(packCount);
            }
            packsContainer.revalidate();
            packsContainer.repaint();
        }
    }
}