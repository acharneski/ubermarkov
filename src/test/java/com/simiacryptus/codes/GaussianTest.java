package com.simiacryptus.codes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;

import com.simiacryptus.binary.BitInputStream;
import com.simiacryptus.binary.BitOutputStream;
import com.simiacryptus.codes.Gaussian;

public class GaussianTest
{
  private long decode(final Gaussian gaussian, final int max,
      final byte[] serializedData) throws IOException
  {
    final ByteArrayInputStream inBuffer = new ByteArrayInputStream(
        serializedData);
    final BitInputStream in = new BitInputStream(inBuffer);
    final long decoded = gaussian.decode(in, max);
    return decoded;
  }
  
  private byte[] encode(final Gaussian gaussian, final int max, final int i)
      throws IOException
  {
    final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
    final BitOutputStream out = new BitOutputStream(outBuffer);
    gaussian.encode(out, i, max);
    out.flush();
    final byte[] serializedData = outBuffer.toByteArray();
    return serializedData;
  }
  
  private double test(final Gaussian gaussian, final int max)
      throws IOException
  {
    int total = 0;
    for (int value = 0; value <= max; value++)
    {
      total += this.test(gaussian, max, value);
    }
    return (double) total / max;
  }
  
  private int test(final Gaussian gaussian, final int max, final int value)
      throws IOException
  {
    final byte[] serializedData = this.encode(gaussian, max, value);
    final long decoded = this.decode(gaussian, max, serializedData);
    Assert.assertEquals(value, decoded);
    return serializedData.length;
  }
  
  @Test
  public void testBinomialRandom() throws IOException
  {
    final Random random = new Random();
    for (int i = 0; i < 100; i++)
    {
      double probability = 0;
      while (0 >= probability || 1 <= probability)
      {
        probability = random.nextDouble();
      }
      for (int max = 1; max < 255; max += 1)
      {
        this.test(Gaussian.fromBinomial(probability, max), max);
      }
    }
  }
  
  @Test
  public void testBinomialScan() throws IOException
  {
    for (double probability = 0.01; probability <= 0.99; probability += .01)
    {
      for (int max = 1; max < 255; max += 1)
      {
        @SuppressWarnings("unused")
        final double result = this.test(
            Gaussian.fromBinomial(probability, max), max);
        // System.out.println(String.format("P=%s,N=%s: %s", probability, max, result));
      }
    }
  }
  
  @Test
  public void testHardcodedGaussians() throws IOException
  {
    System.out.println(String.format("T: %s",
        this.test(new Gaussian(100, 3), 255)));
    System.out.println(String.format("T: %s",
        this.test(new Gaussian(100, 10), 255)));
    System.out.println(String.format("T: %s",
        this.test(new Gaussian(100, 200), 255)));
    System.out.println(String.format("T: %s",
        this.test(new Gaussian(100, 500), 255)));
    System.out.println(String.format("T: %s",
        this.test(new Gaussian(500, 10), 255)));
    System.out.println(String.format("T: %s",
        this.test(new Gaussian(-100, 10), 255)));
    System.out.println(String.format("T: %s",
        this.test(Gaussian.fromBinomial(0.7, 3), 3)));
    System.out.println(String.format("T: %s",
        this.test(Gaussian.fromBinomial(0.5, 1), 1)));
    
  }
  
  @Test
  public void testZeros() throws IOException
  {
    for (int value = 0; value <= 0; value++)
    {
      final Gaussian gaussian = new Gaussian(100, 10);
      final byte[] serializedData = this.encode(gaussian, 0, 0);
      Assert.assertEquals(0, serializedData.length);
      final long decoded = this.decode(gaussian, 0, serializedData);
      Assert.assertEquals(value, decoded);
    }
  }
}
