package benjaminsannholm.util.opengl.shader.uniforms;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import benjaminsannholm.util.math.Matrix4;
import benjaminsannholm.util.opengl.GLAPI;
import benjaminsannholm.util.opengl.shader.ShaderProgram;
import benjaminsannholm.util.opengl.shader.Uniform;

public class Matrix4Uniform extends Uniform<Matrix4>
{
    private static FloatBuffer buffer;

    private static FloatBuffer buffer()
    {
        return buffer != null ? buffer : (buffer = BufferUtils.createFloatBuffer(16));
    }

    public Matrix4Uniform(ShaderProgram parent, String name)
    {
        super(parent, name);
    }

    @Override
    protected void upload()
    {
        final FloatBuffer buffer = buffer();
        buffer.clear();
        getValue().writeTo(buffer);
        buffer.flip();
        GLAPI.setUniformMatrix4(getLocation(), buffer);
    }
}