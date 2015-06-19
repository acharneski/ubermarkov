package com.simiacryptus.markov;

public abstract class MarkovVisitor<T extends Comparable<T>>
{
  public abstract void visit(MarkovNode<T> markovChain);

  public MarkovVisitor<T> visitDown(MarkovNode<T> node)
  {
    this.visit(node);
    for (final MarkovNode<T> child : node.getChildren().values())
    {
      this.visitDown(child);
    }
    return this;
  }

  public MarkovVisitor<T> visitUp(MarkovNode<T> node)
  {
    for (final MarkovNode<T> child : node.getChildren().values())
    {
      this.visitDown(child);
    }
    this.visit(node);
    return this;
  }
}