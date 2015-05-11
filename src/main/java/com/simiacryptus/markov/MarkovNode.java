package com.simiacryptus.markov;

import java.util.Map.Entry;
import java.util.NavigableMap;

public abstract class MarkovNode<T extends Comparable<T>>
{
  
  public static <T extends Comparable<T>> boolean isEquivalent(final MarkovNode<T> a, final MarkovNode<T> b)
  {
    if (a == b)
      return true;
    if (b == null)
      return false;
    if (a.getWeight() != b.getWeight())
      return false;
    final NavigableMap<T, ? extends MarkovNode<T>> thisChildren = a.getChildren();
    final NavigableMap<T, ? extends MarkovNode<T>> otherChildren = b.getChildren();
    if (thisChildren == null)
    {
      if (otherChildren != null)
        return false;
    } else
    {
      for (final Entry<T, ? extends MarkovNode<T>> k : thisChildren.entrySet())
      {
        final MarkovNode<T> otherNode = otherChildren.get(k.getKey());
        if (null == otherNode)
          return false;
        if (!k.getValue().isEquivalent(otherNode))
          return false;
      }
      for (final Entry<T, ? extends MarkovNode<T>> k : otherChildren.entrySet())
      {
        if (!thisChildren.containsKey(k.getKey()))
          return false;
      }
    }
    return true;
  }
  
  public void add(final MarkovPath<T> sequence, final int value)
  {
    final MarkovNode<T> child = this.getChild(sequence);
    if (child.getPath().equals(sequence))
    {
      child.incrementWeight(value);
    }
  }
  
  public MarkovNode<T> getChild(final MarkovPath<T> sequence)
  {
    if (sequence.size() > 0)
    {
      final T key = sequence.path.get(0);
      final MarkovPath<T> remainder = sequence.subPath(1);
      final MarkovNode<T> child = this.getChild(key);
      return child.getChild(remainder);
    } else return this;
  }
  
  public abstract MarkovNode<T> getChild(T key);
  
  public abstract NavigableMap<T, ? extends MarkovNode<T>> getChildren();
  
  public abstract MarkovNode<T> getFallback();
  
  public abstract NavigableMap<T, ? extends MarkovNode<T>> getFallbackChildren();
  
  public abstract T getKey();
  
  public abstract MarkovModel<T> getModel();
  
  public abstract int getNodeCount();
  
  public abstract MarkovNode<T> getParent();
  
  public abstract MarkovPath<T> getPath();
  
  public MarkovNode<T> getRoot()
  {
    return null == this.getParent() ? this : this.getParent().getRoot();
  }
  
  public abstract int getWeight();
  
  public abstract void incrementWeight(int delta);
  
  public boolean isEquivalent(final MarkovNode<T> b)
  {
    return MarkovNode.isEquivalent(this, b);
  }
  
  public abstract MarkovNode<T> removeChild(MarkovNode<T> child);
  
  public abstract void setWeight(int value);
  
  @Override
  public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    builder.append(" [getWeight()=");
    builder.append(getWeight());
    builder.append(", getPath()=");
    builder.append(getPath());
    builder.append("]");
    return builder.toString();
  }
  
}