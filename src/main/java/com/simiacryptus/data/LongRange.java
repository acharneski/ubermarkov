package com.simiacryptus.data;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.simiacryptus.binary.BitInputStream;
import com.simiacryptus.binary.Bits;

public class LongRange
{
  public final long from;
  public final long to;
  public final int  bitDepth;
  
  public LongRange(final long from, final long to)
  {
    super();
    if (from > to) { throw new IllegalArgumentException(); }
    this.from = from;
    this.to = to;
    this.bitDepth = (int) Math.ceil(Math.log(1 + (to - from)) / Math.log(2));
  }
  
  public boolean contains(final long value)
  {
    if (this.from > value) { return false; }
    if (this.to <= value) { return false; }
    return true;
  }
  
  public long decode(final Bits range)
  {
    return this.fromIndex(range.toLong());
  }
  
  public Bits encode(final long d)
  {
    final long index = this.toIndex(d);
    return new Bits(index, this.bitDepth);
  }
  
  @Override
  public boolean equals(final Object obj)
  {
    if (this == obj) { return true; }
    if (obj == null) { return false; }
    if (this.getClass() != obj.getClass()) { return false; }
    final LongRange other = (LongRange) obj;
    if (this.from != other.from) { return false; }
    if (this.size() != other.size()) { return false; }
    return true;
  }
  
  public long fromIndex(final long index)
  {
    final long value = this.from + index;
    if (!this.contains(value)) { throw new IllegalArgumentException(); }
    return value;
  }
  
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (this.from ^ this.from >>> 32);
    result = prime * result + (int) (this.size() ^ this.size() >>> 32);
    return result;
  }
  
  public long read(final BitInputStream in) throws IOException
  {
    return this.decode(in.read(this.bitDepth));
  }
  
  public long size()
  {
    final long size = this.to - this.from;
    if (0 == size) { return 1; }
    return size;
  }
  
  public long toIndex(final long value)
  {
    if (!this.contains(value)) { throw new IllegalArgumentException(); }
    return value - this.from;
  }
  
  public JSONObject toJson() throws JSONException
  {
    final JSONObject dimJson = new JSONObject();
    dimJson.put("from", this.from);
    dimJson.put("to", this.to);
    dimJson.put("bitDepth", this.bitDepth);
    return dimJson;
  }
  
  @Override
  public String toString()
  {
    try
    {
      return this.toJson().toString();
    }
    catch (final JSONException e)
    {
      return e.getMessage();
    }
  }
  
}