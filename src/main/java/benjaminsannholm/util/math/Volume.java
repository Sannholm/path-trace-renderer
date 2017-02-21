package benjaminsannholm.util.math;

import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.Range;

public interface Volume<T extends Volume<T>>
{
    AABB getBounds();
    
    Set<Vector3> getVertexPositions();
    
    Set<Vector3> getSeparatingAxes();
    
    T transform(Transform transform);
    
    T translate(Vector3 vector);
    
    Volume<?> difference(Volume<?> other);
    
    Range<Float> project(Vector3 axis);
    
    boolean contains(Vector3 point);
    
    boolean intersectsRayBool(Vector3 start, Vector3 end);
    
    Optional<RayIntersection> intersectsRay(Vector3 start, Vector3 end);
    
    Optional<VolumeIntersection> intersects(Volume<?> volume);
}