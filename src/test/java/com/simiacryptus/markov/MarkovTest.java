package com.simiacryptus.markov;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.simiacryptus.lang.LOG;
import com.simiacryptus.util.TestUtil;

public class MarkovTest
{
  
  static final boolean SERIALIZATION_CHECKS = false;
  
  File root = new File("src");
  
  public MarkovCoder getCoder()
  {
    return new MarkovCoder1(2);
  }
  
  @Test
  public void sourceCompressIncrementalDictionary() throws Exception
  {
    final MarkovModel<Character> dictionary = getCoder().newModel();
    TestUtil.visitFiles(this.root, file -> {
      try
      {
        LOG.d("Studying %s", file.getCanonicalPath());
        MarkovTest.this.getCoder().study(dictionary, file);
        System.out.print(String.format("Compressing %s (%s bytes)...", file.getCanonicalPath(), file.length()));
        final byte[] byteArray = MarkovTest.this.getCoder().encode(dictionary, file);
        System.out.print(String.format("%s bytes (%.3f%%)...", byteArray.length, 100. * (file.length() - byteArray.length) / file.length()));
        MarkovTest.this.getCoder().verify(dictionary, file, byteArray);
        LOG.d("verified!");
        
        final byte[] compressedMarkov = MarkovTest.this.getCoder().encode(dictionary);
        LOG.d("Markov chain compressed to %s bytes", compressedMarkov.length);
      } catch (final IOException e)
      {
        throw new RuntimeException(e);
      }
    });
  }
  
  @Test
  public void sourceCompressIndividuals() throws Exception
  {
    TestUtil.visitFiles(this.root, file -> {
      try
      {
        final MarkovModel<Character> dictionary = MarkovTest.this.getCoder().newModel();
        LOG.d("Studying %s", file.getCanonicalPath());
        MarkovTest.this.getCoder().study(dictionary, file);
        System.out.print(String.format("Compressing %s (%s bytes)...", file.getCanonicalPath(), file.length()));
        final byte[] byteArray = MarkovTest.this.getCoder().encode(dictionary, file);
        System.out.print(String.format("%s bytes (%.3f%%)...", byteArray.length, 100. * (file.length() - byteArray.length) / file.length()));
        
        final byte[] compressedMarkov = MarkovTest.this.getCoder().encode(dictionary);
        LOG.d("Markov chain compressed to %s bytes", compressedMarkov.length);
        final MarkovModel<Character> dictionary2 = MarkovTest.this.getCoder().decodeModel(compressedMarkov);
        
        MarkovTest.this.getCoder().verify(dictionary2, file, byteArray);
        LOG.d("verified!");
      } catch (final IOException e)
      {
        throw new RuntimeException(e);
      }
    });
  }
  
  @Test
  public void sourceCompressSharedDictionary() throws Exception
  {
    final MarkovModel<Character> dictionary = getCoder().newModel();
    TestUtil.visitFiles(this.root, file -> {
      try
      {
        LOG.d("Studying %s", file.getCanonicalPath());
        MarkovTest.this.getCoder().study(dictionary, file);
      } catch (final IOException e)
      {
        throw new RuntimeException(e);
      }
    });
    
    final byte[] compressedMarkov = getCoder().encode(dictionary);
    LOG.d("Markov chain (%s nodes) compressed to %s bytes", dictionary.root.getNodeCount(), compressedMarkov.length);
    final MarkovModel<Character> dictionary2 = getCoder().decodeModel(compressedMarkov);
    
    final AtomicLong totalUncompressed = new AtomicLong();
    final AtomicLong totalCompressed = new AtomicLong();
    
    TestUtil.visitFiles(this.root, file -> {
      try
      {
        System.out.print(String.format("Compressing %s (%s bytes)...", file.getCanonicalPath(), file.length()));
        totalUncompressed.addAndGet(file.length());
        final byte[] byteArray = MarkovTest.this.getCoder().encode(dictionary, file);
        totalCompressed.addAndGet(byteArray.length);
        System.out.print(String.format("%s bytes (%.3f%%)...", byteArray.length, 100. * (file.length() - byteArray.length) / file.length()));
        MarkovTest.this.getCoder().verify(dictionary2, file, byteArray);
        LOG.d("verified!");
        
      } catch (final IOException e)
      {
        throw new RuntimeException(e);
      }
    });
    
    LOG.d("Data: %s bytes compressed to %s bytes (%.3f%%)", totalUncompressed.get(), totalCompressed.get(),
        100. * (totalUncompressed.get() - totalCompressed.get())
            / totalUncompressed.get());
    System.out.println(String.format("Data+Dict: %s bytes compressed to %s bytes (%.3f%%)", totalUncompressed.get(), totalCompressed.get()
        + compressedMarkov.length, 100.
        * (totalUncompressed.get() - totalCompressed.get() - compressedMarkov.length) / totalUncompressed.get()));
  }
  
}