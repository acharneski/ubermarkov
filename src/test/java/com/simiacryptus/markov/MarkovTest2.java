package com.simiacryptus.markov;

import org.junit.Test;

public class MarkovTest2 extends MarkovTest
{
  
  static final boolean SERIALIZATION_CHECKS = false;
  
  @Override
  public MarkovCoder getCoder()
  {
    return new MarkovCoder2(2);
  }
  
  @Override
  @Test
  public void sourceCompressIncrementalDictionary() throws Exception
  {
    super.sourceCompressIncrementalDictionary();
  }
  
  @Override
  @Test
  public void sourceCompressIndividuals() throws Exception
  {
    super.sourceCompressIndividuals();
  }
  
  @Override
  @Test
  public void sourceCompressSharedDictionary() throws Exception
  {
    super.sourceCompressSharedDictionary();
  }
  
}