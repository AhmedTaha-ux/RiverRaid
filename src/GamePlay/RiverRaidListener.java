package GamePlay;

import com.sun.opengl.util.j2d.TextRenderer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;

public class RiverRaidListener extends AnimListener implements KeyListener, MouseMotionListener {

    GL gl;

    final private int maxWidth = 1000;
    final private int maxHeight = 700;

    int idx, timer, delayShowEnemy, counter, score, delayDestroy, lives;
    private long lastFireTime = 0;
    private final long fireDelay = 500;

    Entity hero = new Entity();

    Entity [] enemy = new Entity[5];

    BitSet keyBits = new BitSet(256);
    String[] textureNames = {"Plane", "Fire2", "Bullet","Ship","Helicopter","Fire",
            "Pause", "Score", "BG",
    };
    int[] enemiesIndex = {3, 4};
    TextureReader.Texture[] texture = new TextureReader.Texture[textureNames.length];
    int[] textures = new int[textureNames.length];

    TextRenderer ren = new TextRenderer(new Font("sanaSerif", Font.BOLD, 10));

    ArrayList<Bullet> bullets = new ArrayList<>();


    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        gl = glAutoDrawable.getGL();
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);    //This Will Clear The Background Color To Black

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
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        gl = glAutoDrawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();
        DrawBackground();
        DrawObject(hero.x, hero.y, 1.0, 0, hero.idx);
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

        DrawObject(50, 600, 0.9 , 0 , 7 );
        DrawObject(850, 600, 0.8 , 0 , 6 );
        ren.beginRendering(100, 100);
        ren.setColor(Color.WHITE);
        ren.draw(score+"" , 15, 90);
        ren.endRendering();
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {

    }

    @Override
    public void displayChanged(GLAutoDrawable glAutoDrawable, boolean b, boolean b1) {

    }

    public void Fire() {
        for (Bullet bullet : bullets) {
            if (bullet.isFired) {
                bullet.y += 4;
                // TODO: fix bullet scale so it looks better
                DrawObject(bullet.x, bullet.y + 35, 0.5, 0, 2);
            }
       }
    }

    public void DrawBackground() {
        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[textureNames.length - 1]);    // Turn Blending On
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

    public void DrawObject(int x, int y, double scale, double degree, int index) {
        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[index]);    // Turn Blending On

        gl.glPushMatrix();
        gl.glTranslated(x / (maxWidth / 2.0) - 0.9, y / (maxHeight / 2.0) - 0.9, 0);
        gl.glScaled(0.1 * scale, 0.1 * scale, 1);
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
        if (delayShowEnemy > 20) {
            if (counter < enemy.length) {
                counter++;
            }
            delayShowEnemy = 0;
        }

        for (int i = 0; i < counter; i++) {

            DrawObject(enemy[i].x, enemy[i].y -= enemy[i].speed, 1, 0, enemy[i].idx);
            if (enemy[i].y < -50) {
                enemy[i].y = 600;
                enemy[i].x = (int) (Math.random() * 500 + 200);
                enemy[i].idx = enemiesIndex[(int) (Math.random() * enemiesIndex.length)];
            }
        }

    }

    void DestroyEnemy() {
        for (int i = 0; i < bullets.size(); i++) {
            for (int j = 0; j < enemy.length; j++) {
                if (Math.abs(bullets.get(i).x - enemy[j].x) < 50 && Math.abs(bullets.get(i).y - enemy[j].y) < 50) {
                    System.out.println("destroy");
                    enemy[j].idx = 5;
                    bullets.get(i).y = 1000;
                    enemy[j].speed = 0;
                    score += enemy[j].score;
                    System.out.println("score :" + score);
                }
                if (enemy[j].idx == 5) {
                    delayDestroy++;
                    if (delayDestroy > 5) {
                        delayDestroy = 0;
                        enemy[j].idx = enemiesIndex[(int) (Math.random() * enemiesIndex.length)];
                        enemy[j].x = (int) (Math.random() * 500 + 200);
                        enemy[j].y = 600;
                        enemy[j].speed = 7;
                    }
                }
            }
        }
    }

    void Crash() {
        for (int j = 0; j < enemy.length; j++) {
            if (Math.abs(hero.x - enemy[j].x) < 75 && Math.abs(hero.y - enemy[j].y) < 50) {
                System.out.println("Crash");
                hero.idx = 1;
                enemy[j].idx = 5;
                enemy[j].speed = 0;

                if (enemy[j].idx == 5) {
                    delayDestroy++;
                    if (delayDestroy > 5) {
                        lives--;
                        System.out.println("lives :" + lives);
                        delayDestroy = 0;
                        enemy[j].idx = enemiesIndex[(int) (Math.random() * enemiesIndex.length)];
                        enemy[j].x = (int) (Math.random() * 500 + 200);
                        enemy[j].y = 600;
                        enemy[j].speed = 7;
                    }
                }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        // TODO: fix on double key pressed at the same time :)
        long currentTime = System.currentTimeMillis();

        keyBits.set(e.getKeyCode());
        if (hero.idx != 1) {
            if (keyBits.get(KeyEvent.VK_RIGHT)) {
                if (hero.x <= 730) {
                    hero.x += 10;
                } else {
                    hero.idx = 1;
                }
            }
            if (keyBits.get(KeyEvent.VK_LEFT)) {
                if (hero.x >= 175) {
                    hero.x -= 10;
                } else {
                    hero.idx = 1;
                }
            }
            if (keyBits.get(KeyEvent.VK_UP)) {
                if (hero.y < 600) {
                    hero.y += 10;
                }
            }
            if (keyBits.get(KeyEvent.VK_DOWN)) {
                if (hero.y > 0) {
                    hero.y -= 10;
                }
            }
            if (keyBits.get(KeyEvent.VK_SPACE) && (currentTime - lastFireTime >= fireDelay)) {
                bullets.add(new Bullet(hero.x, hero.y));
                lastFireTime = currentTime;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyBits.clear(e.getKeyCode());
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        System.out.println(e.getX() + " " + e.getY());
    }
}
