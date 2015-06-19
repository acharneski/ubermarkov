package com.simiacryptus.collections;

import java.util.Collection;
import java.util.Iterator;

import com.google.common.collect.Iterators;
import com.simiacryptus.lang.NotImplementedException;

@SuppressWarnings("deprecation")
public class CompositeCollection<T> implements Collection<T>
{
  private final Collection<T>[] children;
  
  public CompositeCollection(final Collection<T>... inputs)
  {
    this.children = inputs;
  }
  
  @Override
  public boolean add(final T e)
  {
    throw new NotImplementedException();
  }
  
  @Override
  public boolean addAll(final Collection<? extends T> c)
  {
    throw new NotImplementedException();
  }
  
  @Override
  public void clear()
  {
    throw new NotImplementedException();
  }
  
  @Override
  public boolean contains(final Object o)
  {
    throw new NotImplementedException();
  }
  
  @Override
  public boolean containsAll(final Collection<?> c)
  {
    throw new NotImplementedException();
  }
  
  @Override
  public boolean isEmpty()
  {
    throw new NotImplementedException();
  }
  
  @Override
  public Iterator<T> iterator()
  {
    @SuppressWarnings("unchecked")
    final Iterator<T>[] inputs = new Iterator[this.children.length];
    for (int i = 0; i < this.children.length; i++)
    {
      inputs[i] = this.children[i].iterator();
    }
    return Iterators.concat(inputs);
  }
  
  @Override
  public boolean remove(final Object o)
  {
    throw new NotImplementedException();
  }
  
  @Override
  public boolean removeAll(final Collection<?> c)
  {
    throw new NotImplementedException();
  }
  
  @Override
  public boolean retainAll(final Collection<?> c)
  {
    throw new NotImplementedException();
  }
  
  @Override
  public int size()
  {
    int size = 0;
    for (final Collection<T> element : this.children)
    {
      size += element.size();
    }
    return size;
  }
  
  @Override
  public Object[] toArray()
  {
    final Object[] array = new Object[this.size()];
    int index = 0;
    for (final T i : this)
    {
      array[index++] = i;
    }
    return array;
  }
  
  @Override
  public <U> U[] toArray(final U[] a)
  {
    throw new NotImplementedException();
  }
}