package benjaminsannholm.util.math;

import java.nio.FloatBuffer;
import java.util.Objects;

public class Matrix4
{
    public static final Matrix4 IDENTITY = create(
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1);
    
    private final float m00, m01, m02, m03;
    private final float m10, m11, m12, m13;
    private final float m20, m21, m22, m23;
    private final float m30, m31, m32, m33;
    
    private Matrix4(
            float m00, float m01, float m02, float m03,
            float m10, float m11, float m12, float m13,
            float m20, float m21, float m22, float m23,
            float m30, float m31, float m32, float m33)
    {
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;
        
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        
        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
    }

    public Vector4 getC0()
    {
        return Vector4.create(m00, m10, m20, m30);
    }

    public Vector4 getC1()
    {
        return Vector4.create(m01, m11, m21, m31);
    }

    public Vector4 getC2()
    {
        return Vector4.create(m02, m12, m22, m32);
    }

    public Vector4 getC3()
    {
        return Vector4.create(m03, m13, m23, m33);
    }
    
    public Vector3 get3C0()
    {
        return Vector3.create(m00, m10, m20);
    }

    public Vector3 get3C1()
    {
        return Vector3.create(m01, m11, m21);
    }

    public Vector3 get3C2()
    {
        return Vector3.create(m02, m12, m22);
    }

    public Vector3 get3C3()
    {
        return Vector3.create(m03, m13, m23);
    }
    
    public Matrix4 translate(float x, float y, float z)
    {
        return multiply(createTranslation(x, y, z));
    }
    
    public Matrix4 translate(Vector3 vec)
    {
        return translate(vec.getX(), vec.getY(), vec.getZ());
    }
    
    public Matrix4 scale(float x, float y, float z)
    {
        return multiply(createScale(x, y, z));
    }
    
    public Matrix4 scale(Vector3 vec)
    {
        return translate(vec.getX(), vec.getY(), vec.getZ());
    }
    
    public Matrix4 rotate(Quaternion quat)
    {
        return multiply(quat.toMatrix4());
    }
    
    public Matrix4 multiply(Matrix4 mat)
    {
        final float nm00 = m00 * mat.m00 + m10 * mat.m01 + m20 * mat.m02 + m30 * mat.m03;
        final float nm01 = m01 * mat.m00 + m11 * mat.m01 + m21 * mat.m02 + m31 * mat.m03;
        final float nm02 = m02 * mat.m00 + m12 * mat.m01 + m22 * mat.m02 + m32 * mat.m03;
        final float nm03 = m03 * mat.m00 + m13 * mat.m01 + m23 * mat.m02 + m33 * mat.m03;
        
        final float nm10 = m00 * mat.m10 + m10 * mat.m11 + m20 * mat.m12 + m30 * mat.m13;
        final float nm11 = m01 * mat.m10 + m11 * mat.m11 + m21 * mat.m12 + m31 * mat.m13;
        final float nm12 = m02 * mat.m10 + m12 * mat.m11 + m22 * mat.m12 + m32 * mat.m13;
        final float nm13 = m03 * mat.m10 + m13 * mat.m11 + m23 * mat.m12 + m33 * mat.m13;
        
        final float nm20 = m00 * mat.m20 + m10 * mat.m21 + m20 * mat.m22 + m30 * mat.m23;
        final float nm21 = m01 * mat.m20 + m11 * mat.m21 + m21 * mat.m22 + m31 * mat.m23;
        final float nm22 = m02 * mat.m20 + m12 * mat.m21 + m22 * mat.m22 + m32 * mat.m23;
        final float nm23 = m03 * mat.m20 + m13 * mat.m21 + m23 * mat.m22 + m33 * mat.m23;
        
        final float nm30 = m00 * mat.m30 + m10 * mat.m31 + m20 * mat.m32 + m30 * mat.m33;
        final float nm31 = m01 * mat.m30 + m11 * mat.m31 + m21 * mat.m32 + m31 * mat.m33;
        final float nm32 = m02 * mat.m30 + m12 * mat.m31 + m22 * mat.m32 + m32 * mat.m33;
        final float nm33 = m03 * mat.m30 + m13 * mat.m31 + m23 * mat.m32 + m33 * mat.m33;
        
        return create(
                nm00, nm01, nm02, nm03,
                nm10, nm11, nm12, nm13,
                nm20, nm21, nm22, nm23,
                nm30, nm31, nm32, nm33);
    }

    public Matrix4 invert()
    {
        final float dA0 = m00 * m11 - m01 * m10;
        final float dA1 = m00 * m12 - m02 * m10;
        final float dA2 = m00 * m13 - m03 * m10;
        final float dA3 = m01 * m12 - m02 * m11;
        final float dA4 = m01 * m13 - m03 * m11;
        final float dA5 = m02 * m13 - m03 * m12;
        final float dB0 = m20 * m31 - m21 * m30;
        final float dB1 = m20 * m32 - m22 * m30;
        final float dB2 = m20 * m33 - m23 * m30;
        final float dB3 = m21 * m32 - m22 * m31;
        final float dB4 = m21 * m33 - m23 * m31;
        final float dB5 = m22 * m33 - m23 * m32;
        final float det = dA0 * dB5 - dA1 * dB4 + dA2 * dB3 + dA3 * dB2 - dA4 * dB1 + dA5 * dB0;

        if (Math.abs(det) <= 0)
            throw new ArithmeticException("Matrix cannot be inverted, determinant is <= 0");
        
        final float invDet = 1 / det;

        final float nm00 = (+m11 * dB5 - m12 * dB4 + m13 * dB3) * invDet;
        final float nm10 = (-m10 * dB5 + m12 * dB2 - m13 * dB1) * invDet;
        final float nm20 = (+m10 * dB4 - m11 * dB2 + m13 * dB0) * invDet;
        final float nm30 = (-m10 * dB3 + m11 * dB1 - m12 * dB0) * invDet;
        final float nm01 = (-m01 * dB5 + m02 * dB4 - m03 * dB3) * invDet;
        final float nm11 = (+m00 * dB5 - m02 * dB2 + m03 * dB1) * invDet;
        final float nm21 = (-m00 * dB4 + m01 * dB2 - m03 * dB0) * invDet;
        final float nm31 = (+m00 * dB3 - m01 * dB1 + m02 * dB0) * invDet;
        final float nm02 = (+m31 * dA5 - m32 * dA4 + m33 * dA3) * invDet;
        final float nm12 = (-m30 * dA5 + m32 * dA2 - m33 * dA1) * invDet;
        final float nm22 = (+m30 * dA4 - m31 * dA2 + m33 * dA0) * invDet;
        final float nm32 = (-m30 * dA3 + m31 * dA1 - m32 * dA0) * invDet;
        final float nm03 = (-m21 * dA5 + m22 * dA4 - m23 * dA3) * invDet;
        final float nm13 = (+m20 * dA5 - m22 * dA2 + m23 * dA1) * invDet;
        final float nm23 = (-m20 * dA4 + m21 * dA2 - m23 * dA0) * invDet;
        final float nm33 = (+m20 * dA3 - m21 * dA1 + m22 * dA0) * invDet;

        return create(
                nm00, nm01, nm02, nm03,
                nm10, nm11, nm12, nm13,
                nm20, nm21, nm22, nm23,
                nm30, nm31, nm32, nm33);
    }
    
    public void writeTo(FloatBuffer buffer)
    {
        buffer.put(m00).put(m10).put(m20).put(m30)
                .put(m01).put(m11).put(m21).put(m31)
                .put(m02).put(m12).put(m22).put(m32)
                .put(m03).put(m13).put(m23).put(m33);
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        
        final Matrix4 other = (Matrix4)obj;
        return Float.floatToIntBits(m00) == Float.floatToIntBits(other.m00)
                && Float.floatToIntBits(m01) == Float.floatToIntBits(other.m01)
                && Float.floatToIntBits(m02) == Float.floatToIntBits(other.m02)
                && Float.floatToIntBits(m03) == Float.floatToIntBits(other.m03)

                && Float.floatToIntBits(m10) == Float.floatToIntBits(other.m10)
                && Float.floatToIntBits(m11) == Float.floatToIntBits(other.m11)
                && Float.floatToIntBits(m12) == Float.floatToIntBits(other.m12)
                && Float.floatToIntBits(m13) == Float.floatToIntBits(other.m13)

                && Float.floatToIntBits(m20) == Float.floatToIntBits(other.m20)
                && Float.floatToIntBits(m21) == Float.floatToIntBits(other.m21)
                && Float.floatToIntBits(m22) == Float.floatToIntBits(other.m22)
                && Float.floatToIntBits(m23) == Float.floatToIntBits(other.m23)

                && Float.floatToIntBits(m30) == Float.floatToIntBits(other.m30)
                && Float.floatToIntBits(m31) == Float.floatToIntBits(other.m31)
                && Float.floatToIntBits(m32) == Float.floatToIntBits(other.m32)
                && Float.floatToIntBits(m33) == Float.floatToIntBits(other.m33);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33);
    }
    
    @Override
    public String toString()
    {
        return new StringBuilder()
                .append(m00).append(' ').append(m01).append(' ').append(m02).append(' ').append(m03).append('\n')
                .append(m10).append(' ').append(m11).append(' ').append(m12).append(' ').append(m13).append('\n')
                .append(m20).append(' ').append(m21).append(' ').append(m22).append(' ').append(m23).append('\n')
                .append(m30).append(' ').append(m31).append(' ').append(m32).append(' ').append(m33).append('\n')
                .toString();
    }
    
    public static Matrix4 create(
            float m00, float m01, float m02, float m03,
            float m10, float m11, float m12, float m13,
            float m20, float m21, float m22, float m23,
            float m30, float m31, float m32, float m33)
    {
        return new Matrix4(
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33);
    }

    public static Matrix4 fromBuffer(FloatBuffer buffer)
    {
        final float m00 = buffer.get();
        final float m10 = buffer.get();
        final float m20 = buffer.get();
        final float m30 = buffer.get();

        final float m01 = buffer.get();
        final float m11 = buffer.get();
        final float m21 = buffer.get();
        final float m31 = buffer.get();

        final float m02 = buffer.get();
        final float m12 = buffer.get();
        final float m22 = buffer.get();
        final float m32 = buffer.get();

        final float m03 = buffer.get();
        final float m13 = buffer.get();
        final float m23 = buffer.get();
        final float m33 = buffer.get();

        return create(
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33);
    }
    
    public static Matrix4 createTranslation(float x, float y, float z)
    {
        return create(
                1, 0, 0, x,
                0, 1, 0, y,
                0, 0, 1, z,
                0, 0, 0, 1);
    }
    
    public static Matrix4 createTranslation(Vector3 vec)
    {
        return createTranslation(vec.getX(), vec.getY(), vec.getZ());
    }
    
    public static Matrix4 createScale(float x, float y, float z)
    {
        return create(
                x, 0, 0, 0,
                0, y, 0, 0,
                0, 0, z, 0,
                0, 0, 0, 1);
    }
    
    public static Matrix4 createScale(Vector3 vec)
    {
        return createScale(vec.getX(), vec.getY(), vec.getZ());
    }
    
    public static Matrix4 createPerspectiveProjection(float near, float far, float fov, float aspectRatio)
    {
        final float depth = near - far;
        final float height = 1 / (float)Math.tan(Math.toRadians(fov / 2));
        
        final float m00 = height / aspectRatio;
        final float m11 = height;
        final float m22 = (far + near) / depth;
        final float m23 = 2 * near * far / depth;
        
        return create(
                m00, 0, 0, 0,
                0, m11, 0, 0,
                0, 0, m22, m23,
                0, 0, -1, 0);
    }
    
    public static Matrix4 createOrthographicProjection(float near, float far, float zoom, float aspectRatio)
    {
        final float r = zoom;
        final float t = r / aspectRatio;
        
        final float m00 = 1 / r;
        final float m11 = 1 / t;
        final float m22 = -2 / (far - near);
        final float m23 = -(far + near) / (far - near);
        
        return create(
                m00, 0, 0, 0,
                0, m11, 0, 0,
                0, 0, m22, m23,
                0, 0, 0, 1);
    }
}