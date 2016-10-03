package benjaminsannholm.util.math;

import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

public class AABB extends BaseVolume<AABB>
{
    private static final Set<Vector3> SEPARATING_AXES = ImmutableSet.<Vector3>builder()
            .add(Vector3.X_AXIS).add(Vector3.Y_AXIS).add(Vector3.Z_AXIS)
            .build();
    
    private final Vector3 min, max;
    
    public AABB(Vector3 min, Vector3 max)
    {
        this.min = Preconditions.checkNotNull(min, "min");
        this.max = Preconditions.checkNotNull(max, "max");
    }

    public Vector3 getMin()
    {
        return min;
    }

    public Vector3 getMax()
    {
        return max;
    }
    
    @Override
    public AABB getBounds()
    {
        return this;
    }

    public Vector3 getCenter()
    {
        return max.subtract(min).multiply(0.5F);
    }
    
    @Override
    public Set<Vector3> calcVertexPositions()
    {
        final Vector3 min = getMin();
        final Vector3 max = getMax();
        return ImmutableSet.<Vector3>builder()
                // Bottom
                .add(min)
                .add(Vector3.create(max.getX(), min.getY(), min.getZ()))
                .add(Vector3.create(max.getX(), min.getY(), max.getZ()))
                .add(Vector3.create(min.getX(), min.getY(), max.getZ()))
                // Top
                .add(Vector3.create(min.getX(), max.getY(), min.getZ()))
                .add(Vector3.create(max.getX(), max.getY(), min.getZ()))
                .add(max)
                .add(Vector3.create(min.getX(), max.getY(), max.getZ()))
                .build();
    }
    
    @Override
    public Set<Vector3> getSeparatingAxes()
    {
        return SEPARATING_AXES;
    }

    @Override
    public AABB transform(Transform transform)
    {
        final Matrix4 mat = transform.toMatrix();
        final Vector3 c0 = mat.get3C0();
        final Vector3 c1 = mat.get3C1();
        final Vector3 c2 = mat.get3C2();
        final Vector3 c3 = mat.get3C3();

        final Vector3 xa = c0.multiply(getMin().getX());
        final Vector3 xb = c0.multiply(getMax().getX());

        final Vector3 ya = c1.multiply(getMin().getY());
        final Vector3 yb = c1.multiply(getMax().getY());

        final Vector3 za = c2.multiply(getMin().getZ());
        final Vector3 zb = c2.multiply(getMax().getZ());

        final Vector3 min = xa.min(xb).add(ya.min(yb)).add(za.min(zb)).add(c3);
        final Vector3 max = xa.max(xb).add(ya.max(yb)).add(za.max(zb)).add(c3);
        
        return new AABB(min, max);
    }
    
    @Override
    public AABB translate(Vector3 vector)
    {
        return new AABB(getMin().add(vector), getMax().add(vector));
    }

    @Override
    public Volume<?> difference(Volume<?> other)
    {
        if (other instanceof AABB)
        {
            final AABB otherAABB = ((AABB) other);
            final Vector3 min = getMin().subtract(otherAABB.getMax());
            final Vector3 size = getMax().subtract(getMin()).add(otherAABB.getMax().subtract(otherAABB.getMin()));
            return new AABB(min, min.add(size));
        }

        return super.difference(other);
    }

    public AABB encapsulate(Vector3 point)
    {
        return new AABB(getMin().min(point), getMax().max(point));
    }

    public AABB encapsulate(Volume<?> volume)
    {
        final AABB bounds = volume.getBounds();
        return new AABB(getMin().min(bounds.getMin()), getMax().max(bounds.getMax()));
    }

    @Override
    public boolean contains(Vector3 point)
    {
        return MathUtils.isPointInBounds(point, getMin(), getMax());
    }
    
    @Override
    public boolean intersectsRayBool(Vector3 start, Vector3 end)
    {
        if (contains(start))
            return true;
        
        final Vector3 ray = end.subtract(start);
        final Vector3 rayDir = ray.normalize();
        final Vector3 invRayDir = Vector3.create(1 / rayDir.getX(), 1 / rayDir.getY(), 1 / rayDir.getZ());
        
        final float tx1 = (getMin().getX() - start.getX()) * invRayDir.getX();
        final float tx2 = (getMax().getX() - start.getX()) * invRayDir.getX();

        float tmin = Math.min(tx1, tx2);
        float tmax = Math.max(tx1, tx2);

        final float ty1 = (getMin().getY() - start.getY()) * invRayDir.getY();
        final float ty2 = (getMax().getY() - start.getY()) * invRayDir.getY();

        tmin = Math.max(tmin, Math.min(ty1, ty2));
        tmax = Math.min(tmax, Math.max(ty1, ty2));

        final float tz1 = (getMin().getZ() - start.getZ()) * invRayDir.getZ();
        final float tz2 = (getMax().getZ() - start.getZ()) * invRayDir.getZ();

        tmin = Math.max(tmin, Math.min(tz1, tz2));
        tmax = Math.min(tmax, Math.max(tz1, tz2));

        return tmax >= Math.max(0, tmin) && tmin * tmin <= ray.lengthSquared();
    }
    
    @Override
    public Optional<RayIntersection> intersectsRay(Vector3 start, Vector3 end)
    {
        if (contains(start))
            return Optional.of(new RayIntersection(0, start, Vector3.ZERO));
        
        final Vector3 ray = end.subtract(start);
        final Vector3 rayDir = ray.normalize();
        final Vector3 invRayDir = Vector3.create(1 / rayDir.getX(), 1 / rayDir.getY(), 1 / rayDir.getZ());

        final float tx1 = (getMin().getX() - start.getX()) * invRayDir.getX();
        final float tx2 = (getMax().getX() - start.getX()) * invRayDir.getX();
        final float txMin = Math.min(tx1, tx2);

        float tmin = txMin;
        float tmax = Math.max(tx1, tx2);

        final float ty1 = (getMin().getY() - start.getY()) * invRayDir.getY();
        final float ty2 = (getMax().getY() - start.getY()) * invRayDir.getY();
        final float tyMin = Math.min(ty1, ty2);

        tmin = Math.max(tmin, tyMin);
        tmax = Math.min(tmax, Math.max(ty1, ty2));

        final float tz1 = (getMin().getZ() - start.getZ()) * invRayDir.getZ();
        final float tz2 = (getMax().getZ() - start.getZ()) * invRayDir.getZ();
        final float tzMin = Math.min(tz1, tz2);

        tmin = Math.max(tmin, tzMin);
        tmax = Math.min(tmax, Math.max(tz1, tz2));

        if (tmax >= Math.max(0, tmin) && tmin * tmin <= ray.lengthSquared())
        {
            Vector3 normal = null;
            if (txMin > tyMin && txMin > tzMin)
            {
                normal = Vector3.create(-Math.signum(ray.getX()), 0, 0);
            }
            else if (tyMin > txMin && tyMin > tzMin)
            {
                normal = Vector3.create(0, -Math.signum(ray.getY()), 0);
            }
            else
            {
                normal = Vector3.create(0, 0, -Math.signum(ray.getZ()));
            }
            
            return Optional.of(new RayIntersection(tmin / ray.length(), rayDir.multiply(tmin).add(start), normal));
        }
        
        return Optional.absent();
    }

    public boolean intersectsX(AABB other)
    {
        return other.getMax().getX() > getMin().getX() && other.getMin().getX() < getMax().getX();
    }

    public boolean intersectsY(AABB other)
    {
        return other.getMax().getY() > getMin().getY() && other.getMin().getY() < getMax().getY();
    }

    public boolean intersectsZ(AABB other)
    {
        return other.getMax().getZ() > getMin().getZ() && other.getMin().getZ() < getMax().getZ();
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("min", getMin())
                .add("max", getMax())
                .toString();
    }
}