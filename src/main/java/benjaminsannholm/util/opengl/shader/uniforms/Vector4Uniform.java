package benjaminsannholm.util.opengl.shader.uniforms;

import benjaminsannholm.util.math.Vector4;
import benjaminsannholm.util.opengl.GLAPI;
import benjaminsannholm.util.opengl.shader.ShaderProgram;
import benjaminsannholm.util.opengl.shader.Uniform;

public class Vector4Uniform extends Uniform<Vector4>
{
    public Vector4Uniform(ShaderProgram parent, String name)
    {
        super(parent, name);
    }
    
    @Override
    protected void upload(Vector4 value)
    {
        GLAPI.setUniform4f(getParent().getHandle(), getLocation(), value.getX(), value.getY(), value.getZ(), value.getW());
    }
}