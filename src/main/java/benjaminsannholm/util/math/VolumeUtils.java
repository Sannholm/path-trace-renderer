package benjaminsannholm.util.math;

import java.util.Collection;
import java.util.Iterator;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;

public final class VolumeUtils
{
    public static Optional<VolumeIntersection> intersectsSAT(Volume<?> v1, Volume<?> v2)
    {
        Vector3 mtvDir = null;
        float mtvDepth = Float.MAX_VALUE;

        for (Vector3 axis : Iterables.concat(v1.getSeparatingAxes(), v2.getSeparatingAxes()))
        {
            final Range<Float> proj1 = v1.project(axis);
            final Range<Float> proj2 = v2.project(axis);
            
            if (!proj1.isConnected(proj2))
            {
                return Optional.absent();
            }
            else
            {
                final float lower = Math.max(proj1.lowerEndpoint(), proj2.lowerEndpoint());
                final float upper = Math.min(proj1.upperEndpoint(), proj2.upperEndpoint());
                final float depth = upper - lower;

                if (depth < mtvDepth)
                {
                    final boolean flip = (proj1.lowerEndpoint() + proj1.upperEndpoint()) / 2 < (proj2.lowerEndpoint() + proj2.upperEndpoint()) / 2;
                    mtvDir = flip ? axis.negate() : axis;
                    mtvDepth = depth;
                }
            }
        }
        
        return Optional.of(new VolumeIntersection(mtvDir, mtvDepth));
    }
    
    public static Range<Float> projectOnAxis(Collection<Vector3> vertices, Vector3 axis)
    {
        final Iterator<Vector3> it = vertices.iterator();
        final float dot0 = axis.dot(it.next());

        float min = dot0, max = dot0;
        while (it.hasNext())
        {
            final float dot = axis.dot(it.next());
            min = Math.min(min, dot);
            max = Math.max(max, dot);
        }
        
        return Range.closed(min, max);
    }

    public static AABB calcBounds(Collection<Vector3> vertices)
    {
        final Iterator<Vector3> it = vertices.iterator();
        final Vector3 p0 = it.next();

        Vector3 min = p0, max = p0;
        while (it.hasNext())
        {
            final Vector3 p = it.next();
            min = min.min(p);
            max = max.max(p);
        }
        
        return new AABB(min, max);
    }
}