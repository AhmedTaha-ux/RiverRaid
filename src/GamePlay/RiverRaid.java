package GamePlay;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.FPSAnimator;

import javax.media.opengl.GLCanvas;
import javax.swing.*;
import java.awt.*;

public class RiverRaid extends JFrame {
    private Animator animator;
    private GLCanvas glcanvas;
    private RiverRaidListener listener = new RiverRaidListener();

    public static void main(String[] args) {
        new RiverRaid();
    }

    public RiverRaid() {
        glcanvas = new GLCanvas();
        glcanvas.addGLEventListener(listener);
        glcanvas.addKeyListener(listener);
        glcanvas.addMouseMotionListener(listener);
        add(glcanvas, BorderLayout.CENTER);
        animator = new FPSAnimator(10);
        animator.add(glcanvas);
        animator.start();

        setTitle("River Raid Application");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setVisible(true);
        setFocusable(true);
        glcanvas.requestFocus();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
