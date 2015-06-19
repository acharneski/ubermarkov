package com.simiacryptus.markov.visitors;

import java.util.TreeMap;

import com.simiacryptus.markov.MarkovNode;
import com.simiacryptus.markov.MarkovVisitor;

public final class RemoveZeroTerminals extends MarkovVisitor<Character>
{
  
  public static void run(final MarkovNode<Character> markovChain)
  {
    new RemoveZeroTerminals().visitDown(markovChain);
  }
  
  private RemoveZeroTerminals()
  {
    super();
  }
  
  @Override
  public void visit(final MarkovNode<Character> node)
  {
    TreeMap<Character, MarkovNode<Character>> children = new TreeMap<Character, MarkovNode<Character>>(node.getChildren());
    for (final MarkovNode<Character> child : children.values())
    {
      if (child.getWeight() == 0)
      {
        node.removeChild(child);
      }
    }
  }
}