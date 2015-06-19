package com.simiacryptus.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.json.JSONException;
import org.json.JSONObject;

import com.simiacryptus.lang.JsonUtil;

@SuppressWarnings("serial")
public class VolumeMapping extends ArrayList<ResolutionMapping>
{

  public VolumeMapping(final ResolutionMapping... ranges)
  {
    super(Arrays.asList(ranges));
  }

  public VolumeMapping(Collection<ResolutionMapping> array)
  {
    super(array);
  }
  
  public double[] canonicalValue(final double[] point)
  {
    final double[] values = new double[point.length];
    for (int i = 0; i < values.length; i++)
    {
      values[i] = get(i).canonicalValue(point[i]);
    }
    return values;
  }
  
  public boolean equals(final double[] p, final double[] d)
  {
    for (int i = 0; i < size(); i++)
    {
      if (!get(i).equals(p[i], d[i])) { return false; }
    }
    return true;
  }
  
  @Override
  public boolean equals(final Object obj)
  {
    if (this == obj) { return true; }
    if (obj == null) { return false; }
    if (this.getClass() != obj.getClass()) { return false; }
    if(!super.equals(obj)) return false;
    return true;
  }
  
  public double[] fromIndex(final long[] point)
  {
    final double[] values = new double[point.length];
    for (int i = 0; i < values.length; i++)
    {
      values[i] = get(i).fromIndex(point[i]);
    }
    return values;
  }
  
  public DoubleVolume fromIndex(final LongVolume range)
  {
    final ArrayList<DoubleRange> list = new ArrayList<DoubleRange>();
    for (int i = 0; i < size(); i++)
    {
      list.add(get(i).fromIndex(range.getRange(i)));
    }
    return new DoubleVolume(list.toArray(new DoubleRange[] {}));
  }

  public LongVolume getLongRange()
  {
    final LongRange[] longRanges = new LongRange[size()];
    for (int i = 0; i < longRanges.length; i++)
    {
      longRanges[i] = get(i).getIndexRange();
    }
    return new LongVolume(longRanges);
  }

  public DoubleVolume getDoubleRange()
  {
    final DoubleRange[] longRanges = new DoubleRange[size()];
    for (int i = 0; i < longRanges.length; i++)
    {
      longRanges[i] = get(i);
    }
    return new DoubleVolume(longRanges);
  }
  
  public long[] toIndex(final double[] point)
  {
    final long[] values = new long[point.length];
    for (int i = 0; i < values.length; i++)
    {
      values[i] = get(i).toIndex(point[i]);
    }
    return values;
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

  public JSONObject toJson() throws JSONException
  {
    final JSONObject json = new JSONObject();
    json.put("dimensions", JsonUtil.toJsonArray(this));
    return json;
  }
  
}