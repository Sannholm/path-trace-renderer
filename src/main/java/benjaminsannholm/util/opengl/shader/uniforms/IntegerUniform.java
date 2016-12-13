package benjaminsannholm.util.opengl.shader.uniforms;

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
    protected void upload(Integer value)
    {
        GLAPI.setUniform1i(getParent().getHandle(), getLocation(), value);
    }
}