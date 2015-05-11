package com.simiacryptus.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;

public class CountCollection<T, C extends Map<T, AtomicInteger>>
{

  protected final C map;

  public CountCollection(final C collection)
  {
    super();
    this.map = collection;
  }

  public int add(final T bits)
  {
    return this.getCounter(bits).incrementAndGet();
  }

  public int add(final T bits, final int count)
  {
    return this.getCounter(bits).addAndGet(count);
  }

  protected int count(final T key)
  {
    final AtomicInteger counter = this.map.get(key);
    if (null == counter) return 0;
    return counter.get();
  }

  private AtomicInteger getCounter(final T bits)
  {
    AtomicInteger counter = this.map.get(bits);
    if (null == counter)
    {
      counter = new AtomicInteger();
      this.map.put(bits, counter);
    }
    return counter;
  }

  public List<T> getList()
  {
    final ArrayList<T> list = new ArrayList<T>();
    for (final Entry<T, AtomicInteger> e : this.map.entrySet())
    {
      for (int i = 0; i < e.getValue().get(); i++)
      {
        list.add(e.getKey());
      }
    }
    return list;
  }

  public Map<T, Integer> getMap()
  {
    return Maps.transformEntries(this.map, (EntryTransformer<T, AtomicInteger, Integer>) (key, value) -> value.get());
  }

}