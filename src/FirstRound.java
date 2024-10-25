import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

public class FirstRound extends Thread {
    // Constants and Variables
    
    private EarthlingAttack currentEarthlingAttack;
    private Alien currentAlien;
    private AlienAttack currentAlienAttack;

    private final ArrayList<EarthlingAttack> earthlingAttacks = new ArrayList<>();
    private final ArrayList<Alien> aliens = new ArrayList<>();
    private final ArrayList<AlienAttack> alienAttacks = new ArrayList<>();

    private final int FrameDelay = 20;
    private long previousTime;
    private int frameCount;
    private int totalScore;

    private BGM backgroundMusic;
    private BGM hitEffectSound;
    
    private final int EarthlingSpeed = 10;
    private int earthlingHealth = 30;
    private boolean currentStage;
    private boolean moveUp;
    private boolean moveDown;
    private boolean moveLeft;
    private boolean moveRight;
    private boolean isShooting;
    private boolean isStageOver;
    private boolean isNextStage = false;
    private boolean isGameOver = false;
    private Image earthlingImage = new ImageIcon("src/images/earthling.png").getImage();
    private int earthlingPositionX;
    private int earthlingPositionY;
    private final int EarthlingWidth = earthlingImage.getWidth(null);
    private final int EarthlingHeight = earthlingImage.getHeight(null);

    // Getters and Setters
    public boolean isNextStage() {
        return isNextStage;
    }

    public boolean isStageOver() {
        return isStageOver;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public boolean isCurrentStage() {
        return this.currentStage;
    }

    public void setMoveUp(boolean moveUp) {
        this.moveUp = moveUp;
    }

    public void setMoveDown(boolean moveDown) {
        this.moveDown = moveDown;
    }

    public void setMoveLeft(boolean moveLeft) {
        this.moveLeft = moveLeft;
    }

    public void setMoveRight(boolean moveRight) {
        this.moveRight = moveRight;
    }

    public void setShooting(boolean isShooting) {
        this.isShooting = isShooting;
    }

    public void setEarthlingImage(Image earthlingImage) {
        this.earthlingImage = earthlingImage;
    }

    public Image getEarthlingImage() {
        return this.earthlingImage;
    }

    @Override
    public void run() {
        backgroundMusic = new BGM("src/audio/FirstRoundBGM.wav", true);
        hitEffectSound = new BGM("src/audio/HitSound.wav", false);

        reset();
        while (true) {
            while (!isStageOver) {
                previousTime = System.currentTimeMillis();
                if (System.currentTimeMillis() - previousTime < FrameDelay) {
                    try {
                        Thread.sleep(FrameDelay - System.currentTimeMillis() + previousTime);
                        currentStage = true;
                        processKeyInput();
                        processEarthlingAttacks();
                        spawnAliens();
                        moveAliens();
                        processAlienAttacks();
                        frameCount++;

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Game logic methods
    public void reset() {
        isStageOver = false;
        isGameOver = false;
        frameCount = 0;
        totalScore = 0;
        earthlingPositionX = 10;
        earthlingPositionY = (Main.ScreenHeight - EarthlingHeight) / 2;
        earthlingHealth = 30;

        backgroundMusic.start();

        earthlingAttacks.clear();
        aliens.clear();
        alienAttacks.clear();
    }

    private void processKeyInput() {
        if (moveUp && earthlingPositionY - EarthlingSpeed > 0) {
            earthlingPositionY -= EarthlingSpeed;
        }
        if (moveDown && earthlingPositionY 
            + EarthlingHeight + EarthlingSpeed < Main.ScreenHeight) {
            earthlingPositionY += EarthlingSpeed;
        }
        if (moveLeft && earthlingPositionX - EarthlingSpeed > 0) {
            earthlingPositionX -= EarthlingSpeed;
        }
        if (moveRight && earthlingPositionX 
            + EarthlingWidth + EarthlingSpeed < Main.ScreenWidth) {
            earthlingPositionX += EarthlingSpeed;
        }
        if (isShooting && frameCount % 15 == 0) {
            currentEarthlingAttack = new EarthlingAttack (
                earthlingPositionX + 222, earthlingPositionY + 100);
            earthlingAttacks.add(currentEarthlingAttack);
        }
    }

    private void processEarthlingAttacks() {
        for (int i = 0; i < earthlingAttacks.size(); i++) {
            currentEarthlingAttack = earthlingAttacks.get(i);
            currentEarthlingAttack.shoot();

            for (int j = 0; j < aliens.size(); j++) {
                currentAlien = aliens.get(j);
                if (currentEarthlingAttack.x > currentAlien.x  
                    && currentEarthlingAttack.x < currentAlien.x + currentAlien.width
                    && currentEarthlingAttack.y > currentAlien.y 
                    && currentEarthlingAttack.y < currentAlien.y + currentAlien.height) {
                    currentAlien.health -= currentEarthlingAttack.attack;
                    earthlingAttacks.remove(currentEarthlingAttack);
                }
                if (currentAlien.health <= 0) {
                    hitEffectSound.start();
                    aliens.remove(currentAlien);
                    totalScore += 1000;
                }
            }
        }
    }

    private void spawnAliens() {
        if (frameCount % 80 == 0) {
            currentAlien = new Alien(1120, (int) (Math.random() * 621));
            aliens.add(currentAlien);
        }
    }

    private void moveAliens() {
        for (int i = 0; i < aliens.size(); i++) {
            currentAlien = aliens.get(i);
            currentAlien.move();
        }
    }

    private void processAlienAttacks() {
        if (frameCount % 50 == 0) {
            currentAlienAttack = new AlienAttack(currentAlien.x - 79, currentAlien.y + 35);
            alienAttacks.add(currentAlienAttack);
        }

        for (int i = 0; i < alienAttacks.size(); i++) {
            currentAlienAttack = alienAttacks.get(i);
            currentAlienAttack.shoot();

            if (currentAlienAttack.x > earthlingPositionX 
                && currentAlienAttack.x < earthlingPositionX + EarthlingWidth
                && currentAlienAttack.y > earthlingPositionY 
                && currentAlienAttack.y < earthlingPositionY + EarthlingHeight) {
                hitEffectSound.start();
                earthlingHealth -= currentAlienAttack.attack;
                alienAttacks.remove(currentAlienAttack);
                if (earthlingHealth <= 0) {
                    isStageOver = true;
                    isGameOver = true;
                }
            }
        }
    }

    // Drawing methods
    public void drawGame(Graphics g) {
        drawEarthling(g);
        drawAliens(g);
        drawGameInfo(g);
    }

    public void drawEarthling(Graphics g) {
        g.drawImage(earthlingImage, earthlingPositionX, earthlingPositionY, null);
        g.setColor(Color.GREEN);
        g.fillRect(earthlingPositionX - 1, earthlingPositionY - 40, earthlingHealth * 6, 20);
        for (int i = 0; i < earthlingAttacks.size(); i++) {
            currentEarthlingAttack = earthlingAttacks.get(i);
            g.drawImage(currentEarthlingAttack.image, 
                currentEarthlingAttack.x, currentEarthlingAttack.y, null);
        }
    }

    public void drawAliens(Graphics g) {
        for (int i = 0; i < aliens.size(); i++) {
            currentAlien = aliens.get(i);
            g.drawImage(currentAlien.image, currentAlien.x, currentAlien.y, null);
            g.setColor(Color.GREEN);
            g.fillRect(currentAlien.x + 1, currentAlien.y - 40, currentAlien.health * 15, 20);
        }
        for (int i = 0; i < alienAttacks.size(); i++) {
            currentAlienAttack = alienAttacks.get(i);
            g.drawImage(currentAlienAttack.image, currentAlienAttack.x, currentAlienAttack.y, null);
        }
    }

    public void drawGameInfo(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Times New Roman", Font.BOLD, 35));
        g.drawString("SCORE : " + totalScore, 40, 80);
        g.drawString("MISSION : Reach 10000 Points", 800, 80);
        if (isStageOver && isGameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Times New Roman", Font.BOLD, 80));
            g.drawString("Press R Key To Restart", 295, 380);
        }
        if (totalScore == 10000) {
            isNextStage = true;
            isStageOver = true;
            backgroundMusic.stop();
            g.setColor(Color.RED);
            g.setFont(new Font("Times New Roman", Font.BOLD, 40));
            g.drawString("SUCCESS! Press C Key To Continue To The Final Level", 150, 380);
        }
    }
}
