package com.simiacryptus.data;

import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import com.simiacryptus.lang.JsonFormattable;

public class DoubleRange implements JsonFormattable
{
  
  public final double from;
  public final double to;
  
  public DoubleRange(final double from, final double to)
  {
    super();
    if (from > to) { throw new IllegalArgumentException(); }
    this.from = from;
    this.to = to;
  }
  
  public boolean contains(final double value)
  {
    if (this.from > value) { return false; }
    if (this.to <= value) { return false; }
    return true;
  }
  
  @Override
  public boolean equals(final Object obj)
  {
    if (this == obj) { return true; }
    if (obj == null) { return false; }
    if (this.getClass() != obj.getClass()) { return false; }
    final DoubleRange other = (DoubleRange) obj;
    if (Double.doubleToLongBits(this.from) != Double
        .doubleToLongBits(other.from)) { return false; }
    if (Double.doubleToLongBits(this.to) != Double.doubleToLongBits(other.to)) { return false; }
    return true;
  }
  
  public double from()
  {
    return this.from;
  }
  
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(this.from);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(this.to);
    result = prime * result + (int) (temp ^ temp >>> 32);
    return result;
  }
  
  public DoubleRange intersect(final DoubleRange b)
  {
    final double f = Math.max(this.from, b.from());
    final double t = Math.min(this.to, b.to());
    return (f > t)?null:new DoubleRange(f, t);
  }
  
  public boolean intersects(final DoubleRange b)
  {
    final double f = Math.max(this.from, b.from());
    final double t = Math.min(this.to, b.to());
    return f <= t;
  }
  
  public double sample(final Random random)
  {
    return this.from + random.nextDouble() * this.size();
  }
  
  public double size()
  {
    return this.to - this.from;
  }
  
  public JsonFormattable[] split(final double splitValue)
  {
    return new JsonFormattable[] {
        new DoubleRange(this.from, splitValue),
        new DoubleRange(splitValue, this.to)
    
    };
  }
  
  public double to()
  {
    return this.to;
  }
  
  /* (non-Javadoc)
   * @see com.simiacryptus.data.JsonFormattable#toJson()
   */
  @Override
  public JSONObject toJson() throws JSONException
  {
    final JSONObject dimJson = new JSONObject();
    dimJson.put("from", this.from);
    dimJson.put("to", this.to);
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