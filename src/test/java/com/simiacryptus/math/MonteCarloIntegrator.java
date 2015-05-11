package com.simiacryptus.math;

import java.util.Random;

import com.google.common.base.Function;

public class MonteCarloIntegrator
{
  public Function<double[], Double> function;
  public Random random = new Random();
  public double[][] range;
  public int sampleCount = 1000;
  
  public double[] getNextPoint()
  {
    final double[] point = new double[this.range.length];
    this.getNextPoint(point);
    return point;
  }
  
  public void getNextPoint(final double[] point)
  {
    for (int dimension = 0; dimension < this.range.length; dimension++)
    {
      final double[] dimRange = this.range[dimension];
      point[dimension] = dimRange[0] + this.random.nextDouble() * (dimRange[1] - dimRange[0]);
    }
  }
  
  public double getVolume()
  {
    double volume = 1.;
    for (final double[] dimRange : this.range)
    {
      volume *= dimRange[1] - dimRange[0];
    }
    return volume;
  }
  
  public double integrate()
  {
    double sum = 0.;
    final double[] point = this.getNextPoint();
    for (int sample = 0; sample < this.sampleCount; sample++)
    {
      this.getNextPoint(point);
      sum += this.function.apply(point);
    }
    return getVolume() * sum / this.sampleCount;
  }
  
}
