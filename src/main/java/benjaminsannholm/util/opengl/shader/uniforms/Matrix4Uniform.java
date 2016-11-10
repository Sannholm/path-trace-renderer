package benjaminsannholm.util.opengl.shader.uniforms;

import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.FloatBuffer;

import org.lwjgl.system.MemoryStack;

import benjaminsannholm.util.math.Matrix4;
import benjaminsannholm.util.opengl.GLAPI;
import benjaminsannholm.util.opengl.shader.ShaderProgram;
import benjaminsannholm.util.opengl.shader.Uniform;

public class Matrix4Uniform extends Uniform<Matrix4>
{
    public Matrix4Uniform(ShaderProgram parent, String name)
    {
        super(parent, name);
    }
    
    @Override
    protected void upload()
    {
        try (MemoryStack stack = stackPush())
        {
            final FloatBuffer buffer = stack.mallocFloat(16);
            getValue().writeTo(buffer);
            buffer.flip();
            GLAPI.setUniformMatrix4(getLocation(), buffer);
        }
    }
}