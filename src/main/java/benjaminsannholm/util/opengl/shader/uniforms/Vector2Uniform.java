package benjaminsannholm.util.opengl.shader.uniforms;

import benjaminsannholm.util.math.Vector2;
import benjaminsannholm.util.opengl.GLAPI;
import benjaminsannholm.util.opengl.shader.ShaderProgram;
import benjaminsannholm.util.opengl.shader.Uniform;

public class Vector2Uniform extends Uniform<Vector2>
{
    public Vector2Uniform(ShaderProgram parent, String name)
    {
        super(parent, name);
    }

    @Override
    protected void upload()
    {
        final Vector2 vec = getValue();
        GLAPI.setUniform2f(getLocation(), vec.getX(), vec.getY());
    }
}