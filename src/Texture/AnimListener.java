package Texture;


import javax.media.opengl.GLAutoDrawable;

public abstract class AnimListener{
    protected String assetsFolderName = "Assets//";

    public abstract void init(GLAutoDrawable glAutoDrawable);

    public abstract void display(GLAutoDrawable glAutoDrawable);

    public abstract void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3);

    public abstract void displayChanged(GLAutoDrawable glAutoDrawable, boolean b, boolean b1);
}