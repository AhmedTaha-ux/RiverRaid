package Texture;

import javax.media.opengl.GLEventListener;
import java.awt.event.MouseEvent;


public abstract class AnimListener implements GLEventListener{
 
    protected String assetsFolderName = "Assets//";

    public abstract void mouseClicked(MouseEvent e);

    public abstract void mousePressed(MouseEvent e);

    public abstract void mouseReleased(MouseEvent e);

    public abstract void mouseExited(MouseEvent e);

    public abstract void mouseEntered(MouseEvent e);
}