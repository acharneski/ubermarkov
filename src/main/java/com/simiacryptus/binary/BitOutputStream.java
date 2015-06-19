package com.simiacryptus.binary;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class BitOutputStream
{
  
  private boolean      useChecks       = false;
  
  private OutputStream inner;
  
  private Bits         remainder       = new Bits(0);
  
  static final int     varLongDepths[] = { 6, 16, 32, 64 };
  
  public BitOutputStream(final OutputStream inner)
  {
    this.inner = inner;
  }
  
  public synchronized void flush() throws IOException
  {
    this.inner.write(this.remainder.getBytes());
    this.inner.flush();
    this.remainder = new Bits(0);
  }
  
  public boolean isUseChecks()
  {
    return this.useChecks;
  }
  
  public BitOutputStream setUseChecks(final boolean useChecks)
  {
    this.useChecks = useChecks;
    return this;
  }
  
  public synchronized void write(final Bits encode) throws IOException
  {
    final Bits newRemainder = this.remainder.concatenate(encode);
    final int newRemainingBits = newRemainder.bitLength % 8;
    final Bits toWrite = newRemainder.range(0, newRemainder.bitLength
        - newRemainingBits);
    this.inner.write(toWrite.getBytes());
    this.remainder = newRemainder.range(toWrite.bitLength);
  }
  
  public void write(final boolean b) throws IOException
  {
    this.write(new Bits(b ? 1l : 0l, 1));
  }
  
  public void write(final double value) throws IOException
  {
    this.write(new Bits(Double.doubleToLongBits(value), 64));
  }
  
  public <T extends Enum<T>> void write(final Enum<T> value) throws IOException
  {
    final long ordinal = value.ordinal();
    this.write(new Bits(ordinal, 8));
  }
  
  public void write(final int value) throws IOException
  {
    this.write(new Bits(value, 32));
  }
  
  public void writeBoundedLong(final long value, final long max)
      throws IOException
  {
    final int bits = 1 >= max ? 0 : (int) Math
        .ceil(Math.log(max) / Math.log(2));
    if (this.useChecks)
    {
      this.write(new Bits(bits, 8));
    }
    if (0 < bits)
    {
      this.write(new Bits(value, bits));
    }
  }
  
  public void writeVarLong(final long value) throws IOException
  {
    final int bitLength = new Bits(value).bitLength;
    int type = Arrays.binarySearch(varLongDepths, bitLength);
    if (type < 0)
    {
      type = -type - 1;
    }
    this.write(new Bits(type, 2));
    this.write(new Bits(value, varLongDepths[type]));
  }
  
}
