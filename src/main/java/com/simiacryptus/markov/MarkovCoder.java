package com.simiacryptus.markov;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface MarkovCoder
{
  
  public abstract MarkovModel<Character> decodeModel(byte[] bytes) throws IOException;
  
  public abstract byte[] encode(MarkovModel<Character> dictionary) throws IOException;
  
  public abstract byte[] encode(MarkovModel<Character> dictionary, File file) throws FileNotFoundException, IOException;
  
  public abstract MarkovModel<Character> newModel();
  
  public abstract void study(MarkovModel<Character> dictionary, File file) throws FileNotFoundException, IOException;
  
  public abstract void verify(MarkovModel<Character> dictionary, File file, byte[] byteArray) throws FileNotFoundException, IOException;
  
}