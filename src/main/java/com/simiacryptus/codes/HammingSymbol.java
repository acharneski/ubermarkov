package com.simiacryptus.codes;

public class HammingSymbol<T>
{

  public final int count;
  public final T key;

  public HammingSymbol(final int count, final T key)
  {
    this.count = count;
    this.key = key;
  }

}