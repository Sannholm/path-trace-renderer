package benjaminsannholm.util.opengl.geometry;

import org.lwjgl.opengl.GL11;

import benjaminsannholm.util.opengl.GLAPIEnum;

public enum RenderMode implements GLAPIEnum
{
    TRIANGLES(GL11.GL_TRIANGLES),
    TRIANGLE_STRIP(GL11.GL_TRIANGLE_STRIP),
    POINTS(GL11.GL_POINTS);

    private final int glEnum;

    private RenderMode(int glEnum)
    {
        this.glEnum = glEnum;
    }

    @Override
    public int getEnum()
    {
        return glEnum;
    }
}