package benjaminsannholm.util.opengl.shader.uniforms;

import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.IntBuffer;
import java.util.Arrays;

import org.lwjgl.system.MemoryStack;

import benjaminsannholm.util.opengl.GLAPI;
import benjaminsannholm.util.opengl.shader.ShaderProgram;
import benjaminsannholm.util.opengl.shader.Uniform;

public class IntArrayUniform extends Uniform<int[]>
{
    public IntArrayUniform(ShaderProgram parent, String name)
    {
        super(parent, name);
    }

    @Override
    protected boolean equals(int[] value1, int[] value2)
    {
        return Arrays.equals(value1, value2);
    }
    
    @Override
    protected int[] copyValue(int[] value)
    {
        return Arrays.copyOf(value, value.length);
    }

    @Override
    protected void upload(int[] value)
    {
        try (MemoryStack stack = stackPush())
        {
            final IntBuffer buffer = stack.mallocInt(value.length);
            buffer.put(value);
            buffer.flip();
            GLAPI.setUniform1iv(getParent().getHandle(), getLocation(), buffer);
        }
    }
}