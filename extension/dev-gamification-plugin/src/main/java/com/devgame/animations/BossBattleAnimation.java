package com.devgame.animations;

import javax.swing.*;
import java.awt.*;

public class BossBattleAnimation extends JPanel {
    private final boolean victory;
    private int frame = 0;
    private Timer timer;
    private Runnable onComplete;
    
    private static final int TOTAL_FRAMES = 120;
    private static final Color VICTORY_COLOR = new Color(50, 205, 50);
    private static final Color DEFEAT_COLOR = new Color(220, 20, 60);
    
    public BossBattleAnimation(boolean victory) {
        this.victory = victory;
        setPreferredSize(new Dimension(600, 400));
        setBackground(new Color(20, 20, 30, 230));
    }
    
    public void start(Runnable onComplete) {
        this.onComplete = onComplete;
        timer = new Timer(16, e -> {
            frame++;
            repaint();
            
            if (frame >= TOTAL_FRAMES) {
                timer.stop();
                Timer delayTimer = new Timer(1000, evt -> {
                    if (this.onComplete != null) {
                        this.onComplete.run();
                    }
                });
                delayTimer.setRepeats(false);
                delayTimer.start();
            }
        });
        timer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        drawBoss(g2d, width, height);
        drawPlayer(g2d, width, height);
        
        if (frame > 30) {
            drawCombatEffects(g2d, width, height);
        }
        
        if (frame > 60) {
            drawResult(g2d, width, height);
        }
    }
    
    private void drawBoss(Graphics2D g2d, int width, int height) {
        int bossX = width - 200;
        int bossY = height / 2 - 50;
        
        if (!victory && frame > 60) {
            bossX += (Math.random() - 0.5) * 10;
            bossY += (Math.random() - 0.5) * 10;
        }
        
        g2d.setColor(new Color(139, 0, 0));
        g2d.fillOval(bossX, bossY, 100, 100);
        
        g2d.setColor(Color.RED);
        g2d.fillOval(bossX + 20, bossY + 30, 20, 20);
        g2d.fillOval(bossX + 60, bossY + 30, 20, 20);
        
        if (!victory && frame > 80) {
            g2d.setColor(new Color(255, 255, 255, 150));
            int explosionSize = (frame - 80) * 5;
            g2d.fillOval(bossX + 50 - explosionSize / 2, 
                        bossY + 50 - explosionSize / 2, 
                        explosionSize, explosionSize);
        }
        
        g2d.setColor(Color.RED);
        g2d.fillRect(bossX, bossY - 20, 100, 10);
        
        float bossHealth = victory ? 
            Math.max(0, 1 - (frame / 60f)) : 
            Math.max(0, (frame / 60f));
        g2d.setColor(Color.GREEN);
        g2d.fillRect(bossX, bossY - 20, (int)(100 * bossHealth), 10);
    }
    
    private void drawPlayer(Graphics2D g2d, int width, int height) {
        int playerX = 100;
        int playerY = height / 2 - 30;
        
        g2d.setColor(new Color(30, 144, 255));
        g2d.fillRect(playerX, playerY, 60, 80);
        
        g2d.setColor(new Color(255, 220, 177));
        g2d.fillOval(playerX + 10, playerY - 30, 40, 40);
        
        if (frame % 40 < 20) {
            g2d.setColor(Color.YELLOW);
            int swordX = playerX + 60;
            int swordY = playerY + 20;
            g2d.fillRect(swordX, swordY, 40, 10);
        }
        
        g2d.setColor(Color.RED);
        g2d.fillRect(playerX, playerY - 50, 60, 8);
        
        float playerHealth = !victory ? 
            Math.max(0, 1 - (frame / 60f)) : 
            Math.max(0.2f, (frame / 60f));
        g2d.setColor(Color.GREEN);
        g2d.fillRect(playerX, playerY - 50, (int)(60 * playerHealth), 8);
    }
    
    private void drawCombatEffects(Graphics2D g2d, int width, int height) {
        for (int i = 0; i < 10; i++) {
            float t = (frame - 30 + i * 5) / 60f;
            if (t < 0 || t > 1) continue;
            
            int x = (int)(100 + (width - 300) * t);
            int y = height / 2 + (int)(Math.sin(t * Math.PI * 4) * 50);
            
            g2d.setColor(new Color(255, 255, 0, (int)(255 * (1 - t))));
            g2d.fillOval(x, y, 10, 10);
        }
    }
    
    private void drawResult(Graphics2D g2d, int width, int height) {
        float alpha = Math.min(1f, (frame - 60) / 30f);
        
        g2d.setFont(new Font("Arial", Font.BOLD, 72));
        FontMetrics fm = g2d.getFontMetrics();
        
        String text = victory ? "VICTORY!" : "DEFEAT!";
        Color color = victory ? VICTORY_COLOR : DEFEAT_COLOR;
        
        int textWidth = fm.stringWidth(text);
        int x = (width - textWidth) / 2;
        int y = height / 2 + 150;
        
        for (int i = 5; i > 0; i--) {
            g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(50 * alpha / i)));
            g2d.drawString(text, x - i, y - i);
            g2d.drawString(text, x + i, y + i);
        }
        
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(255 * alpha)));
        g2d.drawString(text, x, y);
    }
}
