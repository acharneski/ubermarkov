package com.simiacryptus.markov;

import java.util.NavigableMap;

public class MarkovNodeWrapper<T extends Comparable<T>> extends MarkovNode<T>
{
  private final MarkovNode<T> input;
  
  public MarkovNodeWrapper(final MarkovNode<T> input)
  {
    this.input = input;
  }
  
  @Override
  public MarkovNode<T> getChild(final T key)
  {
    return this.input.getChild(key);
  }
  
  @Override
  public NavigableMap<T, ? extends MarkovNode<T>> getChildren()
  {
    return this.input.getChildren();
  }
  
  @Override
  public MarkovNode<T> getFallback()
  {
    return this.input.getFallback();
  }
  
  @Override
  public NavigableMap<T, ? extends MarkovNode<T>> getFallbackChildren()
  {
    return this.input.getFallbackChildren();
  }
  
  @Override
  public T getKey()
  {
    return this.input.getKey();
  }
  
  @Override
  public MarkovModel<T> getModel()
  {
    return this.input.getModel();
  }
  
  @Override
  public int getNodeCount()
  {
    return this.input.getNodeCount();
  }
  
  @Override
  public MarkovNode<T> getParent()
  {
    return this.input.getParent();
  }
  
  @Override
  public MarkovPath<T> getPath()
  {
    return this.input.getPath();
  }
  
  @Override
  public int getWeight()
  {
    return this.input.getWeight();
  }
  
  @Override
  public void incrementWeight(final int delta)
  {
    this.input.incrementWeight(delta);
  }
  
  @Override
  public boolean isEquivalent(final MarkovNode<T> otherNode)
  {
    return this.input.isEquivalent(otherNode);
  }
  
  @Override
  public MarkovNode<T> removeChild(final MarkovNode<T> child)
  {
    return this.input.removeChild(child);
  }
  
  @Override
  public void setWeight(final int value)
  {
    this.input.setWeight(value);
  }
}