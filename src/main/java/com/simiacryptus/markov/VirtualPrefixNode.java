package com.simiacryptus.markov;

import java.util.ArrayList;
import java.util.NavigableMap;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.simiacryptus.lang.NotImplementedException;

public final class VirtualPrefixNode<T extends Comparable<T>> extends MarkovNodeWrapper<T>
{
  private T prefix;
  private MarkovNode<T> parent;

  public VirtualPrefixNode(T prefix, MarkovNode<T> input, MarkovNode<T> parent)
  {
    super(input);
    this.prefix = prefix;
    this.parent = parent;
  }

  @Override
  public MarkovNode<T> getChild(T key)
  {
    return new VirtualPrefixNode<T>(prefix, super.getChild(key), this);
  }

  @Override
  public NavigableMap<T, ? extends MarkovNode<T>> getChildren()
  {
    return Maps.transformValues(super.getChildren(), new Function<MarkovNode<T>, MarkovNode<T>>() {
      @Override
      @Nullable
      public MarkovNode<T> apply(@Nullable MarkovNode<T> input)
      {
        return new VirtualPrefixNode<T>(prefix, input, VirtualPrefixNode.this);
      }
    });
  }

  @Override
  public int getWeight()
  {
    return super.getWeight();
  }

  @Override
  public MarkovNode<T> getFallback()
  {
    throw new NotImplementedException();
    //return super.getFallback();
  }

  @Override
  public NavigableMap<T, ? extends MarkovNode<T>> getFallbackChildren()
  {
    throw new NotImplementedException();
    //return super.getFallbackChildren();
  }

  @Override
  public T getKey()
  {
    return super.getKey();
  }

  @Override
  public MarkovModel<T> getModel()
  {
    return super.getModel();
  }

  @Override
  public MarkovNode<T> getParent()
  {
    return parent;
  }

  @Override
  public void incrementWeight(int delta)
  {
    //super.incrementWeight(delta);
  }

  @Override
  public MarkovNode<T> removeChild(MarkovNode<T> child)
  {
    throw new NotImplementedException();
    //return super.removeChild(child);
  }

  @Override
  public void setWeight(int value)
  {
    throw new NotImplementedException();
    //super.setWeight(value);
  }

  @Override
  public int getNodeCount()
  {
    return super.getNodeCount();
  }

  @Override
  public MarkovPath<T> getPath()
  {
    ArrayList<T> list = new ArrayList<T>();
    list.add(prefix);
    list.addAll(super.getPath().path);
    return new MarkovPath<T>(list);
  }

  @Override
  public boolean isEquivalent(MarkovNode<T> otherNode)
  {
    return super.isEquivalent(otherNode);
  }
  
  
}