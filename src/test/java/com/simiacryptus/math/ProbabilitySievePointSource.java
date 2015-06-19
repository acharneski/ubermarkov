package com.simiacryptus.math;

import java.util.Iterator;
import java.util.Random;

import com.simiacryptus.lang.Function;

public final class ProbabilitySievePointSource implements Iterable<double[]>
{
  private final class ProbabilitySieveIterator implements Iterator<double[]>
  {
    private final Iterator<double[]> volumePointIterator;
    private final Random             iteratorRandom;
    
    private ProbabilitySieveIterator()
    {
      this.iteratorRandom = new Random(
          ProbabilitySievePointSource.this.random.nextLong());
      this.volumePointIterator = ProbabilitySievePointSource.this.volumePoints
          .iterator();
    }
    
    @Override
    public boolean hasNext()
    {
      return true;
    }
    
    @Override
    public double[] next()
    {
      double[] candidate;
      do
      {
        candidate = this.volumePointIterator.next();
      } while (this.iteratorRandom.nextDouble() > ProbabilitySievePointSource.this.sieveFunction
          .apply(candidate) / ProbabilitySievePointSource.this.max);
      return candidate;
    }
    
    @Override
    public void remove()
    {
      throw new IllegalArgumentException();
    }
  }
  
  private final Function<double[],Double>           sieveFunction;
  private final UniformPointSource volumePoints;
  private final Random             random;
  private final double             max;
  
  public ProbabilitySievePointSource(final long seed,
      final UniformPointSource volumePoints, final Function<double[],Double> sieveFunction,
      final double max)
  {
    this.max = max;
    this.random = new Random(seed);
    this.sieveFunction = sieveFunction;
    this.volumePoints = volumePoints;
  }
  
  public ProbabilitySievePointSource(final long seed,
      final UniformPointSource volumePoints, final GaussianPDF sieveFunction)
  {
    this(seed, volumePoints, sieveFunction, sieveFunction
        .apply(new double[] { sieveFunction.mean }));
  }
  
  public ProbabilitySievePointSource(final UniformPointSource volumePoints,
      final Function<double[],Double> sieveFunction, final double max)
  {
    this(System.currentTimeMillis(), volumePoints, sieveFunction, max);
  }
  
  public ProbabilitySievePointSource(final UniformPointSource volumePoints,
      final GaussianPDF sieveFunction)
  {
    this(System.currentTimeMillis(), volumePoints, sieveFunction);
  }
  
  @Override
  public Iterator<double[]> iterator()
  {
    return new ProbabilitySieveIterator();
  }
}