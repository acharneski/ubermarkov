package com.simiacryptus.markov;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public final class DataNode<T extends Comparable<T>> extends MarkovNode<T>
{
  
  private int                           weight           = 0;
  private int                           nodeCount        = 0;
  private final MarkovPath<T>           path;
  private final DataNode<T>           parent;
  private volatile DataNode<T>          fallback;
  private final TreeMap<T, DataNode<T>> children         = new TreeMap<T, DataNode<T>>();
  private final TreeMap<T, DataNode<T>> fallbackChildren = new TreeMap<T, DataNode<T>>();
  private MarkovModel<T>                model;
  
  @SuppressWarnings("unchecked")
  DataNode(final MarkovModel<T> model)
  {
    this.model = model;
    this.parent = null;
    this.fallback = this;
    this.path = new MarkovPath<T>();
  }
  
  protected DataNode(final DataNode<T> parent, final T key)
  {
    super();
    assert null != parent;
    assert null != key;
    this.model = parent.getModel();
    this.parent = parent;
    
    final List<T> list = null != parent ? new ArrayList<T>(
        parent.getPath().path) : new ArrayList<T>();
    if (null != key)
    {
      list.add(key);
    }
    this.path = new MarkovPath<T>(list);
    // initFallbackIndex();
  }
  
  protected void add(final MarkovNode<T> source)
  {
    final NavigableMap<T, ? extends MarkovNode<T>> sourceChildren = source
        .getChildren();
    if (0 == sourceChildren.size())
    {
      this.incrementWeight(source.getWeight());
    }
    else
    {
      for (final Entry<T, ? extends MarkovNode<T>> e : sourceChildren
          .entrySet())
      {
        final DataNode<T> targetChild = (DataNode<T>) this.newChild(e.getKey());
        final MarkovNode<T> sourceChild = e.getValue();
        targetChild.add(sourceChild);
      }
    }
  }
  
  protected void addFallbackChild(final T key, final DataNode<T> node)
  {
    synchronized (this.fallbackChildren)
    {
      this.fallbackChildren.put(key, node);
      if (this.getModel().dedupPrefix && 1 == this.fallbackChildren.size())
      {
        final DataNode<T> prefixedNode = this.fallbackChildren.values()
            .iterator().next();
        prefixedNode.add(this);
      }
    }
  }
  
  @Override
  public MarkovNode<T> getChild(final T key)
  {
    if (this.isPrefixTruncatable()) { return new VirtualPrefixNode(this
        .getPath().get(0), this.getFallback().getChild(key), this); }
    MarkovNode<T> child = this.getChildren().get(key);
    if (null == child)
    {
      synchronized (this.getRoot())
      {
        child = this.getChildren().get(key);
        if (null == child)
        {
          child = this.newChild(key);
        }
      }
    }
    return child;
  }
  
  @Override
  public NavigableMap<T, ? extends MarkovNode<T>> getChildren()
  {
    if (this.isPrefixTruncatable())
    {
      return Maps.transformValues(this.getFallback().getChildren(),
          new Function<MarkovNode<T>, MarkovNode<T>>() {
            @Override
            @Nullable
            public MarkovNode<T> apply(@Nullable final MarkovNode<T> input)
            {
              final DataNode<T> parent = DataNode.this;
              return new VirtualPrefixNode(
                  parent.getPath().get(0),
                  input,
                  parent);
            }
          });
    }
    else
    {
      return Maps.unmodifiableNavigableMap(Maps.transformValues(this.children,
          new Function<DataNode<T>, MarkovNode<T>>() {
            @Override
            @Nullable
            public MarkovNode<T> apply(@Nullable final DataNode<T> input)
            {
              return input;
            }
          }));
    }
  }
  
  @Override
  public DataNode<T> getFallback()
  {
    if (null == this.fallback)
    {
      synchronized (this)
      {
        if (null == this.fallback)
        {
          MarkovPath<T> path = getPath();
          if(null == path || 0 == path.size())
          {
            return null;
          }
          else if(1 == path.size())
          {
            this.fallback = getParent();
          }
          else
          {
            final MarkovNode<T> f = this.getParent().getFallback().getChild(this.getKey());
            if (f instanceof DataNode)
            {
              this.fallback = (DataNode<T>) f;
            }
          }
        }
      }
    }
    return this.fallback;
  }
  
  @Override
  public NavigableMap<T, ? extends MarkovNode<T>> getFallbackChildren()
  {
    return Maps.unmodifiableNavigableMap(this.fallbackChildren);
  }
  
  @Override
  public T getKey()
  {
    final MarkovPath<T> path = this.getPath();
    return 0 == path.size() ? null : path.get(path.size() - 1);
  }
  
  @Override
  public MarkovModel<T> getModel()
  {
    return this.model;
  }
  
  @Override
  public int getNodeCount()
  {
    return this.nodeCount;
  }
  
  @Override
  public DataNode<T> getParent()
  {
    return this.parent;
  }
  
  @Override
  public MarkovPath<T> getPath()
  {
    return this.path;
  }
  
  @Override
  public int getWeight()
  {
    return this.weight;
  }
  
  protected void incrementNodeCount()
  {
    this.nodeCount++;
    if (null != this.getParent())
    {
      ((DataNode<T>) this.getParent()).incrementNodeCount();
    }
  }
  
  @Override
  public void incrementWeight(final int delta)
  {
    synchronized (this)
    {
      this.weight = this.weight + delta;
    }
    if (null != this.parent)
    {
      this.parent.incrementWeight(delta);
    }
  }
  
  protected void initFallbackIndex()
  {
    final T key = this.getKey();
    for (final MarkovNode<T> uncle : this.getParent()
        .getFallbackChildren().values())
    {
      final MarkovNode<T> child = uncle.getChildren().get(key);
      if (null != child && child instanceof DataNode)
      {
        this.fallbackChildren.put(uncle.getKey(), (DataNode<T>) child);
      }
    }
  }
  
  protected boolean isPrefixTruncatable()
  {
    if (!this.getModel().dedupPrefix) { return false; }
    final MarkovNode<T> fallback = this.getFallback();
    if (null == fallback) { return false; }
    if (this == fallback) { return false; }
    return 1 == fallback.getFallbackChildren().size();
  }
  
  protected MarkovNode<T> newChild(final T key)
  {
    final DataNode<T> child = new DataNode<T>(this, key);
    final MarkovNode<T> oldChild = this.children.put(key, child);
    if (null != oldChild)
    {
      return oldChild;
    }
    else
    {
      child.initFallbackIndex();
      this.incrementNodeCount();
      final T prefix = child.getPath().get(0);
      final MarkovNode<T> newFallback = child.getFallback();
      if (newFallback instanceof DataNode)
      {
        ((DataNode<T>) newFallback).addFallbackChild(prefix, child);
      }
      return child;
    }
  }
  
  @Override
  public MarkovNode<T> removeChild(final MarkovNode<T> child)
  {
    final MarkovNode<T> childCheck = this.children.get(child.getKey());
    if (childCheck != child) { return null; }
    return this.children.remove(child.getKey());
  }
  
  @Override
  public void setWeight(final int value)
  {
    this.weight = value;
  }
  
  protected String toStringTree()
  {
    final StringBuffer sb = new StringBuffer();
    sb.append(this.getWeight());
    for (final Entry<T, DataNode<T>> e : this.children.entrySet())
    {
      sb.append("\n  ");
      sb.append(String.format("\"%s\" = %s", MarkovPath.format(e.getKey()), e.getValue()
          .toStringTree().replaceAll("\n", "\n  ")));
    }
    return sb.toString();
  }
  
}
