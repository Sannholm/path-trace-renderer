package benjaminsannholm.util.math;

import java.util.Objects;
import java.util.Random;

import com.google.common.base.MoreObjects;

public class Vector2 extends Vector<Vector2>
{
    public static final Vector2 ZERO = create(0, 0);
    public static final Vector2 ONE = create(1, 1);
    public static final Vector2 X_AXIS = create(1, 0);
    public static final Vector2 Y_AXIS = create(0, 1);
    
    private final float x, y;
    
    private Vector2(float x, float y)
    {
        this.x = x;
        this.y = y;
    }
    
    public float getX()
    {
        return x;
    }
    
    public float getY()
    {
        return y;
    }
    
    @Override
    public boolean isZero()
    {
        return getX() == 0 && getY() == 0;
    }
    
    @Override
    protected Vector2 getZero()
    {
        return ZERO;
    }
    
    // DISTANCE / LENGTH
    
    @Override
    public float distanceSquared(Vector2 to)
    {
        final Vector2 delta = subtract(to);
        return delta.getX() * delta.getX()
                + delta.getY() * delta.getY();
    }
    
    // SET
    
    public Vector2 setX(float x)
    {
        if (x == getX())
            return this;
        return create(x, getY());
    }
    
    public Vector2 setY(float y)
    {
        if (y == getY())
            return this;
        return create(getX(), y);
    }
    
    // ADD
    
    public Vector2 add(float x, float y)
    {
        if (x == 0 && y == 0)
            return this;
        return create(getX() + x, getY() + y);
    }
    
    @Override
    public Vector2 add(Vector2 vector)
    {
        return add(vector.getX(), vector.getY());
    }
    
    // SUBTRACT
    
    public Vector2 subtract(float x, float y)
    {
        if (x == 0 && y == 0)
            return this;
        return create(getX() - x, getY() - y);
    }
    
    @Override
    public Vector2 subtract(Vector2 vector)
    {
        return subtract(vector.getX(), vector.getY());
    }
    
    // MULTIPLY
    
    public Vector2 multiply(float x, float y)
    {
        if (x == 1 && y == 1)
            return this;
        return create(getX() * x, getY() * y);
    }
    
    @Override
    public Vector2 multiply(Vector2 vector)
    {
        return multiply(vector.getX(), vector.getY());
    }
    
    @Override
    public Vector2 multiply(float factor)
    {
        return multiply(factor, factor);
    }
    
    // DIVIDE
    
    public Vector2 divide(float x, float y)
    {
        if (x == 1 && y == 1)
            return this;
        return create(getX() / x, getY() / y);
    }
    
    @Override
    public Vector2 divide(Vector2 vector)
    {
        return divide(vector.getX(), vector.getY());
    }
    
    @Override
    public Vector2 divide(float divisor)
    {
        return divide(divisor, divisor);
    }
    
    // MISC
    
    public Vector2 rotate(float degrees)
    {
        if (degrees == 0 || isZero())
            return this;
        final float cos = FastMath.cosDeg(degrees);
        final float sin = FastMath.sinDeg(degrees);
        return create(cos * getX() - sin * getY(), sin * getX() + cos * getY());
    }
    
    @Override
    public Vector2 abs()
    {
        return create(FastMath.abs(getX()), FastMath.abs(getY()));
    }
    
    @Override
    public float dot(Vector2 vector)
    {
        return getX() * vector.getX() + getY() * vector.getY();
    }
    
    @Override
    public Vector2 lerp(Vector2 end, float factor)
    {
        if (factor == 0)
            return this;
        else if (factor == 1)
            return end;
        return Vector2.create(
                MathUtils.lerp(getX(), end.getX(), factor),
                MathUtils.lerp(getY(), end.getY(), factor));
    }
    
    @Override
    public Vector2 random(Vector2 end, Random rand)
    {
        return Vector2.create(
                MathUtils.lerp(getX(), end.getX(), rand.nextFloat()),
                MathUtils.lerp(getY(), end.getY(), rand.nextFloat()));
    }
    
    @Override
    public Vector2 min(Vector2 vector)
    {
        return Vector2.create(
                Math.min(getX(), vector.getX()),
                Math.min(getY(), vector.getY()));
    }
    
    @Override
    public Vector2 max(Vector2 vector)
    {
        return Vector2.create(
                Math.max(getX(), vector.getX()),
                Math.max(getY(), vector.getY()));
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        
        final Vector2 other = (Vector2)obj;
        return Float.floatToIntBits(getX()) == Float.floatToIntBits(other.getX())
                && Float.floatToIntBits(getY()) == Float.floatToIntBits(other.getY());
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(getX(), getY());
    }
    
    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .addValue(getX())
                .addValue(getY())
                .toString();
    }
    
    public static Vector2 create(float x, float y)
    {
        return new Vector2(x, y);
    }
}