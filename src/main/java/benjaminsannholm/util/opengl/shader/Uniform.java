package benjaminsannholm.util.opengl.shader;

import com.google.common.base.Preconditions;

import benjaminsannholm.util.opengl.GLAPI;

public abstract class Uniform<T>
{
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

    public T getValue()
    {
        return value;
    }

    public void set(T value)
    {
        Preconditions.checkNotNull(value, "value");

        if (!value.equals(this.value))
            isDirty = true;
            
        this.value = value;
    }

    public void update()
    {
        if (isDirty)
        {
            isDirty = false;
            upload();
        }
    }

    protected int getLocation()
    {
        return location != -2 ? location : (location = GLAPI.getUniformLocation(parent.getHandle(), name));
    }

    protected abstract void upload();
}