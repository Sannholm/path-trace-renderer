package benjaminsannholm.util.opengl.texture;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL12;

import com.google.common.base.Preconditions;

import benjaminsannholm.util.opengl.GLAPI;

public class Texture3D extends Texture
{
    private final int width, height, depth;

    private Texture3D(int width, int height, int depth, Format format, MinificationFilter minFilter, MagnificationFilter magFilter, Wrap wrapS, Wrap wrapT, Wrap wrapU)
    {
        super(Type._3D, format, minFilter, magFilter, wrapS, wrapT, wrapU);

        Preconditions.checkArgument(width > 0, "Width cannot be <= 0");
        Preconditions.checkArgument(height > 0, "Height cannot be <= 0");
        Preconditions.checkArgument(depth > 0, "Depth cannot be <= 0");
        this.width = width;
        this.height = height;
        this.depth = depth;

        create();
    }

    @Override
    protected void create()
    {
        super.create();
        bind(0);

        GLAPI.initTexImage(getType().getTarget(), 0, getFormat().getInternalFormat(), getWidth(), getHeight(), getDepth(), getFormat().getFormat(), getFormat().getType(), (ByteBuffer)null);
        uploadParameters();

        unbind(0);
    }

    public static void unbind(int unit)
    {
        GLAPI.bindTexture(GL12.GL_TEXTURE_3D, unit, 0);
    }
    
    @Override
    public void upload(ByteBuffer buffer, int format, int type)
    {
        upload(0, 0, 0, getWidth(), getHeight(), getDepth(), buffer, format, type);
    }
    
    @Override
    public void upload(int x, int y, int z, int width, int height, int depth, ByteBuffer buffer, int format, int type)
    {
        super.upload(x, y, z, width, height, depth, buffer, format, type);
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public int getDepth()
    {
        return depth;
    }

    public static Builder builder(int width, int height, int depth)
    {
        return new Builder(width, height, depth);
    }

    public static class Builder extends Texture.Builder<Texture3D, Builder>
    {
        private final int width, height, depth;

        private Builder(int width, int height, int depth)
        {
            this.width = width;
            this.height = height;
            this.depth = depth;
        }

        @Override
        public Texture3D build()
        {
            return new Texture3D(width, height, depth, format, minFilter, magFilter, wrapS, wrapT, wrapU);
        }
    }
}