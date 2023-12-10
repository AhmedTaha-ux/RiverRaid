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


public class RiverRaidListener extends AnimListener implements KeyListener,MouseListener, GLEventListener {

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
    int maxUpMovement = 600;
    int maxDownMovement = 20;
    int maxRightMovement = 730;
    int maxLeftMovement = 175;
    int planeMovementSpeed = 8;

    int timer, delayShowEnemy, counter, score, delayDestroy, lives = 3, bankermove = 400,bulletSpeed;
    private long lastFireTime = 0;
    private final long fireDelay = 500;

    Entity hero = new Entity();

    Entity[] enemy = new Entity[5];

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

    private GLCanvas glc;


    public void setGLCanvas(GLCanvas glc) {
        this.glc = glc;
    }
    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        for (int i = 0; i < textureNames.length; i++) {
            System.out.println(i + " = " + textureNames[i]);
        }

        try {
            audioStream = AudioSystem.getAudioInputStream(new File("Assets//Music//chicken.wav"));
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

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

        handleKeyPress();
        DrawObject(hero.x, hero.y, planeScale, 0, hero.idx);


        switch (page) {
            case "Home":
                int soundIndex = sound ? 12 : 13;
                if(soundIndex == 12){
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                }
                DrawBackground(gl,9);
                DrawObject(850, 600, 1.0f, 0, soundIndex);
                textRenderer.beginRendering(100, 100);
                textRenderer.setColor(Color.WHITE);
                textRenderer.endRendering();
                break;
            case "HowToPlay":
                DrawBackground(gl,10);
                break;
            case "Levels":
                DrawBackground(gl,18);
                break;
            case "Game":
                DrawBackground(gl,8);
                DrawObject(hero.x, hero.y, 1, 0, hero.idx);
                DrawObject(maxWidth - 100, bankermove, 5, 0, 14);
                DrawObject(35, bankermove, 5, 0, 15);

                gameTimer = new Timer(1000, e -> {
                    updateTime();
                });
                DrawEnemy();
                DestroyEnemy();
                Crash();
                Fire();
                if (hero.idx == 1) {
                    timer++;
                    if (timer > 10) {
                        hero = new Entity();
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
                if (score >= 5) {
                    page = "Win";
                }
                if (lives == 0) {
                    page = "Lose";
                }

                break;
            case "Win":
                DrawBackground(gl,16);
                break;
            case "Lose":
                DrawBackground(gl,17);
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
        bulletSpeed = pause ? 0 : 9;
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
        for (int i = 0; i < enemy.length; i++) {
            x = (int) (Math.random() * 500 + 200);
            idx = (int) (Math.random() * enemiesIndex.length);
            enemy[i] = new Entity(x, 600, enemiesIndex[idx]);
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
                enemy[i].speed = 7;
            }
            DrawObject(enemy[i].x, enemy[i].y -= enemy[i].speed, 1, 0, enemy[i].idx);
            if (enemy[i].y < -50) {
                enemy[i].y = 600;
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
                        entity.y = 600;
                        entity.speed = 7;
                    }
                }
            }
        }
    }

    void Crash() {
        for (Entity entity : enemy) {
            if (Math.abs(hero.x - entity.x) < 75 && Math.abs(hero.y - entity.y) < 50) {
                System.out.println("Crash");
                hero.idx = 1;
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
    }

    public void handleKeyPress() {
        long currentTime = System.currentTimeMillis();
        if (hero.idx != 1 && !pause) {
            if (isKeyPressed(KeyEvent.VK_LEFT)) {
                if (hero.x >= maxLeftMovement) {
                    hero.x -= planeMovementSpeed;
                } else {
                    hero.idx = 1;
                }
            }
            if (isKeyPressed(KeyEvent.VK_RIGHT)) {
                if (hero.x <= maxRightMovement) {
                    hero.x += planeMovementSpeed;
                } else {
                    hero.idx = 1;
                }
            }

            if (keyBits.get(KeyEvent.VK_UP)) {
                bankermove -= 2;
                if (bankermove < 250) {
                    bankermove = 400;
                }
                for (Entity entity : enemy) {
                    entity.speed = 20;
                }
            }

            if (keyBits.get(KeyEvent.VK_ESCAPE)) {
                pause = !pause;
            }

            if (isKeyPressed(KeyEvent.VK_SPACE) && (currentTime - lastFireTime >= fireDelay)) {
                bullets.add(new Bullet(hero.x, hero.y));
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
            entity.speed = 7;
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
//reversing direction of y axis
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
                }
                if (xPosition <= 65 && xPosition >= 35 && yPosition <= 50 && yPosition >= 45) {
                    page = "Game";
                    gameLevel = "medium";
                }
                if (xPosition <= 65 && xPosition >= 35 && yPosition <= 30 && yPosition >= 40) {
                    page = "Game";
                    gameLevel = "hard";
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
                        System.out.println("go to Home");
                    }
                }
                break;

            case "Lose" :
                if (xPosition >= 27 && xPosition <= 48 && yPosition >= 19 && yPosition <= 25) {
                    Default();
                    page = "Home";
                    System.out.println("Home");
                }
                if (xPosition >= 51 && xPosition <= 71 && yPosition >= 19 && yPosition <= 25) {
                    Default();
                    page = "Game";
                    System.out.println("Game");
                }
                break;
            case "Win" :
                if (xPosition >= 27 && xPosition <= 48 && yPosition >= 19 && yPosition <= 25) {
                    Default();
                    page = "Home";
                    System.out.println("Home");
                }
                if (xPosition >= 51 && xPosition <= 71 && yPosition >= 19 && yPosition <= 25) {
                    Default();
                    page = "Game";
                    System.out.println("medium");
                }
        }
    }

   public void Default() {
        lives = 3;
        score = 0;
        hero.x = 450;
        hero.y = 50;
        CreateEnemy();
        elapsedSeconds = 0;
        elapsedMinutes = 0;
        pause = false;
        counter = 0;
        bullets.clear();
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
}