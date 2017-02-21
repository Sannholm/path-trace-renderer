package benjaminsannholm.util.opengl.texture;

import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import benjaminsannholm.util.opengl.GLAPI;
import benjaminsannholm.util.opengl.GraphicsObject;

public class FrameBuffer extends GraphicsObject
{
    private final List<Texture2D> colorAttachments;
    private final Texture2D depthTexture;
    
    private FrameBuffer(List<Texture2D> colorAttachments, Texture2D depthTexture)
    {
        Preconditions.checkNotNull(colorAttachments, "colorAttachments");
        Preconditions.checkArgument(!colorAttachments.isEmpty(), "Framebuffer needs at least one color attachment");
        this.colorAttachments = ImmutableList.copyOf(colorAttachments);
        this.depthTexture = depthTexture;
        
        create();
    }
    
    @Override
    public void create()
    {
        setHandle(GLAPI.createFramebuffer());
        
        try (MemoryStack stack = stackPush())
        {
            final IntBuffer drawBuffers = stack.mallocInt(colorAttachments.size());
            for (int i = 0; i < colorAttachments.size(); i++)
            {
                final int bufferId = GL30.GL_COLOR_ATTACHMENT0 + i;
                final Texture texture = colorAttachments.get(i);
                GLAPI.setFramebufferAttachment(getHandle(), bufferId, texture.getHandle());
                drawBuffers.put(bufferId);
            }
            drawBuffers.rewind();
            GLAPI.setDrawBuffers(getHandle(), drawBuffers);
        }
        
        if (depthTexture != null)
            GLAPI.setFramebufferAttachment(getHandle(), GL30.GL_DEPTH_ATTACHMENT, depthTexture.getHandle());
        
        checkComplete();
    }
    
    private void checkComplete()
    {
        final int status = GLAPI.checkFramebufferStatus(getHandle());
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE)
        {
            dispose();
            
            switch (status)
            {
                case GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                    throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
                case GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                    throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
                case GL30.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                    throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
                case GL30.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
                    throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
                default:
                    throw new RuntimeException("Unknown framebuffer status: " + status);
            }
        }
    }
    
    @Override
    protected void destroy()
    {
        GLAPI.deleteFramebuffer(getHandle());
    }
    
    public void bind()
    {
        GLAPI.bindFramebuffer(getHandle());
    }
    
    public static void unBind()
    {
        GLAPI.bindFramebuffer(0);
    }
    
    public int getWidth()
    {
        return colorAttachments.get(0).getWidth();
    }
    
    public int getHeight()
    {
        return colorAttachments.get(0).getHeight();
    }
    
    public static Builder builder()
    {
        return new Builder();
    }
    
    public static class Builder
    {
        private final List<Texture2D> colorAttachments = new ArrayList<>();
        private Texture2D depthTexture;
        
        private Builder()
        {
        }
        
        public Builder attach(Texture2D texture)
        {
            colorAttachments.add(Preconditions.checkNotNull(texture, "texture"));
            return this;
        }
        
        public Builder depth(Texture2D texture)
        {
            depthTexture = texture;
            return this;
        }
        
        public FrameBuffer build()
        {
            return new FrameBuffer(colorAttachments, depthTexture);
        }
    }
}