package benjaminsannholm.util.opengl.geometry;

import benjaminsannholm.util.opengl.GLAPI;
import benjaminsannholm.util.opengl.GraphicsObject;

public class VertexArrayObject extends GraphicsObject
{
    public VertexArrayObject()
    {
        create();
    }

    @Override
    protected void create()
    {
        setHandle(GLAPI.createVertexArray());
    }

    @Override
    protected void destroy()
    {
        GLAPI.deleteVertexArray(getHandle());
    }

    public void bind()
    {
        GLAPI.bindVertexArray(getHandle());
    }

    public static void unbind()
    {
        GLAPI.bindVertexArray(0);
    }
}