package benjaminsannholm.util.math;

import java.util.Objects;
import java.util.Random;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class Vector3 extends Vector<Vector3>
{
    public static final Vector3 ZERO = create(0, 0, 0);
    public static final Vector3 ONE = create(1, 1, 1);
    public static final Vector3 X_AXIS = create(1, 0, 0);
    public static final Vector3 Y_AXIS = create(0, 1, 0);
    public static final Vector3 Z_AXIS = create(0, 0, 1);
    
    private final float x, y, z;
    
    private Vector3(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
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
    
    @Override
    public boolean isZero()
    {
        return getX() == 0 && getY() == 0 && getZ() == 0;
    }
    
    @Override
    protected Vector3 getZero()
    {
        return ZERO;
    }
    
    // DISTANCE / LENGTH
    
    @Override
    public float distanceSquared(Vector3 to)
    {
        final Vector3 delta = subtract(to);
        return delta.getX() * delta.getX()
                + delta.getY() * delta.getY()
                + delta.getZ() * delta.getZ();
    }
    
    // SET
    
    public Vector3 setX(float x)
    {
        if (x == getX())
            return this;
        return create(x, getY(), getZ());
    }
    
    public Vector3 setY(float y)
    {
        if (y == getY())
            return this;
        return create(getX(), y, getZ());
    }
    
    public Vector3 setZ(float z)
    {
        if (z == getZ())
            return this;
        return create(getX(), getY(), z);
    }
    
    // ADD
    
    public Vector3 add(float x, float y, float z)
    {
        if (x == 0 && y == 0 && z == 0)
            return this;
        return create(getX() + x, getY() + y, getZ() + z);
    }
    
    @Override
    public Vector3 add(Vector3 vector)
    {
        return add(vector.getX(), vector.getY(), vector.getZ());
    }
    
    // SUBTRACT
    
    public Vector3 subtract(float x, float y, float z)
    {
        if (x == 0 && y == 0 && z == 0)
            return this;
        return create(getX() - x, getY() - y, getZ() - z);
    }
    
    @Override
    public Vector3 subtract(Vector3 vector)
    {
        return subtract(vector.getX(), vector.getY(), vector.getZ());
    }
    
    // MULTIPLY
    
    public Vector3 multiply(float x, float y, float z)
    {
        if (x == 1 && y == 1 && z == 1 || isZero())
            return this;
        return create(getX() * x, getY() * y, getZ() * z);
    }
    
    @Override
    public Vector3 multiply(Vector3 vector)
    {
        return multiply(vector.getX(), vector.getY(), vector.getZ());
    }
    
    @Override
    public Vector3 multiply(float factor)
    {
        return multiply(factor, factor, factor);
    }
    
    // DIVIDE
    
    public Vector3 divide(float x, float y, float z)
    {
        if (x == 1 && y == 1 && z == 1)
            return this;
        return create(getX() / x, getY() / y, getZ() / z);
    }
    
    @Override
    public Vector3 divide(Vector3 vector)
    {
        return divide(vector.getX(), vector.getY(), vector.getZ());
    }
    
    @Override
    public Vector3 divide(float divisor)
    {
        return divide(divisor, divisor, divisor);
    }
    
    // MISC
    
    public Vector3 rotateX(float degrees)
    {
        if (degrees == 0 || isZero())
            return this;
        final float cos = FastMath.cosDeg(degrees);
        final float sin = FastMath.sinDeg(degrees);
        return create(getX(), cos * getY() - sin * getZ(), sin * getY() + cos * getZ());
    }
    
    public Vector3 rotateY(float degrees)
    {
        if (degrees == 0 || isZero())
            return this;
        final float cos = FastMath.cosDeg(degrees);
        final float sin = FastMath.sinDeg(degrees);
        return create(cos * getX() - sin * getZ(), getY(), sin * getX() + cos * getZ());
    }
    
    public Vector3 rotateZ(float degrees)
    {
        if (degrees == 0 || isZero())
            return this;
        final float cos = FastMath.cosDeg(degrees);
        final float sin = FastMath.sinDeg(degrees);
        return create(cos * getX() - sin * getY(), sin * getX() + cos * getY(), getZ());
    }
    
    public Vector3 rotate(Quaternion quat)
    {
        Preconditions.checkNotNull(quat, "quat");
        if (quat.isIdentity() || isZero())
            return this;
        final Quaternion result = quat.multiply(Quaternion.create(getX(), getY(), getZ(), 0)).multiply(quat.conjugate());
        return create(result.getX(), result.getY(), result.getZ());
    }
    
    public Vector3 transform(Transform transform)
    {
        Preconditions.checkNotNull(transform, "transform");
        return multiply(transform.getScale()).rotate(transform.getRot()).add(transform.getPos());
    }
    
    @Override
    public Vector3 abs()
    {
        return create(FastMath.abs(getX()), FastMath.abs(getY()), FastMath.abs(getZ()));
    }
    
    @Override
    public float dot(Vector3 vector)
    {
        return getX() * vector.getX() + getY() * vector.getY() + getZ() * vector.getZ();
    }
    
    @Override
    public Vector3 lerp(Vector3 end, float factor)
    {
        if (factor == 0)
            return this;
        else if (factor == 1)
            return end;
        return Vector3.create(
                MathUtils.lerp(getX(), end.getX(), factor),
                MathUtils.lerp(getY(), end.getY(), factor),
                MathUtils.lerp(getZ(), end.getZ(), factor));
    }
    
    @Override
    public Vector3 random(Vector3 end, Random rand)
    {
        return Vector3.create(
                MathUtils.lerp(getX(), end.getX(), rand.nextFloat()),
                MathUtils.lerp(getY(), end.getY(), rand.nextFloat()),
                MathUtils.lerp(getZ(), end.getZ(), rand.nextFloat()));
    }
    
    @Override
    public Vector3 min(Vector3 vector)
    {
        return Vector3.create(
                Math.min(getX(), vector.getX()),
                Math.min(getY(), vector.getY()),
                Math.min(getZ(), vector.getZ()));
    }
    
    @Override
    public Vector3 max(Vector3 vector)
    {
        return Vector3.create(
                Math.max(getX(), vector.getX()),
                Math.max(getY(), vector.getY()),
                Math.max(getZ(), vector.getZ()));
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        
        final Vector3 other = (Vector3)obj;
        return Float.floatToIntBits(getX()) == Float.floatToIntBits(other.getX())
                && Float.floatToIntBits(getY()) == Float.floatToIntBits(other.getY())
                && Float.floatToIntBits(getZ()) == Float.floatToIntBits(other.getZ());
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(getX(), getY(), getZ());
    }
    
    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .addValue(getX())
                .addValue(getY())
                .addValue(getZ())
                .toString();
    }
    
    public static Vector3 create(float x, float y, float z)
    {
        return new Vector3(x, y, z);
    }
}