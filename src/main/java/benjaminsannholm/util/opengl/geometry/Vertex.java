package benjaminsannholm.util.opengl.geometry;

import com.google.common.base.Preconditions;

import benjaminsannholm.util.math.Vector2;
import benjaminsannholm.util.math.Vector3;

public class Vertex
{
    private final Vector3 pos;
    private final Vector3 normal;
    private final Vector2 uv;
    
    public Vertex(Vector3 pos, Vector3 normal, Vector2 uv)
    {
        this.pos = Preconditions.checkNotNull(pos, "pos");
        this.normal = Preconditions.checkNotNull(normal, "normal");
        this.uv = Preconditions.checkNotNull(uv, "uv");
    }
    
    public Vector3 getPos()
    {
        return pos;
    }
    
    public Vector3 getNormal()
    {
        return normal;
    }
    
    public Vector2 getUV()
    {
        return uv;
    }
}