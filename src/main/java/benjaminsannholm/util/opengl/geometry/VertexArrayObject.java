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
        setHandle(GLAPI.genVertexArray());
    }

    @Override
    public void dispose()
    {
        if (getHandle() != -1)
        {
            GLAPI.deleteVertexArray(getHandle());
            setHandle(-1);
        }
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