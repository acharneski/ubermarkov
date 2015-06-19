package com.simiacryptus.lang;

public interface Function<P, R>
{
  public R apply(P value);
}