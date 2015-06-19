package com.simiacryptus.binary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitInputStream
{
  
  public static BitInputStream toBitStream(final byte[] data)
  {
    return new BitInputStream(new ByteArrayInputStream(data));
  }
  
  private boolean     useChecks = false;
  private InputStream inner;
  
  private Bits        remainder = new Bits(0);
  
  public BitInputStream(final InputStream inner)
  {
    this.inner = inner;
  }
  
  public void close() throws IOException
  {
    this.inner.close();
  }
  
  public void expect(final Bits bits) throws IOException
  {
    final Bits checkBits = this.read(bits.bitLength);
    if (!bits.equals(checkBits)) { throw new IOException(String.format(
        "Check failed: %s != %s", checkBits, bits)); }
    
  }
  
  public void expect(final double expectedValue) throws IOException
  {
    final Bits checkBits = this.read(64);
    final double checkValue = Double.longBitsToDouble(checkBits.toLong());
    if (checkValue != expectedValue)
    {
      final Bits expectedBits = new Bits(
          Double.doubleToLongBits(expectedValue),
          64);
      throw new IOException(String.format("Check for %s failed: %s != %s",
          expectedValue, checkBits, expectedBits));
    }
    
  }
  
  public <T extends Enum<T>> void expect(final Enum<T> expect)
      throws IOException
  {
    final Bits checkBits = this.read(8);
    final long expectedLong = expect.ordinal();
    if (checkBits.toLong() != expectedLong)
    {
      final Bits expectedBits = new Bits(expectedLong, 8);
      throw new IOException(String.format("Check for %s failed: %s != %s",
          expect, checkBits, expectedBits));
    }
  }
  
  public void expect(final int expect) throws IOException
  {
    final Bits checkBits = this.read(32);
    final long expectedValue = expect;
    if (checkBits.toLong() != expectedValue)
    {
      final Bits expectedBits = new Bits(expectedValue, 32);
      throw new IOException(String.format("Check for %s failed: %s != %s",
          expect, checkBits, expectedBits));
    }
  }
  
  public boolean isUseChecks()
  {
    return this.useChecks;
  }
  
  public Bits read(final int length) throws IOException
  {
    final int additionalBitsNeeded = length - this.remainder.bitLength;
    final int additionalBytesNeeded = (int) Math
        .ceil(additionalBitsNeeded / 8.);
    this.readAhead(additionalBytesNeeded);
    final Bits readBits = this.remainder.range(0, length);
    this.remainder = this.remainder.range(length);
    return readBits;
  }
  
  public Bits readAhead() throws IOException
  {
    return this.readAhead(1);
  }
  
  public Bits readAhead(final int bytes) throws IOException
  {
    if (0 < bytes)
    {
      final byte[] buffer = new byte[bytes];
      this.inner.read(buffer);
      final Bits newR = this.remainder.concatenate(new Bits(buffer));
      this.remainder = newR;
    }
    return this.remainder;
  }
  
  public boolean readBool() throws IOException
  {
    return Bits.ONE.equals(this.read(1));
  }
  
  /**
   * Reads a single positive bounded integral value (up to 64-bit, including 0, excluding max)
   * 
   * @param max
   *          Maximum value (exclusive)
   * @return A long within the range [0, max)
   * @throws IOException
   */
  public long readBoundedLong(final long max) throws IOException
  {
    final int bits = 1 >= max ? 0 : (int) Math
        .ceil(Math.log(max) / Math.log(2));
    if (this.useChecks)
    {
      this.expect(new Bits(bits, 8));
    }
    return 0 < bits ? this.read(bits).toLong() : 0;
  }
  
  public long readVarLong() throws IOException
  {
    final int type = (int) this.read(2).toLong();
    return this.read(BitOutputStream.varLongDepths[type]).toLong();
  }
  
  public BitInputStream setUseChecks(final boolean useChecks)
  {
    this.useChecks = useChecks;
    return this;
  }
  
}
