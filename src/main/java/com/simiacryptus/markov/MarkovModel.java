package com.simiacryptus.markov;

import java.util.List;

public class MarkovModel<T extends Comparable<T>>
{
  final MarkovNode<T> root         = new DataNode<T>(this);
  final CharaterCoder charCoder    = new CharaterCoder();
  final int           depth;
  boolean             isNormalized = false;
  public boolean      dedupPrefix  = false;
  
  MarkovModel(final int depth)
  {
    super();
    this.depth = depth;
  }
  
  public void add(final List<T> sequence, long marker)
  {
    root.add(new MarkovPath<T>(sequence), 1);
  }
  
  public void add(final List<T> sequence, final int value)
  {
    root.add(new MarkovPath<T>(sequence), value);
  }
  
}