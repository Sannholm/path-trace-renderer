package benjaminsannholm.util.math;

public class FastMath
{
    private static final int BIG_ENOUGH_INT = 16 * 1024;
    private static final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;
    private static final double BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5;

    public static int fastFloor(double x)
    {
        return (int) (x + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
    }

    public static int fastRound(double x)
    {
        return (int) (x + BIG_ENOUGH_ROUND) - BIG_ENOUGH_INT;
    }

    public static int fastCeil(double x)
    {
        return BIG_ENOUGH_INT - (int) (BIG_ENOUGH_FLOOR - x);
    }

    public static boolean areLowBitsEqual(int value, int lowBits)
    {
        int highBits = 32 - lowBits;
        int lowValue = value << highBits >> highBits;
        return lowValue == lowValue >> 1;
    }

    public static boolean areLowBitsEqual(long value, int lowBits)
    {
        int highBits = 32 - lowBits;
        long lowValue = value << highBits >> highBits;
        return lowValue == lowValue >> 1;
    }

    //

    public static final float abs(float v)
    {
        return Float.intBitsToFloat(Float.floatToRawIntBits(v) & 0x7FFFFFFF);
    }

    public static final double abs(double v)
    {
        return Double.longBitsToDouble(Double.doubleToRawLongBits(v) & 0x7FFFFFFFFFFFFFFFL);
    }

    public static final float neg(float v)
    {
        return Float.intBitsToFloat(Float.floatToRawIntBits(v) & 0x80000000);
    }

    public static final double neg(double v)
    {
        return Double.longBitsToDouble(Double.doubleToRawLongBits(v) & 0x8000000000000000L);
    }

    public static final int clampPositive(int val)
    {
        // every negative value will be 0
        return (val >> 31 | val - 1) + 1;
    }

    public static final int clampByBits(int val, int bits)
    {
        // every positive value will be clamped to (1 << bits)-1
        val = (val >> 31 | val - 1) + 1;
        return (0 - (val & -1 << bits) >> 31 | val) & ~(-1 << bits);
    }

    /**
     * CACHE
     */

    private static final float RAD, DEG;
    private static final int SIN_BITS, SIN_MASK, SIN_COUNT;
    private static final float radFull, radToIndex;
    private static final float degFull, degToIndex;
    private static final float[] sin, cos;

    static
    {
        RAD = (float) Math.PI / 180.0f;
        DEG = 180.0f / (float) Math.PI;

        SIN_BITS = 12;
        SIN_MASK = ~(-1 << SIN_BITS);
        SIN_COUNT = SIN_MASK + 1;

        radFull = (float) (Math.PI * 2.0);
        degFull = (float) 360.0;
        radToIndex = SIN_COUNT / radFull;
        degToIndex = SIN_COUNT / degFull;

        sin = new float[SIN_COUNT];
        cos = new float[SIN_COUNT];

        for (int i = 0; i < SIN_COUNT; i++)
        {
            sin[i] = (float) Math.sin((i + 0.5f) / SIN_COUNT * radFull);
            cos[i] = (float) Math.cos((i + 0.5f) / SIN_COUNT * radFull);
        }
    }

    /**
     * SIN / COS (RAD)
     */

    public static final float sin(float rad)
    {
        return sin[(int) (rad * radToIndex) & SIN_MASK];
    }

    public static final float cos(float rad)
    {
        return cos[(int) (rad * radToIndex) & SIN_MASK];
    }

    /**
     * SIN / COS (DEG)
     */

    public static final float sinDeg(float deg)
    {
        return sin[(int) (deg * degToIndex) & SIN_MASK];
    }

    public static final float cosDeg(float deg)
    {
        return cos[(int) (deg * degToIndex) & SIN_MASK];
    }

    /**
     * SIN / COS (DEG - STRICT)
     */

    public static final float sinDegStrict(float deg)
    {
        return (float) Math.sin(deg * RAD);
    }

    public static final float cosDegStrict(float deg)
    {
        return (float) Math.cos(deg * RAD);
    }

    /**
     * ATAN2
     */

    private static final int ATAN2_BITS = 7;

    private static final int ATAN2_BITS2 = ATAN2_BITS << 1;
    private static final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
    private static final int ATAN2_COUNT = ATAN2_MASK + 1;
    private static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);

    private static final float ATAN2_DIM_MINUS_1 = ATAN2_DIM - 1;

    private static final float[] atan2 = new float[ATAN2_COUNT];

    static
    {
        for (int i = 0; i < ATAN2_DIM; i++)
        {
            for (int j = 0; j < ATAN2_DIM; j++)
            {
                float x0 = (float) i / ATAN2_DIM;
                float y0 = (float) j / ATAN2_DIM;

                atan2[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
            }
        }
    }

    /**
     * ATAN2
     */

    public static final float atan2Deg(float y, float x)
    {
        return FastMath.atan2(y, x) * DEG;
    }

    public static final float atan2DegStrict(float y, float x)
    {
        return (float) Math.atan2(y, x) * DEG;
    }

    public static final float atan2(float y, float x)
    {
        float add, mul;

        if (x < 0.0f)
        {
            if (y < 0.0f)
            {
                x = -x;
                y = -y;

                mul = 1.0f;
            }
            else
            {
                x = -x;
                mul = -1.0f;
            }

            add = -3.141592653f;
        }
        else
        {
            if (y < 0.0f)
            {
                y = -y;
                mul = -1.0f;
            }
            else
            {
                mul = 1.0f;
            }

            add = 0.0f;
        }

        float invDiv = ATAN2_DIM_MINUS_1 / (x < y ? y : x);

        int xi = (int) (x * invDiv);
        int yi = (int) (y * invDiv);

        return (atan2[yi * ATAN2_DIM + xi] + add) * mul;
    }

    public static void vector(float rad, float len, float[] result)
    {
        result[0] = FastMath.cos(rad) * len;
        result[1] = FastMath.sin(rad) * len;
    }

    private static final int NORMALIZE_LOOKUP_FACTOR = 1024;
    private static final float[] NORMALIZE_LOOKUP_TABLE = new float[NORMALIZE_LOOKUP_FACTOR + 1];

    static
    {
        for (int i = 0; i < NORMALIZE_LOOKUP_TABLE.length; i++)
        {
            NORMALIZE_LOOKUP_TABLE[i] = 1.0f / (float) Math.sqrt(i / (float) NORMALIZE_LOOKUP_FACTOR);
        }
    }

    public static void normalizeSlow(float[] v)
    {
        float square = v[0] * v[0] + v[1] * v[1] + v[2] * v[2];
        float factor = (float) (1.0 / Math.sqrt(square));

        v[0] *= factor;
        v[1] *= factor;
        v[2] *= factor;
    }

    public static void normalizeFast(float[] v)
    {
        float square = v[0] * v[0] + v[1] * v[1] + v[2] * v[2];
        float factor = NORMALIZE_LOOKUP_TABLE[(int) (square * NORMALIZE_LOOKUP_FACTOR)];

        v[0] *= factor;
        v[1] *= factor;
        v[2] *= factor;
    }

    public static void lerpNormals(float t, float[] n1, float[] n2, float[] nDst)
    {
        nDst[0] = n1[0] + t * (n2[0] - n1[0]);
        nDst[1] = n1[1] + t * (n2[1] - n1[1]);
        nDst[2] = n1[2] + t * (n2[2] - n1[2]);

        normalizeFast(nDst);
    }
}