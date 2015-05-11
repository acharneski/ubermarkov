package com.simiacryptus.util;

import java.util.Arrays;
import java.util.function.IntToDoubleFunction;
import java.util.function.LongToDoubleFunction;

public class ArrayUtil
{

  public static double[] toDouble(final int[] from, final IntToDoubleFunction f)
  {
    final double[] to = new double[from.length];
    Arrays.setAll(to, i -> f.applyAsDouble(from[i]));
    return to;
  }

  public static double[] toDouble(final long[] from, final LongToDoubleFunction f)
  {
    final double[] to = new double[from.length];
    Arrays.setAll(to, i -> f.applyAsDouble(from[i]));
    return to;
  }

}
