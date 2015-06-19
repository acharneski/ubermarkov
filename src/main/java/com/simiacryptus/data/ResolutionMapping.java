package com.simiacryptus.data;

import org.json.JSONException;
import org.json.JSONObject;

public class ResolutionMapping extends DoubleRange
{
  public final double     resolution;
  
  public ResolutionMapping(final double from, final double to,
      final double resolution)
  {
    super(from, to);
    this.resolution = resolution;
  }
  
  public double canonicalValue(final double d)
  {
    return this.fromIndex(this.toIndex(d));
  }
  
  public boolean equals(final double d, final double e)
  {
    final double diff = Math.abs(d - e);
    final boolean result = diff < this.resolution;
    if (result)
    {
      return true;
    }
    else
    {
      return false;
    }
  }
  
  @Override
  public boolean equals(final Object obj)
  {
    if (this == obj) { return true; }
    if (!super.equals(obj)) { return false; }
    if (this.getClass() != obj.getClass()) { return false; }
    final ResolutionMapping other = (ResolutionMapping) obj;
    if (Double.doubleToLongBits(this.resolution) != Double
        .doubleToLongBits(other.resolution)) { return false; }
    return true;
  }
  
  public double fromIndex(final long index)
  {
    return this.from() + index * this.resolution;
  }
  
  public DoubleRange fromIndex(final LongRange dimensionIndexRange)
  {
    return new DoubleRange(
        this.fromIndex(dimensionIndexRange.from),
        this.fromIndex(dimensionIndexRange.to));
  }
  
  public LongRange getIndexRange()
  {
    final double length = size() / resolution;
    return new LongRange(0, (long) Math.floor(length));
  }
  
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(this.resolution);
    result = prime * result + (int) (temp ^ temp >>> 32);
    return result;
  }
  
  public ResolutionMapping[] split(final double splitValue)
  {
    return new ResolutionMapping[] {
        new ResolutionMapping(this.from(), splitValue, this.resolution),
        new ResolutionMapping(splitValue, this.to(), this.resolution)
    
    };
  }
  
  public long toIndex(final double d)
  {
    final double delta = d - this.from();
    // We use the floor function because the canonical point of
    // any point which is in the range should also be in the range
    final double doubleIndex = delta / this.resolution;
    final long floor = (long) Math.floor(doubleIndex);
    LongRange indexRange = getIndexRange();
    if (floor < indexRange.from) { return indexRange.from; }
    if (floor >= indexRange.to) { return indexRange.to - 1; }
    return floor;
  }
  
  public JSONObject toJson() throws JSONException
  {
    final JSONObject dimJson = super.toJson();
    dimJson.put("resolution", this.resolution);
    return dimJson;
  }
  
}