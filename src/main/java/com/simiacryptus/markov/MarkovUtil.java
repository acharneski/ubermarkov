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
  
  private static final WeakHashMap<MarkovNode<?>, HammingCode<?>> cache = new WeakHashMap<MarkovNode<?>, HammingCode<?>>();
  
  public static <T extends Comparable<T>> HammingCode<T> getHammingCode(final MarkovNode<T> node)
  {
    @SuppressWarnings("unchecked")
    HammingCode<T> code = (HammingCode<T>) MarkovUtil.cache.get(node);
    if (null != code && code.totalWeight() == node.getWeight())
      return code;
    else
    {
      final TreeSet<HammingSymbol<T>> symbols = new TreeSet<HammingSymbol<T>>(Maps.transformEntries(node.getChildren(),
          (EntryTransformer<T, MarkovNode<T>, HammingSymbol<T>>) (key, value) -> new HammingSymbol<T>(value.getWeight(), key)).values());
      code = new HammingCode<T>(symbols);
      MarkovUtil.cache.put(node, code);
    }
    return code;
  }
  
  public static <T extends Comparable<T>> List<MarkovNode<T>> getLeaves(final MarkovNode<T> node)
  {
    final ArrayList<MarkovNode<T>> list = new ArrayList<MarkovNode<T>>();
    if (0 == node.getChildren().size() && 0 < node.getWeight())
    {
      list.add(node);
    }
    for (final MarkovNode<T> child : node.getChildren().values())
    {
      list.addAll(MarkovUtil.getLeaves(child));
    }
    return list;
  }
  
  public static <T extends Comparable<T>> List<MarkovNode<T>> getNodes(final MarkovNode<T> node, final int level)
  {
    final ArrayList<MarkovNode<T>> list = new ArrayList<MarkovNode<T>>();
    if (level == 0)
    {
      list.add(node);
    } else
    {
      for (final MarkovNode<T> child : node.getChildren().values())
      {
        list.addAll(MarkovUtil.getNodes(child, level - 1));
      }
    }
    return list;
  }
  
}
