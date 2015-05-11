package com.simiacryptus.binary;

import java.io.IOException;
import java.util.Random;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import com.simiacryptus.binary.Bits;

public class BitsTest
{
  Random random = new Random();

  private long randomLong()
  {
    return this.random.nextLong() >> this.random.nextInt(62);
  }

  @Test
  public void testConcatenate() throws JSONException, IOException
  {
    for (int i = 0; i < 1000; i++)
    {
      this.testConcatenate(randomLong(), randomLong());
    }
  }

  private void testConcatenate(final long a, final long b)
  {
    final String asStringA = 0 == a ? "" : Long.toBinaryString(a);
    final String asStringB = 0 == b ? "" : Long.toBinaryString(b);
    final String asString = asStringA + asStringB;
    final Bits bitsA = new Bits(a);
    final Bits bitsB = new Bits(b);
    final Bits bits = bitsA.concatenate(bitsB);
    Assert.assertEquals(String.format("Concatenate %s and %s", a, b), asString, bits.toBitString());
  }

  @Test
  public void testFixedLength() throws JSONException, IOException
  {
    for (int i = 0; i < 1000; i++)
    {
      this.testFixedLength(randomLong());
    }
  }

  private void testFixedLength(final long value)
  {
    String asString = 0 == value ? "" : Long.toBinaryString(value);
    final Bits bits = new Bits(value, 64);
    while (asString.length() < 64)
    {
      asString = "0" + asString;
    }
    Assert.assertEquals("toLong for " + value, value, bits.toLong());
    Assert.assertEquals("toString for " + value, asString, bits.toBitString());
  }

  @Test
  public void testHardcoded() throws JSONException, IOException
  {
    Assert.assertEquals(new Bits(0), new Bits(0));
    Assert.assertEquals("", new Bits(0).toBitString());
    Assert.assertEquals("1", new Bits(1).toBitString());
    Assert.assertEquals("100", new Bits(4).toBitString());
    Assert.assertEquals("10001", new Bits(17).toBitString());
    Assert.assertEquals("100", new Bits(17).range(0, 3).toBitString());
    Assert.assertEquals("01", new Bits(17).range(3).toBitString());
    Assert.assertEquals("111", new Bits(7).toBitString());
    Assert.assertEquals("10111", new Bits(2).concatenate(new Bits(7)).toBitString());
    Assert.assertEquals("00110", new Bits(6l, 5).toBitString());
    Assert.assertEquals("111000000", new Bits(7l).leftShift(6).toBitString());
    Assert.assertEquals("1110", new Bits(7l).leftShift(6).range(0, 4).toBitString());
    Assert.assertEquals("00000", new Bits(7l).leftShift(6).range(4).toBitString());
    Assert.assertEquals("110", new Bits(6l).toBitString());
    Assert.assertEquals("11100", new Bits(7l).leftShift(2).toBitString());
    Assert.assertEquals("11000", new Bits(7l).leftShift(2).bitwiseAnd(new Bits(6l)).toBitString());
    Assert.assertEquals("11100", new Bits(7l).leftShift(2).bitwiseOr(new Bits(6l)).toBitString());
    Assert.assertEquals("00100", new Bits(7l).leftShift(2).bitwiseXor(new Bits(6l)).toBitString());
    Assert.assertEquals(2, new Bits(7l, 16).getBytes().length);
  }

  @Test
  public void testSubrange() throws JSONException, IOException
  {
    for (int i = 0; i < 1000; i++)
    {
      final long value = this.random.nextLong();
      final Bits bits = new Bits(value);
      this.testSubrange(bits);
    }
  }

  private void testSubrange(final Bits bits)
  {
    final String asString = bits.toBitString();
    for (int j = 0; j < 10; j++)
    {
      final int from = this.random.nextInt(asString.length());
      final int to = from + this.random.nextInt(asString.length() - from);
      this.testSubrange(bits, asString, from, to);
    }
  }

  private void testSubrange(final Bits bits, final String asString, final int from, final int to)
  {
    final String subStr = asString.substring(from, to);
    final Bits subBits = bits.range(from, to - from);
    Assert.assertEquals(String.format("Substring (%s,%s) of %s", from, to, bits), subStr, subBits.toBitString());
  }

  @Test
  public void testToString() throws JSONException, IOException
  {
    for (int i = 0; i < 1000; i++)
    {
      this.testToString(randomLong());
    }
  }

  private void testToString(final long value)
  {
    final String asString = 0 == value ? "" : Long.toBinaryString(value);
    final Bits bits = new Bits(value);
    Assert.assertEquals("toLong for " + value, value, bits.toLong());
    Assert.assertEquals("toString for " + value, asString, bits.toBitString());
  }

}