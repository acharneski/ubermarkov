package com.simiacryptus.math;

import com.google.common.base.Function;

public final class GaussianPDF implements Function<double[], Double>
{
  public int dimension = 0;
  public double mean = 0;
  public double stdDev = 1;
  
  @Override
  public Double apply(final double[] value)
  {
    final double x = value[this.dimension];
    return Math.pow(Math.E, -(Math.pow(x - this.mean, 2) / (2 * this.stdDev * this.stdDev))) / (Math.sqrt(2 * Math.PI) * this.stdDev);
  }
}