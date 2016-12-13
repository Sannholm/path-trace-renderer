package benjaminsannholm.util.opengl.shader;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import benjaminsannholm.util.math.Matrix4;
import benjaminsannholm.util.math.Vector2;
import benjaminsannholm.util.math.Vector3;
import benjaminsannholm.util.math.Vector4;
import benjaminsannholm.util.opengl.GLAPI;
import benjaminsannholm.util.opengl.shader.uniforms.FloatUniform;
import benjaminsannholm.util.opengl.shader.uniforms.IntArrayUniform;
import benjaminsannholm.util.opengl.shader.uniforms.IntegerUniform;
import benjaminsannholm.util.opengl.shader.uniforms.Matrix4Uniform;
import benjaminsannholm.util.opengl.shader.uniforms.Vector2Uniform;
import benjaminsannholm.util.opengl.shader.uniforms.Vector3Uniform;
import benjaminsannholm.util.opengl.shader.uniforms.Vector4Uniform;

public abstract class Uniform<T>
{
    public static final Map<Class<?>, Class<? extends Uniform<?>>> TYPES = ImmutableMap.<Class<?>, Class<? extends Uniform<?>>>builder()
            .put(Integer.class, IntegerUniform.class)
            .put(Float.class, FloatUniform.class)
            .put(Vector2.class, Vector2Uniform.class)
            .put(Vector3.class, Vector3Uniform.class)
            .put(Vector4.class, Vector4Uniform.class)
            .put(Matrix4.class, Matrix4Uniform.class)
            .put(int[].class, IntArrayUniform.class)
            .build();
    
    private final ShaderProgram parent;
    private final String name;

    private int location = -2; // -1 cannot be used since getUniformLocation might return it
    private T value;
    private boolean isDirty = true;

    public Uniform(ShaderProgram parent, String name)
    {
        this.parent = Preconditions.checkNotNull(parent, "parent");
        this.name = Preconditions.checkNotNull(name, "name");
    }
    
    protected ShaderProgram getParent()
    {
        return parent;
    }

    protected int getLocation()
    {
        return location != -2 ? location : (location = GLAPI.getUniformLocation(parent.getHandle(), name));
    }

    protected boolean equals(T value1, T value2)
    {
        return value1.equals(value2);
    }

    protected T copyValue(T value)
    {
        return value;
    }

    public void set(T value)
    {
        Preconditions.checkNotNull(value, "value");

        if (!equals(value, this.value))
        {
            isDirty = true;
            this.value = copyValue(value);
        }
    }
    
    public void update()
    {
        if (isDirty)
        {
            isDirty = false;
            upload(value);
        }
    }

    protected abstract void upload(T value);
}