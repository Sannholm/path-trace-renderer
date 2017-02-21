package benjaminsannholm.util.opengl.shader;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL43;

import com.google.common.base.Preconditions;

import benjaminsannholm.util.opengl.GLAPI;
import benjaminsannholm.util.opengl.GLAPIEnum;
import benjaminsannholm.util.opengl.GraphicsObject;

public class Shader extends GraphicsObject
{
    private final Type type;
    private final String source;
    
    public Shader(Type type, String source)
    {
        this.type = Preconditions.checkNotNull(type, "type");
        this.source = Preconditions.checkNotNull(source, "source");
        
        create();
    }
    
    @Override
    protected void create()
    {
        setHandle(GLAPI.createShader(type.getEnum()));
        
        GLAPI.setShaderSource(getHandle(), source);
        GLAPI.compileShader(getHandle());
        
        if (GLAPI.getShaderi(getHandle(), GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
        {
            final String log = GLAPI.getShaderInfoLog(getHandle()).trim();
            dispose();
            throw new ShaderCompilationException("Type: " + type + " Log: " + log);
        }
    }
    
    @Override
    protected void destroy()
    {
        GLAPI.deleteShader(getHandle());
    }
    
    public static enum Type implements GLAPIEnum
    {
        VERTEX(GL20.GL_VERTEX_SHADER),
        FRAGMENT(GL20.GL_FRAGMENT_SHADER),
        COMPUTE(GL43.GL_COMPUTE_SHADER);
        
        private final int glEnum;
        
        private Type(int glEnum)
        {
            this.glEnum = glEnum;
        }
        
        @Override
        public int getEnum()
        {
            return glEnum;
        }
    }
    
    public static class ShaderCompilationException extends RuntimeException
    {
        private static final long serialVersionUID = -7729196031230265536L;
        
        public ShaderCompilationException(String message)
        {
            super(message);
        }
    }
}