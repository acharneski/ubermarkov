package com.simiacryptus.markov;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.WeakHashMap;

import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;
import com.simiacryptus.codes.HammingCode;
import com.simiacryptus.codes.HammingSymbol;

public class MarkovUtil
{

  public static <T extends Comparable<T>> List<MarkovNode<T>> getLeaves(MarkovNode<T> node)
  {
    final ArrayList<MarkovNode<T>> list = new ArrayList<MarkovNode<T>>();
    if (0 == node.getChildren().size() && 0 < node.getWeight())
    {
      list.add(node);
    }
    for (final MarkovNode<T> child : node.getChildren().values())
    {
      list.addAll(getLeaves(child));
    }
    return list;
  }

  public static <T extends Comparable<T>> List<MarkovNode<T>> getNodes(MarkovNode<T> node, final int level)
  {
    final ArrayList<MarkovNode<T>> list = new ArrayList<MarkovNode<T>>();
    if (level == 0)
    {
      list.add(node);
    }
    else
    {
      for (final MarkovNode<T> child : node.getChildren().values())
      {
        list.addAll(getNodes(child, level - 1));
      }
    }
    return list;
  }

  private static final WeakHashMap<MarkovNode<?>, HammingCode<?>> cache = new WeakHashMap<MarkovNode<?>, HammingCode<?>>();
  public static <T extends Comparable<T>> HammingCode<T> getHammingCode(MarkovNode<T> node)
  {
    @SuppressWarnings("unchecked")
    HammingCode<T> code = (HammingCode<T>) cache.get(node);
    if(null != code && code.totalWeight() == node.getWeight())
    {
      return code;
    }
    else
    {
      TreeSet<HammingSymbol<T>> symbols = new TreeSet<HammingSymbol<T>>(Maps
          .transformEntries(node.getChildren(),
              new EntryTransformer<T, MarkovNode<T>, HammingSymbol<T>>() {
            @Override
            public HammingSymbol<T> transformEntry(final T key,
                final MarkovNode<T> value)
                {
              return new HammingSymbol<T>(value.getWeight(), key);
                }
          }).values());
      code = new HammingCode<T>(symbols);
      cache.put(node, code);
    }
    return code;
  }
  
}
