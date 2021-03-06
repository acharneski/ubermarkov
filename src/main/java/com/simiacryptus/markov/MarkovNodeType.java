package com.simiacryptus.markov;

public enum MarkovNodeType
{
  Root, Branch, Twig, Leaf;
  
  public static MarkovNodeType getType(final MarkovNode<?> node)
  {
    if (node.getPath().path.size() == 0) { return Root; }
    if (node.getChildren().size() == 0) { return Leaf; }
    if (node.getChildren().firstEntry().getValue().getChildren().size() == 0) { return Twig; }
    return Branch;
  }
}