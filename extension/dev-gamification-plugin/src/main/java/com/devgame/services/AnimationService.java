package com.devgame.services;

import com.devgame.animations.BossBattleAnimation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;
import java.awt.*;

public class AnimationService {
    private final Project project;
    
    public AnimationService(Project project) {
        this.project = project;
    }
    
    public static AnimationService getInstance(Project project) {
        return project.getService(AnimationService.class);
    }
    
    public void showBossBattle(boolean victory) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = WindowManager.getInstance().getFrame(project);
            if (frame == null) return;
            
            BossBattleAnimation animation = new BossBattleAnimation(victory);
            
            JWindow overlay = new JWindow(frame);
            overlay.setLayout(new BorderLayout());
            overlay.add(animation, BorderLayout.CENTER);
            overlay.setSize(600, 400);
            overlay.setLocationRelativeTo(frame);
            overlay.setAlwaysOnTop(true);
            
            animation.start(() -> overlay.dispose());
            overlay.setVisible(true);
        });
    }
}
