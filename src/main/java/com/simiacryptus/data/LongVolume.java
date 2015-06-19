package com.simiacryptus.data;

import java.util.Arrays;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.simiacryptus.binary.Bits;

public class LongVolume
{
  private final LongRange[] ranges;
  public final int          bitDepth;
  public final int          dimension;
  
  public LongVolume(final LongRange... ranges)
  {
    super();
    this.ranges = Arrays.copyOf(ranges, ranges.length);
    int bitDepth = 0;
    for (final LongRange range : ranges)
    {
      bitDepth += range.bitDepth;
    }
    this.bitDepth = bitDepth;
    this.dimension = ranges.length;
  }
  
  public boolean contains(final long[] point)
  {
    for (int i = 0; i < this.ranges.length; i++)
    {
      if (!this.ranges[i].contains(point[i])) { return false; }
    }
    return true;
  }
  
  public long[] decode(final Bits encoded)
  {
    final long[] point = new long[this.ranges.length];
    int p = 0;
    for (int i = 0; i < this.ranges.length; i++)
    {
      final LongRange r = this.ranges[i];
      final int bitDepth = r.bitDepth;
      point[i] = r.decode(encoded.range(p, bitDepth));
      p += bitDepth;
    }
    return point;
  }
  
  public Bits encode(final long[] p)
  {
    Bits bits = new Bits(0);
    for (int i = 0; i < this.ranges.length; i++)
    {
      bits = bits.concatenate(this.ranges[i].encode(p[i]));
    }
    return bits;
  }
  
  @Override
  public boolean equals(final Object obj)
  {
    if (this == obj) { return true; }
    if (obj == null) { return false; }
    if (this.getClass() != obj.getClass()) { return false; }
    final LongVolume other = (LongVolume) obj;
    if (!Arrays.equals(this.ranges, other.ranges)) { return false; }
    return true;
  }
  
  public long[] fromIndex(final long[] point)
  {
    final long[] values = new long[point.length];
    for (int i = 0; i < values.length; i++)
    {
      values[i] = this.ranges[i].fromIndex(point[i]);
    }
    return values;
  }
  
  public LongRange getRange(final int dimension)
  {
    return this.ranges[dimension];
  }
  
  public LongRange[] getRanges()
  {
    return Arrays.copyOf(this.ranges, this.ranges.length);
  }
  
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(this.ranges);
    return result;
  }
  
  public long[] sample(final Random random)
  {
    final long[] values = new long[this.ranges.length];
    for (int i = 0; i < values.length; i++)
    {
      final LongRange range = this.ranges[i];
      values[i] = range.from + random.nextInt((int) (range.to - range.from));
    }
    return values;
  }
  
  public VolumeMetric size()
  {
    double volume = 1;
    int dimension = 0;
    for (final LongRange range : this.ranges)
    {
      final long dimSize = range.size();
      volume *= dimSize;
      if(1 < dimSize)
      {
        dimension++;
      }
    }
    return new VolumeMetric(volume, dimension);
  }
  
  public LongVolume[] split(final int splitDimension, final long splitValue)
  {
    final LongRange splitRange = this.ranges[splitDimension];
    if (!splitRange.contains(splitValue)) { throw new IllegalArgumentException(); }
    final LongRange[] left = Arrays.copyOf(this.ranges, this.ranges.length);
    left[splitDimension] = new LongRange(splitRange.from, splitValue);
    final LongRange[] right = Arrays.copyOf(this.ranges, this.ranges.length);
    right[splitDimension] = new LongRange(splitValue, splitRange.to);
    return new LongVolume[] { new LongVolume(left), new LongVolume(right) };
  }
  
  public long[] toIndex(final long[] point)
  {
    final long[] values = new long[point.length];
    for (int i = 0; i < values.length; i++)
    {
      values[i] = this.ranges[i].toIndex(point[i]);
    }
    return values;
  }
  
  public JSONObject toJson() throws JSONException
  {
    final JSONArray dimensions = new JSONArray();
    for (final LongRange r : this.ranges)
    {
      dimensions.put(r.toJson());
    }
    final JSONObject json = new JSONObject();
    json.put("dimensions", dimensions);
    json.put("bitDepth", this.bitDepth);
    return json;
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