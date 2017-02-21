package benjaminsannholm.util.math;

public final class MathUtils
{
    public static float lerp(float start, float end, float factor)
    {
        if (start == end)
            return start;
        return start + (end - start) * factor;
    }
    
    public static float slerp(float start, float end, float factor)
    {
        float diff = end - start;
        
        while (diff < -180)
            diff += 360;
        
        while (diff >= 180)
            diff -= 360;
        
        return start + diff * factor;
    }
    
    public static boolean isBetween(float value, float min, float max)
    {
        return min <= value && value <= max;
    }
    
    public static boolean isPointInBounds(Vector3 point, Vector3 min, Vector3 max)
    {
        return isBetween(point.getX(), min.getX(), max.getX())
                && isBetween(point.getY(), min.getY(), max.getY())
                && isBetween(point.getZ(), min.getZ(), max.getZ());
    }
    
    public static int nextPoT(int x)
    {
        x--;
        x |= x >> 1; // handle 2 bit numbers
        x |= x >> 2; // handle 4 bit numbers
        x |= x >> 4; // handle 8 bit numbers
        x |= x >> 8; // handle 16 bit numbers
        x |= x >> 16; // handle 32 bit numbers
        x++;
        return x;
    }
}