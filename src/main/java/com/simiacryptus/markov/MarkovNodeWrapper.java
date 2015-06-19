package com.simiacryptus.markov;

import java.util.NavigableMap;

public class MarkovNodeWrapper<T extends Comparable<T>> extends MarkovNode<T>
{
  private final MarkovNode<T> input;
  
  public MarkovNodeWrapper(MarkovNode<T> input)
  {
    this.input = input;
  }
  
  @Override
  public MarkovNode<T> getChild(T key)
  {
    return input.getChild(key);
  }
  
  @Override
  public NavigableMap<T, ? extends MarkovNode<T>> getChildren()
  {
    return input.getChildren();
  }
  
  @Override
  public int getWeight()
  {
    return input.getWeight();
  }
  
  @Override
  public MarkovNode<T> getFallback()
  {
    return input.getFallback();
  }
  
  @Override
  public NavigableMap<T, ? extends MarkovNode<T>> getFallbackChildren()
  {
    return input.getFallbackChildren();
  }
  
  @Override
  public T getKey()
  {
    return input.getKey();
  }
  
  @Override
  public MarkovModel<T> getModel()
  {
    return input.getModel();
  }
  
  @Override
  public MarkovNode<T> getParent()
  {
    return input.getParent();
  }
  
  @Override
  public void incrementWeight(int delta)
  {
    input.incrementWeight(delta);
  }
  
  @Override
  public MarkovNode<T> removeChild(MarkovNode<T> child)
  {
    return input.removeChild(child);
  }
  
  @Override
  public void setWeight(int value)
  {
    input.setWeight(value);
  }
  
  @Override
  public int getNodeCount()
  {
    return input.getNodeCount();
  }
  
  @Override
  public MarkovPath<T> getPath()
  {
    return input.getPath();
  }
  
  @Override
  public boolean isEquivalent(MarkovNode<T> otherNode)
  {
    return input.isEquivalent(otherNode);
  }
}