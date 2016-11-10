package benjaminsannholm.util.math;

import com.google.common.base.Preconditions;

public class RayIntersection
{
    public final float fraction;
    public final Vector3 point;
    public final Vector3 normal;
    
    RayIntersection(float fraction, Vector3 point, Vector3 normal)
    {
        this.fraction = fraction;
        this.point = Preconditions.checkNotNull(point, "point");
        this.normal = Preconditions.checkNotNull(normal, "normal");
    }
}