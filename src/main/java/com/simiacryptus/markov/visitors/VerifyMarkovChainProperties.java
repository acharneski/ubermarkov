package com.simiacryptus.markov.visitors;

import java.util.Collection;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;
import com.simiacryptus.markov.MarkovNode;
import com.simiacryptus.markov.MarkovNodeType;
import com.simiacryptus.markov.MarkovVisitor;

public final class VerifyMarkovChainProperties extends MarkovVisitor<Character>
{
  public static void run(final MarkovNode<Character> markovChain)
  {
    //new VerifyMarkovChainProperties().visitUp(markovChain);
  }
  
  public boolean verifyCountSums = true;
  
  public VerifyMarkovChainProperties()
  {
    super();
  }
  
  protected void verifyFallbackCounts(final MarkovNode<Character> node)
  {
    if (!this.verifyCountSums) { return; }
    final TreeMap<Character, AtomicInteger> subCounts = new TreeMap<Character, AtomicInteger>(
        Maps.transformEntries(
            node.getChildren(),
            new EntryTransformer<Character, MarkovNode<Character>, AtomicInteger>() {
              @Override
              public AtomicInteger transformEntry(
                  @Nullable final Character key,
                  @Nullable final MarkovNode<Character> value)
              {
                return new AtomicInteger(value.getWeight());
              }
            }));
    final Collection<? extends MarkovNode<Character>> forwardNodes = node.getFallbackChildren().values();
    for (final MarkovNode<Character> follower : forwardNodes)
    {
      for (final MarkovNode<Character> child : follower.getChildren().values())
      {
        final AtomicInteger count = subCounts.get(child.getKey());
        final int childCount = child.getWeight();
        count.addAndGet(-childCount);
      }
    }
//    for (final Entry<Character, AtomicInteger> e : subCounts.entrySet())
//    {
//      if (e.getValue().get() != 0) { throw new RuntimeException(String.format(
//          "%s has invalid cyclical count: %s",
//          node.getChildren().get(e.getKey()).getPath().toString(), e.getValue()
//              .get())); }
//    }
  }
  
  private void verifyLeafChildren(final MarkovNode<Character> node)
  {
//    for (final MarkovChain<Character> child : node.getChildren().values())
//    {
//      final MarkovNodeType childType = MarkovNodeType.getType(child);
//      if (MarkovNodeType.Leaf != childType) { throw new RuntimeException(
//          String.format("Direct child of twig %s is %s: %s", node, childType,
//              child)); }
//    }
//    for (final MarkovChain<Character> child : node.getFallbackChildren()
//        .values())
//    {
//      final MarkovNodeType childType = MarkovNodeType.getType(child);
//      if (MarkovNodeType.Leaf != childType) { throw new RuntimeException(
//          String.format("Fallback child of twig %s is %s: %s", node, childType,
//              child)); }
//    }
  }
  
  private void verifyNonTerminal(final MarkovNode<Character> node)
  {
    if (this.verifyCountSums)
    {
      int count = node.getWeight();
      for (final MarkovNode<Character> child : node.getChildren().values())
      {
        count -= child.getWeight();
      }
      if (0 < count) { throw new RuntimeException(String.format(
          "Terminal count %s for node %s", count, node)); }
    }
    if (this.verifyCountSums)
    {
      int count = node.getWeight();
      for (final MarkovNode<Character> child : node.getFallbackChildren()
          .values())
      {
        count -= child.getWeight();
      }
      if (0 < count) { throw new RuntimeException(String.format(
          "Fallback terminal count %s for node %s", count, node)); }
    }
  }
  
  @Override
  public void visit(final MarkovNode<Character> node)
  {
    final MarkovNodeType type = MarkovNodeType.getType(node);
    switch (type) {
    case Root:
      this.verifyFallbackCounts(node);
      this.verifyNonTerminal(node);
      break;
    case Branch:
      this.verifyFallbackCounts(node);
      this.verifyNonTerminal(node);
      break;
    case Twig:
      this.verifyLeafChildren(node);
      this.verifyNonTerminal(node);
      break;
    case Leaf:
      break;
    default:
      break;
    }
  }
}