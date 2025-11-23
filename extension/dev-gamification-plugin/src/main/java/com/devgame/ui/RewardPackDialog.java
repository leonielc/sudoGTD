package com.devgame.ui;

import com.devgame.models.RewardPack;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class RewardPackDialog extends DialogWrapper {
    private final RewardPack pack;
    
    public RewardPackDialog(Project project, RewardPack pack) {
        super(project);
        this.pack = pack;
        setTitle("Reward Pack Earned!");
        init();
    }
    
    public static void show(Project project, RewardPack pack) {
        SwingUtilities.invokeLater(() -> {
            RewardPackDialog dialog = new RewardPackDialog(project, pack);
            dialog.show();
            pack.setOpened(true);
        });
    }
    
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel(pack.getType().getEmoji() + " " + pack.getType().getDisplayName());
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JLabel congrats = new JLabel("You've earned a reward pack for passing tests!");
        congrats.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(congrats);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JLabel itemsTitle = new JLabel("Contents:");
        itemsTitle.setFont(new Font("Arial", Font.BOLD, 16));
        itemsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(itemsTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        for (String item : pack.getItems()) {
            JLabel itemLabel = new JLabel("✨ " + item);
            itemLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            itemLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(itemLabel);
        }
        
        panel.setPreferredSize(new Dimension(400, 300));
        return panel;
    }
}
