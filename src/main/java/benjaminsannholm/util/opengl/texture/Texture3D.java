package benjaminsannholm.util.opengl.texture;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.common.base.Preconditions;

import benjaminsannholm.util.opengl.GLAPI;

public class Texture3D extends Texture
{
    private final int width, height, depth;
    private final Wrap wrapS, wrapT, wrapU;
    
    private Texture3D(int width, int height, int depth, Format format, int levels, MinificationFilter minFilter, MagnificationFilter magFilter, Wrap wrapS, Wrap wrapT, Wrap wrapU)
    {
        super(Type._3D, format, levels, minFilter, magFilter);
        
        Preconditions.checkArgument(width > 0, "Width cannot be <= 0");
        Preconditions.checkArgument(height > 0, "Height cannot be <= 0");
        Preconditions.checkArgument(depth > 0, "Depth cannot be <= 0");
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.wrapS = Preconditions.checkNotNull(wrapS, "wrapS");
        this.wrapT = Preconditions.checkNotNull(wrapT, "wrapT");
        this.wrapU = Preconditions.checkNotNull(wrapU, "wrapU");
        
        create();
    }
    
    @Override
    protected void create()
    {
        super.create();
        GLAPI.initTextureImage(getHandle(), getType().getTarget(), getLevels(), getFormat().getInternalFormat(), getWidth(), getHeight(), getDepth());
        uploadParameters();
    }
    
    public static void unbind(int unit)
    {
        GLAPI.bindTexture(GL12.GL_TEXTURE_3D, unit, 0);
    }

    @Override
    protected void uploadParameters()
    {
        super.uploadParameters();
        GLAPI.setTextureParameteri(getHandle(), getType().getTarget(), GL11.GL_TEXTURE_WRAP_S, getWrapS().getEnum());
        GLAPI.setTextureParameteri(getHandle(), getType().getTarget(), GL11.GL_TEXTURE_WRAP_T, getWrapT().getEnum());
        GLAPI.setTextureParameteri(getHandle(), getType().getTarget(), GL12.GL_TEXTURE_WRAP_R, getWrapU().getEnum());
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

    public Wrap getWrapS()
    {
        return wrapS;
    }

    public Wrap getWrapT()
    {
        return wrapT;
    }

    public Wrap getWrapU()
    {
        return wrapU;
    }
    
    public static Builder builder(int width, int height, int depth)
    {
        return new Builder(width, height, depth);
    }
    
    public static class Builder extends Texture.Builder<Texture3D, Builder>
    {
        private final int width, height, depth;
        private Wrap wrapS = Wrap.REPEAT, wrapT = Wrap.REPEAT, wrapU = Wrap.REPEAT;
        
        private Builder(int width, int height, int depth)
        {
            this.width = width;
            this.height = height;
            this.depth = depth;
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

        public Builder wrapU(Wrap wrap)
        {
            wrapU = wrap;
            return this;
        }
        
        @Override
        public Texture3D build()
        {
            return new Texture3D(width, height, depth, format, levels, minFilter, magFilter, wrapS, wrapT, wrapU);
        }
    }
}