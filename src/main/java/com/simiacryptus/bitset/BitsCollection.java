package com.simiacryptus.bitset;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.simiacryptus.binary.BitInputStream;
import com.simiacryptus.binary.BitOutputStream;
import com.simiacryptus.binary.Bits;
import com.simiacryptus.collections.CountCollection;

public abstract class BitsCollection<C extends Map<Bits, AtomicInteger>> extends CountCollection<Bits, C>
{

  public enum CodeType
  {
    Prefix, Terminal, Unknown
  }

  public final Integer bitDepth;

  public BitsCollection(final C collection)
  {
    super(collection);
    this.bitDepth = null;
  }

  public BitsCollection(final int bitDepth, final C collection)
  {
    super(collection);
    this.bitDepth = bitDepth;
  }

  public CodeType getType(final Bits bits)
  {
    if (null != this.bitDepth)
    {
      if (bits.bitLength == this.bitDepth) return CodeType.Terminal;
      if (bits.bitLength < this.bitDepth) return CodeType.Prefix;
      throw new IllegalArgumentException();
    }
    return CodeType.Unknown;
  }

  public abstract void read(BitInputStream in) throws IOException;

  public abstract void write(BitOutputStream out) throws IOException;

}