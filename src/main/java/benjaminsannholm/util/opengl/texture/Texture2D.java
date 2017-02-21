package benjaminsannholm.util.opengl.texture;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Preconditions;

import benjaminsannholm.util.opengl.GLAPI;

public class Texture2D extends Texture
{
    private final int width, height;
    private Wrap wrapS, wrapT;
    
    private Texture2D(int width, int height, Format format, int levels, MinificationFilter minFilter, MagnificationFilter magFilter, Wrap wrapS, Wrap wrapT)
    {
        super(Type._2D, format, levels, minFilter, magFilter);
        
        Preconditions.checkArgument(width > 0, "Width cannot be <= 0");
        Preconditions.checkArgument(height > 0, "Height cannot be <= 0");
        this.width = width;
        this.height = height;
        this.wrapS = Preconditions.checkNotNull(wrapS, "wrapS");
        this.wrapT = Preconditions.checkNotNull(wrapT, "wrapT");
        
        create();
    }
    
    @Override
    protected void create()
    {
        super.create();
        GLAPI.initTextureImage(getHandle(), getType().getTarget(), getLevels(), getFormat().getInternalFormat(), getWidth(), getHeight(), 0);
        uploadParameters();
    }
    
    public static void unbind(int unit)
    {
        GLAPI.bindTexture(GL11.GL_TEXTURE_2D, unit, 0);
    }
    
    @Override
    protected void uploadParameters()
    {
        super.uploadParameters();
        GLAPI.setTextureParameteri(getHandle(), getType().getTarget(), GL11.GL_TEXTURE_WRAP_S, getWrapS().getEnum());
        GLAPI.setTextureParameteri(getHandle(), getType().getTarget(), GL11.GL_TEXTURE_WRAP_T, getWrapT().getEnum());
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
    
    public Wrap getWrapS()
    {
        return wrapS;
    }
    
    public Wrap getWrapT()
    {
        return wrapT;
    }
    
    public static Builder builder(int width, int height)
    {
        return new Builder(width, height);
    }
    
    public static class Builder extends Texture.Builder<Texture2D, Builder>
    {
        private final int width, height;
        private Wrap wrapS = Wrap.REPEAT, wrapT = Wrap.REPEAT;
        
        private Builder(int width, int height)
        {
            this.width = width;
            this.height = height;
        }
        
        public Builder wrapS(Wrap wrap)
        {
            wrapS = wrap;
            return this;
        }
        
        public Builder wrapT(Wrap wrap)
        {
            wrapT = wrap;
            return this;
        }
        
        @Override
        public Texture2D build()
        {
            return new Texture2D(width, height, format, levels, minFilter, magFilter, wrapS, wrapT);
        }
    }
}