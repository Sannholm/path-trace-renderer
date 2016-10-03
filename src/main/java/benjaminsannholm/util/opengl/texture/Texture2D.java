package benjaminsannholm.util.opengl.texture;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Preconditions;

import benjaminsannholm.util.opengl.GLAPI;

public class Texture2D extends Texture
{
    private final int width, height;

    private Texture2D(int width, int height, Format format, MinificationFilter minFilter, MagnificationFilter magFilter, Wrap wrapS, Wrap wrapT)
    {
        super(Type._2D, format, minFilter, magFilter, wrapS, wrapT, Wrap.REPEAT);

        Preconditions.checkArgument(width > 0, "Width cannot be <= 0");
        Preconditions.checkArgument(height > 0, "Height cannot be <= 0");
        this.width = width;
        this.height = height;

        create();
    }

    @Override
    protected void create()
    {
        super.create();
        bind(0);

        GLAPI.initTexImage(getType().getTarget(), 0, getFormat().getInternalFormat(), getWidth(), getHeight(), 0, getFormat().getFormat(), getFormat().getType(), (ByteBuffer)null);
        uploadParameters();

        unbind(0);
    }

    public static void unbind(int unit)
    {
        GLAPI.bindTexture(GL11.GL_TEXTURE_2D, unit, 0);
    }

    @Override
    public void upload(ByteBuffer buffer, int format, int type)
    {
        upload(0, 0, getWidth(), getHeight(), buffer, format, type);
    }

    public void upload(int x, int y, int width, int height, ByteBuffer buffer, int format, int type)
    {
        super.upload(x, y, 0, width, height, 0, buffer, format, type);
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public static Builder builder(int width, int height)
    {
        return new Builder(width, height);
    }

    public static class Builder extends Texture.Builder<Texture2D, Builder>
    {
        private final int width, height;

        private Builder(int width, int height)
        {
            this.width = width;
            this.height = height;
        }

        @Override
        public Texture2D build()
        {
            return new Texture2D(width, height, format, minFilter, magFilter, wrapS, wrapT);
        }
    }
}