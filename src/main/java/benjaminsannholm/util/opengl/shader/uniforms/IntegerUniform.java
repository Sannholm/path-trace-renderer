package benjaminsannholm.util.opengl.shader.uniforms;

import static org.lwjgl.system.MemoryStack.stackPush;

import org.lwjgl.system.MemoryStack;

import benjaminsannholm.util.opengl.GLAPI;
import benjaminsannholm.util.opengl.shader.ShaderProgram;
import benjaminsannholm.util.opengl.shader.Uniform;

public class IntegerUniform extends Uniform<Integer>
{
    public IntegerUniform(ShaderProgram parent, String name)
    {
        super(parent, name);
    }
    
    @Override
    protected void upload()
    {
        try (MemoryStack stack = stackPush())
        {
            GLAPI.setUniform1i(getLocation(), stack.ints(getValue()));
        }
    }
}