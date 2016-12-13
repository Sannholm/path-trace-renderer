package benjaminsannholm.util.opengl.geometry;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import com.google.common.base.Preconditions;

import benjaminsannholm.util.opengl.GLAPI;
import benjaminsannholm.util.opengl.GLAPIEnum;
import benjaminsannholm.util.opengl.GraphicsObject;

public class VertexBufferObject extends GraphicsObject
{
    private static final int TARGET = GL15.GL_ARRAY_BUFFER;

    private final int size;
    private final Usage usage;

    public VertexBufferObject(int size, Usage usage)
    {
        Preconditions.checkArgument(size > 0, "Size cannot be <= 0");
        this.size = size;
        this.usage = Preconditions.checkNotNull(usage, "usage");

        create();
    }

    @Override
    protected void create()
    {
        setHandle(GLAPI.createBuffer());
        GLAPI.initBufferData(getHandle(), TARGET, getSize(), usage.getEnum());
    }

    @Override
    public void dispose()
    {
        if (getHandle() != -1)
        {
            GLAPI.deleteBuffer(getHandle());
            setHandle(-1);
        }
    }

    public void bind()
    {
        GLAPI.bindBuffer(TARGET, getHandle());
    }

    public static void unbind()
    {
        GLAPI.bindBuffer(TARGET, 0);
    }

    public ByteBuffer map(int offset, int length, int flags)
    {
        return GLAPI.mapBuffer(getHandle(), TARGET, flags, offset, length);
    }

    public ByteBuffer map(int flags)
    {
        return map(0, getSize(), flags);
    }

    public ByteBuffer map()
    {
        return map(GL30.GL_MAP_WRITE_BIT | GL30.GL_MAP_INVALIDATE_BUFFER_BIT | GL30.GL_MAP_INVALIDATE_RANGE_BIT | GL30.GL_MAP_UNSYNCHRONIZED_BIT);
    }

    public void unmap()
    {
        GLAPI.unmapBuffer(getHandle(), TARGET);
    }

    public int getSize()
    {
        return size;
    }

    public static enum Usage implements GLAPIEnum
    {
        STATIC_DRAW(GL15.GL_STATIC_DRAW),
        DYNAMIC_DRAW(GL15.GL_DYNAMIC_DRAW),
        STREAM_DRAW(GL15.GL_STREAM_DRAW);

        private final int glEnum;

        private Usage(int glEnum)
        {
            this.glEnum = glEnum;
        }

        @Override
        public int getEnum()
        {
            return glEnum;
        }
    }
}