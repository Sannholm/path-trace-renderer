package benjaminsannholm.util.math;

import java.util.Random;

public class XSRandom extends Random
{
    private static final long serialVersionUID = 1477647080849334881L;

    private static final XSRandom GLOBAL = new XSRandom(System.nanoTime());
    
    public static XSRandom get()
    {
        return GLOBAL;
    }
    
    private long seed;
    
    public XSRandom(long seed)
    {
        setSeed(seed);
    }
    
    @Override
    protected int next(int nBits)
    {
        seed ^= seed << 21;
        seed ^= seed >>> 35;
        seed ^= seed << 4;
        return (int) (seed & (1 << nBits) - 1);
    }
    
    public long getSeed()
    {
        return seed;
    }
    
    @Override
    public void setSeed(long seed)
    {
        this.seed = seed;
    }
}