package com.simiacryptus.markov;

import java.util.NavigableMap;
import java.util.Map.Entry;

public abstract class MarkovNode<T extends Comparable<T>>
{
  
  public abstract MarkovNode<T> getChild(T key);
  
  public abstract NavigableMap<T, ? extends MarkovNode<T>> getChildren();
  
  public abstract int getWeight();
  
  public abstract MarkovNode<T> getFallback();
  
  public abstract NavigableMap<T, ? extends MarkovNode<T>> getFallbackChildren();
  
  public abstract T getKey();
  
  public abstract MarkovModel<T> getModel();
  
  public abstract MarkovNode<T> getParent();
  
  public abstract void incrementWeight(int delta);
  
  public abstract MarkovNode<T> removeChild(MarkovNode<T> child);
  
  public abstract void setWeight(int value);
  
  public abstract int getNodeCount();
  
  public abstract MarkovPath<T> getPath();
  
  public void add(final MarkovPath<T> sequence, final int value)
  {
    MarkovNode<T> child = this.getChild(sequence);
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
    }
    else
    {
      return this;
    }
  }
  
  public MarkovNode<T> getRoot()
  {
    return null == this.getParent() ? this : this.getParent().getRoot();
  }
  
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    builder.append(" [getWeight()=");
    builder.append(getWeight());
    builder.append(", getPath()=");
    builder.append(getPath());
    builder.append("]");
    return builder.toString();
  }
  
  public boolean isEquivalent(final MarkovNode<T> b)
  {
    return isEquivalent(this, b);
  }
  
  public static <T extends Comparable<T>> boolean isEquivalent(MarkovNode<T> a, final MarkovNode<T> b)
  {
    if (a == b)
    {
      return true;
    }
    if (b == null)
    {
      return false;
    }
    if (a.getWeight() != b.getWeight())
    {
      return false;
    }
    final NavigableMap<T, ? extends MarkovNode<T>> thisChildren = a.getChildren();
    final NavigableMap<T, ? extends MarkovNode<T>> otherChildren = b.getChildren();
    if (thisChildren == null)
    {
      if (otherChildren != null)
      {
        return false;
      }
    }
    else
    {
      for (final Entry<T, ? extends MarkovNode<T>> k : thisChildren.entrySet())
      {
        final MarkovNode<T> otherNode = otherChildren.get(k.getKey());
        if (null == otherNode)
        {
          return false;
        }
        if (!k.getValue().isEquivalent(otherNode))
        {
          return false;
        }
      }
      for (final Entry<T, ? extends MarkovNode<T>> k : otherChildren.entrySet())
      {
        if (!thisChildren.containsKey(k.getKey()))
        {
          return false;
        }
      }
    }
    return true;
  }
  
}