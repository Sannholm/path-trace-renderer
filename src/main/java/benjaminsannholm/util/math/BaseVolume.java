package benjaminsannholm.util.math;

import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.Range;

public abstract class BaseVolume<T extends Volume<T>> implements Volume<T>
{
    private AABB bounds;
    private Set<Vector3> vertexPositions;
    
    @Override
    public AABB getBounds()
    {
        return bounds != null ? bounds : (bounds = VolumeUtils.calcBounds(getVertexPositions()));
    }
    
    protected abstract Set<Vector3> calcVertexPositions();
    
    @Override
    public Set<Vector3> getVertexPositions()
    {
        return vertexPositions != null ? vertexPositions : (vertexPositions = calcVertexPositions());
    }

    @Override
    public Volume<?> difference(Volume<?> other)
    {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Range<Float> project(Vector3 axis)
    {
        return VolumeUtils.projectOnAxis(getVertexPositions(), axis);
    }
    
    @Override
    public Optional<VolumeIntersection> intersects(Volume<?> volume)
    {
        return VolumeUtils.intersectsSAT(this, volume);
    }
}