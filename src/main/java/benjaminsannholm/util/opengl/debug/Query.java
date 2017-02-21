package benjaminsannholm.util.opengl.debug;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;

import com.google.common.base.Preconditions;

import benjaminsannholm.util.opengl.GLAPI;
import benjaminsannholm.util.opengl.GLAPIEnum;
import benjaminsannholm.util.opengl.GraphicsObject;

public class Query extends GraphicsObject
{
    private final Type type;
    
    public Query(Type type)
    {
        this.type = Preconditions.checkNotNull(type, "type");
        
        create();
    }
    
    @Override
    protected void create()
    {
        setHandle(GLAPI.createQuery(type.getEnum()));
    }
    
    @Override
    protected void destroy()
    {
        GLAPI.deleteQuery(getHandle());
    }
    
    public void begin()
    {
        GLAPI.beginQuery(type.getEnum(), getHandle());
    }
    
    public void end()
    {
        GLAPI.endQuery(type.getEnum());
    }
    
    public boolean isAvailable()
    {
        return GLAPI.getQueryObjectui64(getHandle(), GL15.GL_QUERY_RESULT_AVAILABLE) == GL11.GL_TRUE;
    }
    
    public long getResult()
    {
        return GLAPI.getQueryObjectui64(getHandle(), GL15.GL_QUERY_RESULT);
    }
    
    public static enum Type implements GLAPIEnum
    {
        TIME_ELAPSED(GL33.GL_TIME_ELAPSED);
        
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
}