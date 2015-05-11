package com.simiacryptus.markov;

import java.util.List;

public class MarkovModel<T extends Comparable<T>>
{
  final CharaterCoder charCoder = new CharaterCoder();
  public boolean dedupPrefix = true;
  final int depth;
  boolean isNormalized = false;
  final MarkovNode<T> root = new DataNode<T>(this);
  
  MarkovModel(final int depth)
  {
    super();
    this.depth = depth;
  }
  
  public void add(final List<T> sequence, final int value)
  {
    this.root.add(new MarkovPath<T>(sequence), value);
  }
  
  public void add(final List<T> sequence, final long marker)
  {
    this.root.add(new MarkovPath<T>(sequence), 1);
  }
  
}