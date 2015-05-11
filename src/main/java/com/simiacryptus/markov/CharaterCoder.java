package com.simiacryptus.markov;

import com.simiacryptus.binary.Bits;

public class CharaterCoder
{
  public final int bitLength = 16;
  public final Character nullValue = Character.valueOf('\0');
  
  public Character fromBits(final Bits bits)
  {
    final char primitive = (char) bits.toLong();
    final Character object = Character.valueOf(primitive);
    // assert(bits.equals(toBits(object)));
    return object;
  }
  
  public Bits toBits(final Character object)
  {
    final char primitive = object.charValue();
    final Bits bits = new Bits(primitive, this.bitLength);
    assert object.toString().equals(fromBits(bits).toString());
    return bits;
  }
  
}