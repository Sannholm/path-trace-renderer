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
    protected boolean equalsValue(int[] value)
    {
        return Arrays.equals(value, getValue());
    }

    @Override
    protected int[] copyValue(int[] value)
    {
        return Arrays.copyOf(value, value.length);
    }
    
    @Override
    protected void upload()
    {
        try (MemoryStack stack = stackPush())
        {
            final IntBuffer buffer = stack.mallocInt(getValue().length);
            buffer.put(getValue());
            buffer.flip();
            GLAPI.setUniform1iv(getLocation(), buffer);
        }
    }
}