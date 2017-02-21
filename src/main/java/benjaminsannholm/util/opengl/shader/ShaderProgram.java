package benjaminsannholm.util.opengl.shader;

import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL43;
import org.lwjgl.system.MemoryStack;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import benjaminsannholm.util.opengl.GLAPI;
import benjaminsannholm.util.opengl.GraphicsObject;
import gnu.trove.map.hash.THashMap;

public class ShaderProgram extends GraphicsObject
{
    private Set<Shader> shaders;
    private List<String> vertexInputs, fragmentOutputs;
    
    private final Map<String, Uniform> uniforms = new THashMap<>();
    
    private int workgroupSizeX = -1, workgroupSizeY = -1, workgroupSizeZ = -1;
    
    public ShaderProgram(Set<Shader> shaders, List<String> vertexInputs, List<String> fragmentOutputs)
    {
        this.shaders = ImmutableSet.copyOf(Preconditions.checkNotNull(shaders, "shaders"));
        Preconditions.checkArgument(!this.shaders.isEmpty(), "Shaders list cannot be empty");
        for (Shader shader : this.shaders)
            Preconditions.checkArgument(shader.getHandle() != -1, "Cannot attach disposed or erroring shader " + shader);
        
        this.vertexInputs = ImmutableList.copyOf(Preconditions.checkNotNull(vertexInputs, "vertexInputs"));
        this.fragmentOutputs = ImmutableList.copyOf(Preconditions.checkNotNull(fragmentOutputs, "fragmentOutputs"));
        
        create();
    }
    
    protected ShaderProgram()
    {
    }
    
    @Override
    protected void create()
    {
        setHandle(GLAPI.createProgram());
        
        for (Shader shader : shaders)
            GLAPI.attachShader(getHandle(), shader.getHandle());
        bindLocations();
        GLAPI.linkProgram(getHandle());
        
        if (GLAPI.getProgrami(getHandle(), GL20.GL_LINK_STATUS) == GL11.GL_FALSE)
        {
            final String log = GLAPI.getProgramInfoLog(getHandle()).trim();
            dispose();
            throw new ShaderProgramLinkException(log);
        }
        
        for (Shader shader : shaders)
            GLAPI.detachShader(getHandle(), shader.getHandle());
        shaders = null;
    }
    
    private void bindLocations()
    {
        for (int i = 0; i < vertexInputs.size(); i++)
            GLAPI.bindAttribLocation(getHandle(), vertexInputs.get(i), i);
        for (int i = 0; i < fragmentOutputs.size(); i++)
            GLAPI.bindFragDataLocation(getHandle(), fragmentOutputs.get(i), i);
        vertexInputs = fragmentOutputs = null;
    }
    
    @Override
    protected void destroy()
    {
        GLAPI.deleteProgram(getHandle());
    }
    
    public void getBinary(IntBuffer length, IntBuffer format, ByteBuffer binary)
    {
        GLAPI.getProgramBinary(getHandle(), length, format, binary);
    }
    
    private void fetchWorkgroupSize()
    {
        try (MemoryStack stack = stackPush())
        {
            final IntBuffer values = stack.mallocInt(3);
            GLAPI.getProgramiv(getHandle(), GL43.GL_COMPUTE_WORK_GROUP_SIZE, values);
            
            workgroupSizeX = values.get(0);
            workgroupSizeY = values.get(1);
            workgroupSizeZ = values.get(2);
        }
    }
    
    public int getWorkgroupSizeX()
    {
        if (workgroupSizeX == -1)
            fetchWorkgroupSize();
        return workgroupSizeX;
    }
    
    public int getWorkgroupSizeY()
    {
        if (workgroupSizeY == -1)
            fetchWorkgroupSize();
        return workgroupSizeY;
    }
    
    public int getWorkgroupSizeZ()
    {
        if (workgroupSizeZ == -1)
            fetchWorkgroupSize();
        return workgroupSizeZ;
    }
    
    public void use()
    {
        GLAPI.useProgram(getHandle());
    }
    
    public static void unuse()
    {
        GLAPI.useProgram(0);
    }
    
    public <T> void setUniform(String name, T value)
    {
        Preconditions.checkNotNull(name, "name");
        Preconditions.checkNotNull(value, "value");
        
        Uniform<T> uniform = uniforms.get(name);
        if (uniform == null)
        {
            final Class<? extends Uniform<?>> uniformType = Uniform.TYPES.get(value.getClass());
            Preconditions.checkArgument(uniformType != null, "No uniform type for " + value.getClass());
            
            try
            {
                uniform = (Uniform<T>)uniformType.getConstructor(ShaderProgram.class, String.class).newInstance(this, name);
                uniforms.put(name, uniform);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        
        uniform.set(value);
    }
    
    public void dispatchCompute(int numGroupsX, int numGroupsY, int numGroupsZ)
    {
        GLAPI.dispatchCompute(numGroupsX, numGroupsY, numGroupsZ);
    }
    
    public static class ShaderProgramLinkException extends RuntimeException
    {
        private static final long serialVersionUID = 92869432865905474L;
        
        public ShaderProgramLinkException(String message)
        {
            super(message);
        }
    }
}