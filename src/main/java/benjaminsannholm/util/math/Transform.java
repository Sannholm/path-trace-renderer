package benjaminsannholm.util.math;

import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class Transform
{
    public static final Transform IDENTITY = create(Vector3.ZERO, Quaternion.IDENTITY, Vector3.ONE);
    
    private final Vector3 pos;
    private final Quaternion rot;
    private final Vector3 scale;
    
    private Matrix4 matrix;
    
    private Transform(Vector3 pos, Quaternion rot, Vector3 scale)
    {
        this.pos = Preconditions.checkNotNull(pos, "pos");
        this.rot = Preconditions.checkNotNull(rot, "rot");
        this.scale = Preconditions.checkNotNull(scale, "scale");
    }
    
    public Vector3 getPos()
    {
        return pos;
    }
    
    public Quaternion getRot()
    {
        return rot;
    }
    
    public Vector3 getScale()
    {
        return scale;
    }
    
    public Transform setPos(Vector3 pos)
    {
        return create(pos, rot, scale);
    }
    
    public Transform setRot(Quaternion rot)
    {
        return create(pos, rot, scale);
    }
    
    public Transform setScale(Vector3 scale)
    {
        return create(pos, rot, scale);
    }
    
    public Transform lerp(Transform end, float factor)
    {
        return create(getPos().lerp(end.getPos(), factor), getRot().slerp(end.getRot(), factor), getScale().lerp(end.getScale(), factor));
    }
    
    public Matrix4 toMatrix()
    {
        if (matrix != null)
            return matrix;
        
        final Vector3 p = getPos();
        final Quaternion r = getRot();
        final Vector3 s = getScale();
        
        final float xx = r.getX() * r.getX();
        final float xy = r.getX() * r.getY();
        final float xz = r.getX() * r.getZ();
        final float xw = r.getX() * r.getW();
        
        final float yy = r.getY() * r.getY();
        final float yz = r.getY() * r.getZ();
        final float yw = r.getY() * r.getW();
        
        final float zz = r.getZ() * r.getZ();
        final float zw = r.getZ() * r.getW();
        
        final float m00 = 1 - 2 * (yy + zz);
        final float m01 = 2 * (xy - zw);
        final float m02 = 2 * (xz + yw);
        
        final float m10 = 2 * (xy + zw);
        final float m11 = 1 - 2 * (xx + zz);
        final float m12 = 2 * (yz - xw);
        
        final float m20 = 2 * (xz - yw);
        final float m21 = 2 * (yz + xw);
        final float m22 = 1 - 2 * (xx + yy);
        
        return matrix = Matrix4.create(
                m00 * s.getX(), m01 * s.getX(), m02 * s.getX(), p.getX(),
                m10 * s.getY(), m11 * s.getY(), m12 * s.getY(), p.getY(),
                m20 * s.getZ(), m21 * s.getZ(), m22 * s.getZ(), p.getZ(),
                0, 0, 0, 1);
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        
        final Transform other = (Transform)obj;
        return getPos().equals(other.getPos())
                && getRot().equals(other.getRot())
                && getScale().equals(other.getScale());
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(getPos(), getRot(), getScale());
    }
    
    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("pos", getPos())
                .add("rot", getRot())
                .add("scale", getScale())
                .toString();
    }
    
    public static Transform create(Vector3 pos, Quaternion rot, Vector3 scale)
    {
        return new Transform(pos, rot, scale);
    }
}