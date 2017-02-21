package benjaminsannholm.util.opengl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import benjaminsannholm.util.Disposable;

public abstract class GraphicsObject implements Disposable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphicsObject.class);
    
    private int handle = -1;
    
    public int getHandle()
    {
        return handle;
    }
    
    protected void setHandle(int handle)
    {
        this.handle = handle;
    }
    
    protected abstract void create();
    
    protected abstract void destroy();
    
    @Override
    public void dispose()
    {
        if (getHandle() != -1)
        {
            destroy();
            setHandle(-1);
        }
    }
    
    @Override
    protected void finalize() throws Throwable
    {
        if (getHandle() != -1)
            LOGGER.error("MEMORY LEAK! GraphicsObject " + getClass().getSimpleName() + " was GC'd but the native equivalent was not disposed!");
        super.finalize();
    }
}