package com.simiacryptus.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.linear.MatrixUtils;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.AtomicDouble;

public class Util
{

  public static <T> ArrayList<T> flatten(final List<List<T>> asList)
  {
    final ArrayList<T> list = new ArrayList<T>();
    for (final List<T> list1 : asList)
    {
      list.addAll(list1);
    }
    return list;
  }

  public static String getId(final Object obj)
  {
    return obj.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(obj));
  }

  public static <K, V> Function<K, V> memoize(final Function<K, V> f)
  {
    final LoadingCache<K, V> cache = CacheBuilder.newBuilder().build(new CacheLoader<K, V>() {
      @Override
      public V load(final K key) throws Exception
      {
        return f.apply(key);
      }
    });
    return k -> {
      V v;
      try
      {
        v = cache.get(k);
      } catch (final ExecutionException e)
      {
        throw new RuntimeException(e);
      }
      return v;
    };
  }

  public static <K, V> LoadingCache<K, V> newLazyMap(final Function<K, V> f)
  {
    return CacheBuilder.newBuilder().build(new CacheLoader<K, V>() {
      @Override
      public V load(final K key) throws Exception
      {
        return f.apply(key);
      }
    });
  }

  public static double[] normalize(final double[] probs)
  {
    final double factor = Arrays.stream(probs).sum();
    return Util.transform(probs, x -> x / factor);
  }
  
  public static double[] normalize2(final double[] probs)
  {
    final double factor = Math.sqrt(Arrays.stream(probs).map(x -> x * x).sum());
    return Util.transform(probs, x -> x / factor);
  }

  public static double[][] normalizeRowSum(final double[][] data)
  {
    return Util.transform(data, double[].class, x -> {
      // double normalizationFactor = Math.sqrt(Arrays.stream(x).map(v -> v * v).sum());
      final double normalizationFactor = Arrays.stream(x).sum();
      if (0. >= normalizationFactor) return x;
      return Util.transform(x, i -> i / normalizationFactor);
    });
  }

  public static double percentile(final double[] array, final double v)
  {
    double i = Arrays.binarySearch(array, v);
    if (i < 0)
    {
      i = -(i + 0.5);
    }
    final double f = i / array.length;
    final double tol = 1e-8;
    if (f < tol) return tol;
    if (f > 1. - tol) return 1. - tol;
    return f;
  }

  public static String print(final Consumer<PrintStream> c)
  {
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    final PrintStream out = new PrintStream(byteArrayOutputStream);
    try
    {
      c.accept(out);
    } finally
    {
      out.close();
    }
    return byteArrayOutputStream.toString();
  }

  public static double product(final DoubleStream stream)
  {
    return Util.product(stream, v -> {
      if (Double.isNaN(v)) return 1.;
      if (Double.isInfinite(v)) return 1.;
      return v;
    });
  }

  public static double product(final DoubleStream stream, final UnivariateFunction filter)
  {
    final Supplier<AtomicDouble> supplier = () -> new AtomicDouble(1);
    final ObjDoubleConsumer<AtomicDouble> accum = (a, b) -> {
      double expect;
      double update;
      do
      {
        expect = a.get();
        update = filter.value(filter.value(expect) * filter.value(b));
      } while (!a.compareAndSet(expect, update));
    };
    final BiConsumer<AtomicDouble, AtomicDouble> combiner = (a, b) -> {
      double expect;
      double update;
      do
      {
        expect = a.get();
        update = filter.value(filter.value(expect) * filter.value(b.get()));
      } while (!a.compareAndSet(expect, update));
    };
    // The following line should be allowed to die. If we allowed that, however, it would not get to suffer anymore.
    final double product = stream.collect(supplier, accum, combiner).get();
    return product;
  }

  public static double[] transform(final double[] from, final UnivariateFunction f)
  {
    final double[] to = new double[from.length];
    Arrays.setAll(to, i -> f.value(from[i]));
    return to;
  }

  public static <F, T> T[] transform(final F[] from, final Class<T> object, final Function<F, T> f)
  {
    @SuppressWarnings("unchecked")
    final T[] to = (T[]) Array.newInstance(object, from.length);
    Arrays.setAll(to, (IntFunction<T>) i -> f.apply(from[i]));
    return to;
  }

  public static <F> double[] transform(final F[] from, final ToDoubleFunction<F> f)
  {
    final double[] to = new double[from.length];
    Arrays.setAll(to, i -> f.applyAsDouble(from[i]));
    return to;
  }

  public static double[][] transpose(final double[][] data)
  {
    return MatrixUtils.createRealMatrix(data).transpose().getData();
  }

  public static double[] unwrap(final Double[] values)
  {
    final double[] result = new double[values.length];
    for (int i = 0; i < values.length; i++)
    {
      result[i] = values[i];
    }
    return result;
  }

  public static double[] unwrap(final List<Double> values)
  {
    final double[] result = new double[values.size()];
    for (int i = 0; i < values.size(); i++)
    {
      result[i] = values.get(i);
    }
    return result;
  }

  public static Double[] wrap(final double[] values)
  {
    final Double[] result = new Double[values.length];
    for (int i = 0; i < values.length; i++)
    {
      result[i] = values[i];
    }
    return result;
  }

  public static Integer[] wrap(final int[] values)
  {
    final Integer[] result = new Integer[values.length];
    for (int i = 0; i < values.length; i++)
    {
      result[i] = values[i];
    }
    return result;
  }

}
