package benjaminsannholm.util.opengl.texture;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;

import com.google.common.base.Preconditions;

import benjaminsannholm.util.opengl.GLAPI;
import benjaminsannholm.util.opengl.GLAPIEnum;
import benjaminsannholm.util.opengl.GraphicsObject;

public abstract class Texture extends GraphicsObject
{
    private final Type type;
    private final Format format;
    private final int levels;
    
    private MinificationFilter minFilter;
    private MagnificationFilter magFilter;
    
    protected Texture(Type type, Format format, int levels, MinificationFilter minFilter, MagnificationFilter magFilter)
    {
        this.type = Preconditions.checkNotNull(type, "type");
        this.format = Preconditions.checkNotNull(format, "format");
        Preconditions.checkArgument(levels > 0, "Levels cannot be <= 0");
        this.levels = levels;
        this.minFilter = Preconditions.checkNotNull(minFilter, "minFilter");
        this.magFilter = Preconditions.checkNotNull(magFilter, "magFilter");
    }
    
    @Override
    protected void create()
    {
        setHandle(GLAPI.createTexture(type.getTarget()));
    }
    
    @Override
    protected void destroy()
    {
        GLAPI.deleteTexture(getHandle());
    }
    
    public void bind(int unit)
    {
        GLAPI.bindTexture(getType().getTarget(), unit, getHandle());
    }
    
    public void bindImage(int unit, Access access, Format format)
    {
        GLAPI.bindImageTexture(unit, getHandle(), access.getEnum(), format.getInternalFormat());
    }
    
    protected void uploadParameters()
    {
        GLAPI.setTextureParameteri(getHandle(), getType().getTarget(), GL11.GL_TEXTURE_MIN_FILTER, getMinFilter().getEnum());
        GLAPI.setTextureParameteri(getHandle(), getType().getTarget(), GL11.GL_TEXTURE_MAG_FILTER, getMagFilter().getEnum());
        GLAPI.setTextureParameteri(getHandle(), getType().getTarget(), GL14.GL_TEXTURE_COMPARE_MODE, GL11.GL_NONE);
    }
    
    public abstract void upload(ByteBuffer buffer, int format, int type);
    
    protected void upload(int x, int y, int z, int width, int height, int depth, ByteBuffer buffer, int format, int type)
    {
        GLAPI.uploadTextureImage(getHandle(), getType().getTarget(), 0, x, y, z, width, height, depth, format, type, buffer);
    }
    
    public void generateMipmaps()
    {
        GLAPI.generateTextureMipmaps(getHandle(), getType().getTarget());
    }
    
    public Type getType()
    {
        return type;
    }
    
    public Format getFormat()
    {
        return format;
    }
    
    public int getLevels()
    {
        return levels;
    }
    
    public MinificationFilter getMinFilter()
    {
        return minFilter;
    }
    
    public MagnificationFilter getMagFilter()
    {
        return magFilter;
    }
    
    public static abstract class Builder<T extends Texture, B extends Builder<T, B>>
    {
        protected Format format = Format.RGBA8;
        protected int levels = 1;
        
        protected MinificationFilter minFilter = MinificationFilter.LINEAR;
        protected MagnificationFilter magFilter = MagnificationFilter.LINEAR;
        
        public B format(Format format)
        {
            this.format = format;
            return (B)this;
        }
        
        public B levels(int levels)
        {
            this.levels = levels;
            return (B)this;
        }
        
        public B minFilter(MinificationFilter filter)
        {
            minFilter = filter;
            return (B)this;
        }
        
        public B magFilter(MagnificationFilter filter)
        {
            magFilter = filter;
            return (B)this;
        }
        
        public abstract T build();
    }
    
    public static enum Type
    {
        _1D(GL11.GL_TEXTURE_1D), _2D(GL11.GL_TEXTURE_2D), _3D(GL12.GL_TEXTURE_3D);
        
        private final int target;
        
        private Type(int target)
        {
            this.target = target;
        }
        
        public int getTarget()
        {
            return target;
        }
    }
    
    public static enum Format
    {
        RGBA8(GL11.GL_RGBA8, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE),
        RGBA32F(GL30.GL_RGBA32F, GL11.GL_RGBA, GL11.GL_FLOAT),
        SRGB8_ALPHA8(GL21.GL_SRGB8_ALPHA8, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE),
        RGB32F(GL30.GL_RGB32F, GL11.GL_RGB, GL11.GL_FLOAT),
        RGB16F(GL30.GL_RGB16F, GL11.GL_RGB, GL30.GL_HALF_FLOAT),
        RG16(GL30.GL_RG16, GL30.GL_RG, GL11.GL_UNSIGNED_SHORT),
        RG8(GL30.GL_RG8, GL30.GL_RG, GL11.GL_UNSIGNED_BYTE),
        R8(GL30.GL_R8, GL11.GL_RED, GL11.GL_UNSIGNED_BYTE),
        DEPTH24(GL14.GL_DEPTH_COMPONENT24, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT);
        
        private final int internalFormat;
        private final int format;
        private final int type;
        
        private Format(int internalFormat, int format, int type)
        {
            this.internalFormat = internalFormat;
            this.format = format;
            this.type = type;
        }
        
        public int getInternalFormat()
        {
            return internalFormat;
        }
        
        public int getFormat()
        {
            return format;
        }
        
        public int getType()
        {
            return type;
        }
    }
    
    public static enum MagnificationFilter implements GLAPIEnum
    {
        NEAREST(GL11.GL_NEAREST),
        LINEAR(GL11.GL_LINEAR);
        
        private final int glEnum;
        
        private MagnificationFilter(int glEnum)
        {
            this.glEnum = glEnum;
        }
        
        @Override
        public int getEnum()
        {
            return glEnum;
        }
    }
    
    public static enum MinificationFilter implements GLAPIEnum
    {
        NEAREST(GL11.GL_NEAREST),
        LINEAR(GL11.GL_LINEAR),
        NEAREST_MIPMAP(GL11.GL_NEAREST_MIPMAP_LINEAR),
        LINEAR_MIPMAP(GL11.GL_LINEAR_MIPMAP_LINEAR);
        
        private final int glEnum;
        
        private MinificationFilter(int glEnum)
        {
            this.glEnum = glEnum;
        }
        
        @Override
        public int getEnum()
        {
            return glEnum;
        }
    }
    
    public static enum Wrap implements GLAPIEnum
    {
        REPEAT(GL11.GL_REPEAT),
        CLAMP(GL12.GL_CLAMP_TO_EDGE);
        
        private final int glEnum;
        
        private Wrap(int glEnum)
        {
            this.glEnum = glEnum;
        }
        
        @Override
        public int getEnum()
        {
            return glEnum;
        }
    }
    
    public static enum Access implements GLAPIEnum
    {
        READ(GL15.GL_READ_ONLY),
        WRITE(GL15.GL_WRITE_ONLY),
        READ_WRITE(GL15.GL_READ_WRITE);
        
        private final int glEnum;
        
        private Access(int glEnum)
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