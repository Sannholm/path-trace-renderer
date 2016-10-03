package benjaminsannholm.util.math;

import java.util.Objects;
import java.util.Random;

import com.google.common.base.MoreObjects;

public class Vector4 extends Vector<Vector4>
{
    public static final Vector4 ZERO = create(0, 0, 0, 0);
    public static final Vector4 ONE = create(1, 1, 1, 1);
    
    private final float x, y, z, w;
    
    private Vector4(float x, float y, float z, float w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
    
    public float getX()
    {
        return x;
    }
    
    public float getY()
    {
        return y;
    }
    
    public float getZ()
    {
        return z;
    }
    
    public float getW()
    {
        return w;
    }
    
    @Override
    public boolean isZero()
    {
        return getX() == 0 && getY() == 0 && getZ() == 0 && getW() == 0;
    }

    @Override
    protected Vector4 getZero()
    {
        return ZERO;
    }
    
    // DISTANCE / LENGTH
    
    @Override
    public float distanceSquared(Vector4 to)
    {
        final Vector4 delta = subtract(to);
        return delta.getX() * delta.getX()
                + delta.getY() * delta.getY()
                + delta.getZ() * delta.getZ()
                + delta.getW() * delta.getW();
    }
    
    // SET
    
    public Vector4 setX(float x)
    {
        if (x == getX())
            return this;
        return create(x, getY(), getZ(), getW());
    }
    
    public Vector4 setY(float y)
    {
        if (y == getY())
            return this;
        return create(getX(), y, getZ(), getW());
    }
    
    public Vector4 setZ(float z)
    {
        if (z == getZ())
            return this;
        return create(getX(), getY(), z, getW());
    }
    
    public Vector4 setW(float w)
    {
        if (w == getW())
            return this;
        return create(getX(), getY(), getZ(), w);
    }
    
    // ADD
    
    public Vector4 add(float x, float y, float z, float w)
    {
        if (x == 0 && y == 0 && z == 0 && w == 0)
            return this;
        return create(getX() + x, getY() + y, getZ() + z, getW() + w);
    }
    
    @Override
    public Vector4 add(Vector4 vector)
    {
        return add(vector.getX(), vector.getY(), vector.getZ(), vector.getW());
    }
    
    // SUBTRACT
    
    public Vector4 subtract(float x, float y, float z, float w)
    {
        if (x == 0 && y == 0 && z == 0 && w == 0)
            return this;
        return create(getX() - x, getY() - y, getZ() - z, getW() - w);
    }
    
    @Override
    public Vector4 subtract(Vector4 vector)
    {
        return subtract(vector.getX(), vector.getY(), vector.getZ(), vector.getW());
    }
    
    // MULTIPLY
    
    public Vector4 multiply(float x, float y, float z, float w)
    {
        if (x == 1 && y == 1 && z == 1 && w == 1)
            return this;
        return create(getX() * x, getY() * y, getZ() * z, getW() * w);
    }
    
    @Override
    public Vector4 multiply(Vector4 vector)
    {
        return multiply(vector.getX(), vector.getY(), vector.getZ(), vector.getW());
    }
    
    @Override
    public Vector4 multiply(float factor)
    {
        return multiply(factor, factor, factor, factor);
    }
    
    // DIVIDE
    
    public Vector4 divide(float x, float y, float z, float w)
    {
        if (x == 1 && y == 1 && z == 1 && w == 1)
            return this;
        return create(getX() / x, getY() / y, getZ() / z, getW() / w);
    }
    
    @Override
    public Vector4 divide(Vector4 vector)
    {
        return divide(vector.getX(), vector.getY(), vector.getZ(), vector.getW());
    }
    
    @Override
    public Vector4 divide(float divisor)
    {
        return divide(divisor, divisor, divisor, divisor);
    }
    
    // MISC

    @Override
    public Vector4 abs()
    {
        return create(FastMath.abs(getX()), FastMath.abs(getY()), FastMath.abs(getZ()), FastMath.abs(getW()));
    }

    @Override
    public float dot(Vector4 vector)
    {
        return getX() * vector.getX()
                + getY() * vector.getY()
                + getZ() * vector.getZ()
                + getW() * vector.getW();
    }
    
    @Override
    public Vector4 lerp(Vector4 end, float factor)
    {
        if (factor == 0)
            return this;
        else if (factor == 1)
            return end;
        return Vector4.create(
                MathUtils.lerp(getX(), end.getX(), factor),
                MathUtils.lerp(getY(), end.getY(), factor),
                MathUtils.lerp(getZ(), end.getZ(), factor),
                MathUtils.lerp(getW(), end.getW(), factor));
    }
    
    @Override
    public Vector4 random(Vector4 end, Random rand)
    {
        return Vector4.create(
                MathUtils.lerp(getX(), end.getX(), rand.nextFloat()),
                MathUtils.lerp(getY(), end.getY(), rand.nextFloat()),
                MathUtils.lerp(getZ(), end.getZ(), rand.nextFloat()),
                MathUtils.lerp(getW(), end.getW(), rand.nextFloat()));
    }

    @Override
    public Vector4 min(Vector4 vector)
    {
        return Vector4.create(
                Math.min(getX(), vector.getX()),
                Math.min(getY(), vector.getY()),
                Math.min(getZ(), vector.getZ()),
                Math.min(getW(), vector.getW()));
    }

    @Override
    public Vector4 max(Vector4 vector)
    {
        return Vector4.create(
                Math.max(getX(), vector.getX()),
                Math.max(getY(), vector.getY()),
                Math.max(getZ(), vector.getZ()),
                Math.max(getW(), vector.getW()));
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || getClass() != obj.getClass())
            return false;

        final Vector4 other = (Vector4) obj;
        return getX() == other.getX()
                && getY() == other.getY()
                && getZ() == other.getZ()
                && getW() == other.getW();
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(getX(), getY(), getZ(), getW());
    }
    
    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .addValue(getX())
                .addValue(getY())
                .addValue(getZ())
                .addValue(getW())
                .toString();
    }
    
    public static Vector4 create(float x, float y, float z, float w)
    {
        return new Vector4(x, y, z, w);
    }
}