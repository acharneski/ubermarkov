package com.simiacryptus.math;

import java.util.Iterator;
import java.util.Random;

import com.simiacryptus.data.DoubleRange;
import com.simiacryptus.data.DoubleVolume;

public final class UniformPointSource implements Iterable<double[]>
{
  private final class UniformIterator implements Iterator<double[]>
  {
    private final Random iteratorRandom = new Random(
                                            UniformPointSource.this.random
                                                .nextLong());
    
    private UniformIterator()
    {
    }
    
    @Override
    public boolean hasNext()
    {
      return true;
    }
    
    @Override
    public double[] next()
    {
      final double[] point = new double[UniformPointSource.this.range.size()];
      for (int i = 0; i < point.length; i++)
      {
        DoubleRange range = UniformPointSource.this.range.get(i);
        point[i] = range.from() + this.iteratorRandom.nextDouble() * range.size();
      }
      // point = range.canonicalValue(point);
      return point;
    }
    
    @Override
    public void remove()
    {
      throw new IllegalArgumentException();
    }
  }
  
  private final DoubleVolume range;
  private final Random       random;
  
  public UniformPointSource(final long seed, final DoubleVolume range)
  {
    this.random = new Random(seed);
    this.range = range;
  }
  
  public UniformPointSource(final DoubleVolume range)
  {
    this(System.currentTimeMillis(), range);
  }
  
  @Override
  public Iterator<double[]> iterator()
  {
    return new UniformIterator();
  }
}