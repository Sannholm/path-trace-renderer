package benjaminsannholm.util.math;

import com.google.common.base.Preconditions;

public class VolumeIntersection
{
    public final Vector3 mtvDir;
    public final float mtvDepth;
    
    VolumeIntersection(Vector3 mtvDir, float mtvDepth)
    {
        this.mtvDir = Preconditions.checkNotNull(mtvDir, "mtvDir");
        this.mtvDepth = mtvDepth;
    }
}