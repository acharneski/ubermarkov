package com.simiacryptus.binary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import com.simiacryptus.binary.BitInputStream;
import com.simiacryptus.binary.BitOutputStream;
import com.simiacryptus.binary.Bits;
import com.simiacryptus.math.SCRandom;

public class StreamTest
{
  public static enum Checks
  {
    CheckFour, CheckOne, CheckThree, CheckTwo;
  }

  SCRandom random = new SCRandom();

  @Test
  public void streamEnumChecks() throws IOException
  {
    final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
    final BitOutputStream out = new BitOutputStream(outBuffer);
    for (int i = 0; i < 100; i++)
    {
      out.write(this.random.choose(Checks.values()));
    }
    out.flush();

    final byte[] serializedData = outBuffer.toByteArray();
    this.random.reset();

    final ByteArrayInputStream inBuffer = new ByteArrayInputStream(serializedData);
    final BitInputStream in = new BitInputStream(inBuffer);
    for (int i = 0; i < 100; i++)
    {
      in.expect(this.random.choose(Checks.values()));
    }
  };

  @Test
  public void streamTest() throws IOException
  {
    final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
    final BitOutputStream out = new BitOutputStream(outBuffer);
    for (int i = 0; i < 100; i++)
    {
      out.write(this.random.nextBits());
    }
    out.flush();

    final byte[] serializedData = outBuffer.toByteArray();
    this.random.reset();

    final ByteArrayInputStream inBuffer = new ByteArrayInputStream(serializedData);
    final BitInputStream in = new BitInputStream(inBuffer);
    for (int i = 0; i < 100; i++)
    {
      final Bits nextBits = this.random.nextBits();
      Assert.assertEquals(nextBits, in.read(nextBits.bitLength));
    }
  }

}
