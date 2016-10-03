package benjaminsannholm.util.math;

import java.util.Random;

public abstract class Vector<T extends Vector<T>>
{
    private float lengthSquared = -1, length = -1;
    
    public abstract boolean isZero();
    
    protected abstract T getZero();
    
    public float length()
    {
        return length != -1 ? length : (length = (float)Math.sqrt(lengthSquared()));
    }
    
    public float lengthSquared()
    {
        return lengthSquared != -1 ? lengthSquared : (lengthSquared = distanceSquared(getZero()));
    }
    
    public float distance(T to)
    {
        return (float)Math.sqrt(distanceSquared(to));
    }
    
    public abstract float distanceSquared(T to);
    
    public abstract T add(T vector);
    
    public abstract T subtract(T vector);
    
    public abstract T multiply(float factor);
    
    public abstract T multiply(T vector);
    
    public abstract T divide(float divisor);
    
    public abstract T divide(T vector);
    
    public T normalize()
    {
        if (isZero() || lengthSquared() == 1)
            return (T)this;
        return divide(length());
    }
    
    public T negate()
    {
        return multiply(-1);
    }

    public abstract T abs();
    
    public abstract float dot(T vector);
    
    public abstract T lerp(T end, float factor);

    public abstract T random(T end, Random rand);

    public abstract T min(T vector);

    public abstract T max(T vector);
}