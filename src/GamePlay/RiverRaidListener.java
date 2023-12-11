package GamePlay;

import Texture.AnimListener;
import Texture.TextureReader;
import com.sun.opengl.util.j2d.TextRenderer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import javax.media.opengl.GLCanvas;
import javax.swing.Timer;
import java.io.File;

import javax.sound.sampled.*;


public class RiverRaidListener extends AnimListener implements KeyListener, MouseListener, GLEventListener {

    GL gl;
    AudioInputStream audioStream;
    private Timer gameTimer;
    private Clip clip;

    private int elapsedMinutes, elapsedSeconds;

    int xPosition = 90;
    int yPosition = 90;
    String page = "Home", gameLevel, playMode;
    boolean sound = true, pause;

    final private int maxWidth = 1000;
    final private int maxHeight = 700;
    int maxRightMovement = 730;
    int maxLeftMovement = 215;
    int planeMovementSpeed = 10;

    int timer, delayShowEnemy, counter, score, delayDestroy, lives = 3, backGroundMove = 400, bulletSpeed;
    private long lastFireTime = 0;

    Entity plane = new Entity();
    int numberOfEnemies = 5;

    Entity[] enemy = new Entity[numberOfEnemies];
    int enemySpeed = 8;

    BitSet keyBits = new BitSet(256);
    String[] textureNames = {"Plane", "Fire2", "Bullet", "Ship", "Helicopter", "Fire",
            "Pause", "Score", "River", "Home", "HowToPlay", "Menu", "Sound", "Muted", "Right",
            "Left", "Win", "GameOver", "Levels"
    };

    int[] enemiesIndex = {3, 4};
    TextureReader.Texture[] texture = new TextureReader.Texture[textureNames.length];
    int[] textures = new int[textureNames.length];
    //TODO: fixme (pixelated)
    TextRenderer textRenderer = new TextRenderer(new Font("Arial", Font.PLAIN, 10));
    ArrayList<Bullet> bullets = new ArrayList<>();

    float bulletScale = 0.3f;
    float planeScale = 1.0f;
    int archeryIndex = 7;
    float archeryScale = 0.9f;
    int pauseIndex = 6;
    float pauseScale = 0.8f;
    int bulletIndex = 2;
    int winningScore = 100;
    String clipPath;
    String gameClip;

    private GLCanvas glc;


    public void setGLCanvas(GLCanvas glc) {
        this.glc = glc;
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        for (int i = 0; i < textureNames.length; i++) {
            System.out.println(i + " = " + textureNames[i]);
        }
        clipPath = "Assets//Music//awaken.wav";
        playClip(clipPath, false);
        gl = glAutoDrawable.getGL();
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);    //This Will Clear The Background Color To Black

        gl.glEnable(GL.GL_TEXTURE_2D);  // Enable Texture Mapping
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);


        gl.glGenTextures(textureNames.length, textures, 0);
        gl.glEnable(GL.GL_TEXTURE_2D);  // Enable Texture Mapping
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glGenTextures(textureNames.length, textures, 0);
        for (int i = 0; i < textureNames.length; i++) {
            try {
                texture[i] = TextureReader.readTexture(assetsFolderName + textureNames[i] + ".png", true);
                gl.glBindTexture(GL.GL_TEXTURE_2D, textures[i]);

                new GLU().gluBuild2DMipmaps(
                        GL.GL_TEXTURE_2D,
                        GL.GL_RGBA, // Internal Texel Format,
                        texture[i].getWidth(), texture[i].getHeight(),
                        GL.GL_RGBA, // External format from image,
                        GL.GL_UNSIGNED_BYTE,
                        texture[i].getPixels() // Imagedata
                );
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        CreateEnemy();

        gameTimer = new Timer(1000, e -> {
            updateTime();
            glc.repaint();
        });
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        gl = glAutoDrawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        DrawObject(plane.x, plane.y, planeScale, 0, plane.idx);
        HandleNavigation();
    }

    public void HandleNavigation() {
        switch (page) {
            case "Home":
                int soundIndex = sound ? 12 : 13;
                if (soundIndex == 12) {
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                }
                if (soundIndex == 13) {
                    clip.stop();
                }
                DrawBackground(gl, 9);
                DrawObject(850, 600, 1.0f, 0, soundIndex);
                textRenderer.beginRendering(100, 100);
                textRenderer.setColor(Color.WHITE);
                textRenderer.endRendering();
                break;
            case "HowToPlay":
                DrawBackground(gl, 10);
                break;
            case "Levels":
                DrawBackground(gl, 18);
                break;
            case "Game":
                if (!sound) {
                    clip.stop();
                }
                handleKeyPress();
                DrawBackground(gl, 8);
                DrawObject(plane.x, plane.y, 1, 0, plane.idx);
                DrawObject(maxWidth - 100, backGroundMove, 5, 0, 14);
                DrawObject(35, backGroundMove, 5, 0, 15);

                gameTimer = new Timer(1000, e -> updateTime());
                DrawEnemy();
                DestroyEnemy();
                Crash();
                Fire();
                if (plane.idx == 1) {
                    timer++;
                    if (timer > 10) {
                        plane = new Entity();
                        timer = 0;
                    }
                }
                DrawObject(50, 600, archeryScale, 0, archeryIndex);
                DrawObject(850, 600, pauseScale, 0, pauseIndex);
                textRenderer.beginRendering(200, 200);
                textRenderer.setColor(Color.WHITE);
                textRenderer.draw(score + "", 50, 170);
                textRenderer.draw(String.format("%02d:%02d", elapsedMinutes, elapsedSeconds), 160, 5);
                textRenderer.endRendering();

                gameTimer.start();

                if (pause) {
                    DrawObject(450, 350, 7, 0, 11);
                    textRenderer.beginRendering(100, 100);
                    textRenderer.setColor(Color.GRAY);
                    textRenderer.endRendering();
                }
                if (score == winningScore) {
                    gameClip = "Assets//Music//win.wav";
                    playClip(gameClip, false);
                    page = "Win";
                }
                if (lives == 0) {
                    gameClip = "Assets//Music//lose.wav";
                    playClip(gameClip, false);
                    page = "Lose";
                }

                break;
            case "Win":
                DrawBackground(gl, 16);
                break;
            case "Lose":
                DrawBackground(gl, 17);
                break;
        }
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {

    }

    @Override
    public void displayChanged(GLAutoDrawable glAutoDrawable, boolean b, boolean b1) {

    }

    public void Fire() {
        bulletSpeed = pause ? 0 : 20;
        for (Bullet bullet : bullets) {
            if (bullet.isFired) {
                bullet.y += bulletSpeed;
                DrawObject(bullet.x, bullet.y + 35, bulletScale, 0, bulletIndex);
            }
        }
    }

    public void DrawBackground(GL gl, int index) {
        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[index]);    // Turn Blending On
        gl.glPushMatrix();
        gl.glBegin(GL.GL_QUADS);
        // Front Face
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glEnd();
        gl.glPopMatrix();

        gl.glDisable(GL.GL_BLEND);
    }

    public void DrawObject(int x, int y, float scale, double degree, int index) {
        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[index]);    // Turn Blending On

        gl.glPushMatrix();
        gl.glTranslated(x / (maxWidth / 2.0) - 0.9, y / (maxHeight / 2.0) - 0.9, 0);
        gl.glScaled(0.1 * scale, 0.1 * scale, 1);
        if (index == 14 || index == 15) {
            gl.glScaled(0.1 * scale, 0.5 * scale, 1);

        }
        gl.glRotated(degree, 0, 0, 1);
        gl.glBegin(GL.GL_QUADS);
        // Front Face
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glEnd();
        gl.glPopMatrix();
        gl.glDisable(GL.GL_BLEND);
    }

    public void CreateEnemy() {
        int x, idx;
        int y = 650;
        for (int i = 0; i < enemy.length; i++) {
            x = (int) (Math.random() * 500 + 200);
            idx = (int) (Math.random() * enemiesIndex.length);
            enemy[i] = new Entity(x, y, enemiesIndex[idx]);
        }
    }

    void DrawEnemy() {
        delayShowEnemy++;
        if (delayShowEnemy > 20 && !pause) {
            if (counter < enemy.length) {
                counter++;
            }
            delayShowEnemy = 0;
        }

        for (int i = 0; i < counter; i++) {
            if (pause) {
                enemy[i].speed = 0;
            } else {
                enemy[i].speed = enemySpeed;
            }
            DrawObject(enemy[i].x, enemy[i].y -= enemy[i].speed, 1, 0, enemy[i].idx);
            if (enemy[i].y < -50) {
                enemy[i].y = 650;
                enemy[i].x = (int) (Math.random() * 500 + 200);
                enemy[i].idx = enemiesIndex[(int) (Math.random() * enemiesIndex.length)];
            }
        }
    }

    void DestroyEnemy() {
        for (Bullet bullet : bullets) {
            for (Entity entity : enemy) {
                if (Math.abs(bullet.x - entity.x) < 50 && Math.abs(bullet.y - entity.y) < 50 && bullet.y < 600) {
                    System.out.println("destroy");
                    entity.idx = 5;
                    bullet.y = 1000;
                    entity.speed = 0;
                    score += entity.score;
                    System.out.println("score :" + score);
                }
                if (entity.idx == 5) {
                    delayDestroy++;
                    if (delayDestroy > 5) {
                        delayDestroy = 0;
                        entity.idx = enemiesIndex[(int) (Math.random() * enemiesIndex.length)];
                        entity.x = (int) (Math.random() * 500 + 200);
                        entity.y = 650;
                        entity.speed = enemySpeed;
                    }
                }
            }
        }
    }

    void Crash() {
        for (Entity entity : enemy) {
            if (Math.abs(plane.x - entity.x) < 75 && Math.abs(plane.y - entity.y) < 50) {
                System.out.println("Crash");
                plane.idx = 1;
                entity.idx = 5;
                entity.speed = 0;
                delayDestroy++;
                if (delayDestroy > 5) {
                    lives--;
                    System.out.println("lives :" + lives);
                    delayDestroy = 0;
                    entity.idx = enemiesIndex[(int) (Math.random() * enemiesIndex.length)];
                    entity.x = (int) (Math.random() * 500 + 200);
                    entity.y = 600;
                    entity.speed = 7;
                }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        keyBits.set(keyCode);
        if (page.equals("Game")) {
            if (keyBits.get(KeyEvent.VK_ESCAPE)) {
                pause = !pause;
            }
        }
        if (page.equals("Game") || page.equals("Home") || page.equals("win") || page.equals("lose")) {
            if (keyBits.get(KeyEvent.VK_M)) {
                clip.stop();
                sound = !sound;
            }
        }
    }

    public void handleKeyPress() {
        long currentTime = System.currentTimeMillis();
        if (plane.idx != 1 && !pause) {
            if (isKeyPressed(KeyEvent.VK_LEFT)) {
                if (plane.x >= maxLeftMovement) {
                    plane.x -= planeMovementSpeed;
                } else {
                    plane.idx = 1;
                    lives--;
                }
            }
            if (isKeyPressed(KeyEvent.VK_RIGHT)) {
                if (plane.x <= maxRightMovement) {
                    plane.x += planeMovementSpeed;
                } else {
                    plane.idx = 1;
                    lives--;
                }
            }

            if (keyBits.get(KeyEvent.VK_UP)) {
                backGroundMove -= 2;
                if (backGroundMove < 250) {
                    backGroundMove = 400;
                }
                for (Entity entity : enemy) {
                    entity.y -= 2;
                }
            }

            int fireDelay = 350;
            if (isKeyPressed(KeyEvent.VK_SPACE) && (currentTime - lastFireTime >= fireDelay)) {
                bullets.add(new Bullet(plane.x, plane.y));
                gameClip = "Assets//Music//fire.wav";
                playClip(gameClip, false);
                lastFireTime = currentTime;
            }
        }
    }

    public boolean isKeyPressed(final int keyCode) {
        return keyBits.get(keyCode);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyBits.clear(e.getKeyCode());
        for (Entity entity : enemy) {
            entity.speed = enemySpeed;
        }
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();


//        System.out.println(x + " " + y);
        Component c = e.getComponent();
        double width = c.getWidth();
        double height = c.getHeight();
        System.out.println(width + " " + height);
//get percent of GLCanvas instead of
//points and then converting it to our
//'100' based coordinate system.

        xPosition = (int) ((x / width) * 100);
        yPosition = ((int) ((y / height) * 100));
        //  reversing direction of y-axis
        yPosition = 100 - yPosition;

        System.out.println(xPosition + " " + yPosition);

        switch (page) {
            case "Home":
                if (xPosition <= 65 && xPosition >= 34 && yPosition <= 61 && yPosition >= 51) {
                    page = "Levels";
                    playMode = "single";
                }
                if (xPosition <= 65 && xPosition >= 34 && yPosition <= 32 && yPosition >= 21) {
                    page = "HowToPlay";
                }
                if (xPosition <= 55 && xPosition >= 44 && yPosition <= 9 && yPosition >= 2) {
                    System.out.println("Exit button clicked");
                    System.exit(0);
                }
                if (xPosition >= 87 && xPosition <= 94 && yPosition >= 85 && yPosition <= 95) {
                    System.out.println("Mute clicked");
                    clip.stop();
                    sound = !sound;
                }

                if (xPosition >= 34 && xPosition <= 65 && yPosition >= 37 && yPosition <= 45) {
                    page = "Levels";
                    playMode = "multi";
                }

                break;

            case "HowToPlay":
                if (xPosition <= 59 && xPosition >= 40 && yPosition <= 22 && yPosition >= 15) {
                    page = "Home";
                }
                break;
            case "Levels":
                if (xPosition <= 65 && xPosition >= 35 && yPosition <= 22 && yPosition >= 15) {
                    page = "Home";
                }
                if (xPosition <= 65 && xPosition >= 35 && yPosition <= 65 && yPosition >= 55) {
                    page = "Game";
                    gameLevel = "easy";
                    EasyLevel();
                }
                if (xPosition <= 65 && xPosition >= 35 && yPosition <= 50 && yPosition >= 45) {
                    page = "Game";
                    gameLevel = "medium";
                    MediumLevel();
                }
                if (xPosition >= 34 && xPosition <= 65 && yPosition >= 28 && yPosition <= 40) {
                    page = "Game";
                    gameLevel = "hard";
                    HardLevel();
                }
                break;

            case "Game":
                if (xPosition >= 86 && xPosition <= 93 && yPosition >= 88 && yPosition <= 95) {
                    pause = true;
                    System.out.println("pause");
                }
                if (pause) {
                    if (xPosition >= 31 && xPosition <= 68 && yPosition >= 57 && yPosition <= 64) {
                        pause = false;
                        System.out.println("resume");
                    }
                    if (xPosition >= 31 && xPosition <= 68 && yPosition >= 44 && yPosition <= 51) {
                        Default();
                        System.out.println("restart");
                    }
                    if (xPosition >= 31 && xPosition <= 68 && yPosition >= 30 && yPosition <= 40) {
                        Default();
                        page = "Home";
                        System.out.println("Exit to Home");
                    }
                }
                break;

            case "Lose":
                if (xPosition >= 27 && xPosition <= 48 && yPosition >= 19 && yPosition <= 25) {
                    Default();
                    page = "Home";
                    System.out.println("Back to home");
                }
                if (xPosition >= 51 && xPosition <= 71 && yPosition >= 19 && yPosition <= 25) {
                    Default();
                    page = "Game";
                    System.out.println("Try Again");
                }
                break;
            case "Win":
                if (xPosition >= 27 && xPosition <= 48 && yPosition >= 19 && yPosition <= 25) {
                    Default();
                    page = "Home";
                    System.out.println("Home");
                }
                if (xPosition >= 51 && xPosition <= 71 && yPosition >= 19 && yPosition <= 25) {
                    page = "Game";
                    System.out.println(gameLevel);
                    switch (gameLevel) {
                        case "easy":
                            EasyLevel();
                            break;
                        case "medium":
                            MediumLevel();
                            break;
                        case "hard":
                            HardLevel();
                            break;
                        case "done":
                            page = "Home";
                            break;
                    }
                    System.out.println(gameLevel);
                }
        }
    }

    public void Default() {
        lives = 3;
        score = 0;
        plane.x = 450;
        plane.y = 50;
        CreateEnemy();
        elapsedSeconds = 0;
        elapsedMinutes = 0;
        updateTime();
        pause = false;
        counter = 0;
        bullets.clear();
    }

    public void EasyLevel() {
        Default();
        gameLevel = "medium";
        winningScore = 100;
    }

    public void MediumLevel() {
        Default();
        gameLevel = "hard";
        winningScore = 250;
        numberOfEnemies += 5;
        planeMovementSpeed -= 1;
        enemySpeed = 14;
    }

    public void HardLevel() {
        Default();
        gameLevel = "done";
        numberOfEnemies += 10;
        planeMovementSpeed -= 1;
        winningScore = 350;
        enemySpeed = 16;
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }


    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    private void updateTime() {
        if (!pause) {
            elapsedSeconds++;
            if (elapsedSeconds == 60) {
                elapsedSeconds = 0;
                elapsedMinutes++;
            }
        }
    }

    public void playClip(String path, boolean isLoop) {
        try {
            audioStream = AudioSystem.getAudioInputStream(new File(path));
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            if (isLoop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.start();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}