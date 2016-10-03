package benjaminsannholm.util.opengl.shader.uniforms;

import benjaminsannholm.util.opengl.GLAPI;
import benjaminsannholm.util.opengl.shader.ShaderProgram;
import benjaminsannholm.util.opengl.shader.Uniform;

public class FloatUniform extends Uniform<Float>
{
    public FloatUniform(ShaderProgram parent, String name)
    {
        super(parent, name);
    }

    @Override
    protected void upload()
    {
        GLAPI.setUniform1f(getLocation(), getValue());
    }
}