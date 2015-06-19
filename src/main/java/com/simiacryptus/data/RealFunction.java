package com.simiacryptus.data;

import com.simiacryptus.lang.Function;

public interface RealFunction extends Function<double[], double[]>
{
  public int inputDimension();
  public int outputDimension();
}