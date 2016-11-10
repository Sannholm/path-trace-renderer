package benjaminsannholm.util.opengl.shader;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class DummyShaderProgram extends ShaderProgram
{
    public DummyShaderProgram()
    {
    }

    @Override
    protected void create()
    {
    }

    @Override
    public void dispose()
    {
    }

    @Override
    public void getBinary(IntBuffer length, IntBuffer binaryFormat, ByteBuffer binary)
    {
    }

    @Override
    public void use()
    {
    }

    @Override
    public <T> void setUniform(String name, T value)
    {
    }

    @Override
    public void dispatchCompute(int numGroupsX, int numGroupsY, int numGroupsZ)
    {
    }
}