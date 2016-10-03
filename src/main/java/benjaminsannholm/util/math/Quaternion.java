package benjaminsannholm.util.math;

import com.google.common.base.MoreObjects;

public class Quaternion
{
    public static final Quaternion IDENTITY = create(0, 0, 0, 1);
    
    private final float x, y, z, w;
    
    private Quaternion(float x, float y, float z, float w)
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
    
    public boolean isIdentity()
    {
        return x == 0 && y == 0 && z == 0 && w == 1;
    }
    
    public Quaternion multiply(Quaternion quat)
    {
        final float x = getW() * quat.getX() + quat.getW() * getX() + getY() * quat.getZ() - getZ() * quat.getY();
        final float y = getW() * quat.getY() + quat.getW() * getY() - getX() * quat.getZ() + getZ() * quat.getX();
        final float z = getW() * quat.getZ() + quat.getW() * getZ() + getX() * quat.getY() - getY() * quat.getX();
        final float w = getW() * quat.getW() - getX() * quat.getX() - getY() * quat.getY() - getZ() * quat.getZ();
        return create(x, y, z, w);
    }
    
    public Quaternion conjugate()
    {
        return create(-getX(), -getY(), -getZ(), getW());
    }
    
    public Quaternion slerp(Quaternion end, float factor)
    {
        if (factor == 0)
            return this;
        else if (factor == 1)
            return end;

        final float d = getX() * end.getX() + getY() * end.getY() + getZ() * end.getZ() + getW() * end.getW();
        final float absDot = Math.abs(d);

        // Set the first and second scale for the interpolation
        float scale0 = 1 - factor;
        float scale1 = factor;

        // Check if the angle between the 2 quaternions was big enough to
        // warrant such calculations
        if ((1 - absDot) > 0.1)
        {
            // Get the angle between the 2 quaternions,
            // and then store the sin() of that angle
            final float angle = (float) Math.acos(absDot);
            final float invSinTheta = 1 / FastMath.sin(angle);

            // Calculate the scale for q1 and q2, according to the angle and
            // it's sine value
            scale0 = FastMath.sin(scale0 * angle) * invSinTheta;
            scale1 = FastMath.sin(scale1 * angle) * invSinTheta;
        }

        if (d < 0)
            scale1 = -scale1;

        // Calculate the x, y, z and w values for the quaternion by using a
        // special form of linear interpolation for quaternions.
        final float x = scale0 * getX() + scale1 * end.getX();
        final float y = scale0 * getY() + scale1 * end.getY();
        final float z = scale0 * getZ() + scale1 * end.getZ();
        final float w = scale0 * getW() + scale1 * end.getW();
        
        return create(x, y, z, w);
    }
    
    public Matrix4 toMatrix4()
    {
        final float xx = getX() * getX();
        final float xy = getX() * getY();
        final float xz = getX() * getZ();
        final float xw = getX() * getW();
        
        final float yy = getY() * getY();
        final float yz = getY() * getZ();
        final float yw = getY() * getW();
        
        final float zz = getZ() * getZ();
        final float zw = getZ() * getW();
        
        final float m00 = 1 - 2 * (yy + zz);
        final float m01 = 2 * (xy - zw);
        final float m02 = 2 * (xz + yw);
        
        final float m10 = 2 * (xy + zw);
        final float m11 = 1 - 2 * (xx + zz);
        final float m12 = 2 * (yz - xw);
        
        final float m20 = 2 * (xz - yw);
        final float m21 = 2 * (yz + xw);
        final float m22 = 1 - 2 * (xx + yy);
        
        return Matrix4.create(
                m00, m01, m02, 0,
                m10, m11, m12, 0,
                m20, m21, m22, 0,
                0, 0, 0, 1);
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
    
    public static Quaternion create(float x, float y, float z, float w)
    {
        return new Quaternion(x, y, z, w);
    }
    
    public static Quaternion fromAxisAngle(Vector3 axis, float degrees)
    {
        final float halfAngle = degrees / 2;
        axis = axis.normalize().multiply(FastMath.sinDeg(halfAngle));
        return create(axis.getX(), axis.getY(), axis.getZ(), FastMath.cosDeg(halfAngle));
    }
    
    public static Quaternion fromEulerAngles(float pitch, float yaw, float roll)
    {
        final float halfYaw = yaw * 0.5F;
        final float sinYaw = FastMath.sinDeg(halfYaw);
        final float cosYaw = FastMath.cosDeg(halfYaw);
        final float halfPitch = pitch * 0.5F;
        final float sinPitch = FastMath.sinDeg(halfPitch);
        final float cosPitch = FastMath.cosDeg(halfPitch);
        final float halfRoll = roll * 0.5F;
        final float sinRoll = FastMath.sinDeg(halfRoll);
        final float cosRoll = FastMath.cosDeg(halfRoll);
        
        final float cosYawXcosRoll = cosYaw * cosRoll;
        final float sinYawXsinRoll = sinYaw * sinRoll;
        final float cosYawXsinRoll = cosYaw * sinRoll;
        final float sinYawXcosRoll = sinYaw * cosRoll;
        
        final float x = cosYawXcosRoll * sinPitch + sinYawXsinRoll * cosPitch;
        final float y = sinYawXcosRoll * cosPitch + cosYawXsinRoll * sinPitch;
        final float z = cosYawXsinRoll * cosPitch - sinYawXcosRoll * sinPitch;
        final float w = cosYawXcosRoll * cosPitch - sinYawXsinRoll * sinPitch;
        
        return create(x, y, z, w);
    }
}