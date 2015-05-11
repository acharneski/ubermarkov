package com.simiacryptus.markov.visitors;

import com.simiacryptus.markov.MarkovNode;
import com.simiacryptus.markov.MarkovVisitor;

public final class RemoveMixedTerminalCounts extends MarkovVisitor<Character>
{
  
  public static void run(final MarkovNode<Character> markovChain)
  {
    new RemoveMixedTerminalCounts().visitUp(markovChain);
  }
  
  private RemoveMixedTerminalCounts()
  {
    super();
  }
  
  @Override
  public void visit(final MarkovNode<Character> node)
  {
    if (node.getChildren().size() == 0)
      return;
    int value = 0;
    for (final MarkovNode<Character> child : node.getChildren().values())
    {
      value += child.getWeight();
    }
    node.setWeight(value);
  }
}