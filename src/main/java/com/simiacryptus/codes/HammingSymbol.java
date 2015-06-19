package com.simiacryptus.codes;

public class HammingSymbol<T extends Comparable<T>> implements
    Comparable<HammingSymbol<T>>
{
  
  public final T   key;
  public final int count;
  
  public HammingSymbol(final int count, final T key)
  {
    this.count = count;
    this.key = key;
  }
  
  @Override
  public int compareTo(final HammingSymbol<T> o)
  {
    return this.key.compareTo(o.key);
  }
  
}