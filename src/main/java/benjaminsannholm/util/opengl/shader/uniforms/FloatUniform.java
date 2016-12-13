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
    protected void upload(Float value)
    {
        GLAPI.setUniform1f(getParent().getHandle(), getLocation(), value);
    }
}