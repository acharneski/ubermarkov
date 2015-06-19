package com.simiacryptus.math;

import java.util.Random;

import com.simiacryptus.binary.Bits;

public class SCRandom
{
  final ThreadLocal<Random> random = new ThreadLocal<Random>() {
                                     
                                     @Override
                                     protected Random initialValue()
                                     {
                                       return new Random(
                                           SCRandom.this.getSeed());
                                     }
                                     
                                   };
  private long              seed;
  
  public SCRandom()
  {
    this(System.nanoTime());
  }
  
  public SCRandom(final long seed)
  {
    this.setSeed(seed);
  }
  
  public <T> T choose(final T[] values)
  {
    return values[this.random.get().nextInt(values.length)];
  }
  
  public long getSeed()
  {
    return this.seed;
  }
  
  public Bits nextBits()
  {
    return new Bits(this.nextLong());
  }
  
  public long nextLong()
  {
    final Random r = this.random.get();
    return this.random.get().nextLong() >> r.nextInt(62);
  }
  
  public Random random()
  {
    return this.random.get();
  }
  
  public void reset()
  {
    this.setSeed(this.getSeed());
  }
  
  public void setSeed(final long seed)
  {
    this.seed = seed;
    this.random.set(new Random(seed));
  }
  
}
