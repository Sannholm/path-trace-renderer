package benjaminsannholm.util.opengl.shader.uniforms;

import benjaminsannholm.util.math.Vector3;
import benjaminsannholm.util.opengl.GLAPI;
import benjaminsannholm.util.opengl.shader.ShaderProgram;
import benjaminsannholm.util.opengl.shader.Uniform;

public class Vector3Uniform extends Uniform<Vector3>
{
    public Vector3Uniform(ShaderProgram parent, String name)
    {
        super(parent, name);
    }

    @Override
    protected void upload()
    {
        final Vector3 vec = getValue();
        GLAPI.setUniform3f(getLocation(), vec.getX(), vec.getY(), vec.getZ());
    }
}