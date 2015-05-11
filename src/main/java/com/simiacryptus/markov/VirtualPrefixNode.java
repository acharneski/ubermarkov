package com.simiacryptus.markov;

import java.util.ArrayList;
import java.util.NavigableMap;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.simiacryptus.lang.NotImplementedException;

public final class VirtualPrefixNode<T extends Comparable<T>> extends MarkovNodeWrapper<T>
{
  private MarkovNode<T> parent;
  private T prefix;
  
  public VirtualPrefixNode(final T prefix, final MarkovNode<T> input, final MarkovNode<T> parent)
  {
    super(input);
    this.prefix = prefix;
    this.parent = parent;
  }
  
  @Override
  public MarkovNode<T> getChild(final T key)
  {
    return new VirtualPrefixNode<T>(this.prefix, super.getChild(key), this);
  }
  
  @Override
  public NavigableMap<T, ? extends MarkovNode<T>> getChildren()
  {
    return Maps.transformValues(super.getChildren(), (Function<MarkovNode<T>, MarkovNode<T>>) input -> new VirtualPrefixNode<T>(VirtualPrefixNode.this.prefix,
        input,
        VirtualPrefixNode.this));
  }
  
  @Override
  public MarkovNode<T> getFallback()
  {
    throw new NotImplementedException();
    // return super.getFallback();
  }
  
  @Override
  public NavigableMap<T, ? extends MarkovNode<T>> getFallbackChildren()
  {
    throw new NotImplementedException();
    // return super.getFallbackChildren();
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
  public int getNodeCount()
  {
    return super.getNodeCount();
  }
  
  @Override
  public MarkovNode<T> getParent()
  {
    return this.parent;
  }
  
  @Override
  public MarkovPath<T> getPath()
  {
    final ArrayList<T> list = new ArrayList<T>();
    list.add(this.prefix);
    list.addAll(super.getPath().path);
    return new MarkovPath<T>(list);
  }
  
  @Override
  public int getWeight()
  {
    return super.getWeight();
  }
  
  @Override
  public void incrementWeight(final int delta)
  {
    // super.incrementWeight(delta);
  }
  
  @Override
  public boolean isEquivalent(final MarkovNode<T> otherNode)
  {
    return super.isEquivalent(otherNode);
  }
  
  @Override
  public MarkovNode<T> removeChild(final MarkovNode<T> child)
  {
    throw new NotImplementedException();
    // return super.removeChild(child);
  }
  
  @Override
  public void setWeight(final int value)
  {
    throw new NotImplementedException();
    // super.setWeight(value);
  }
  
}