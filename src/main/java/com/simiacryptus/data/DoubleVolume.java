package com.simiacryptus.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import com.simiacryptus.lang.JsonUtil;


@SuppressWarnings("serial")
public class DoubleVolume extends ArrayList<DoubleRange>
{

  public DoubleVolume(final DoubleRange... ranges)
  {
    super(Arrays.asList(ranges));
  }

  public DoubleVolume(Collection<DoubleRange> array)
  {
    super(array);
  }

  public DoubleVolume intersect(
      final DoubleVolume right)
  {
    final DoubleRange[] intersection = new DoubleRange[right.size()];
    for (int i = 0; i < intersection.length; i++)
    {
      final DoubleRange a = get(i);
      final DoubleRange b = right.get(i);
      intersection[i] = a.intersect(b);
    }
    return new DoubleVolume(intersection);
  }
  
  public boolean intersects(final DoubleVolume range)
  {
    for (int i = 0; i < size(); i++)
    {
      final DoubleRange a = range.get(i);
      final DoubleRange b = get(i);
      if (!a.intersects(b)) { return false; }
    }
    return true;
  }

  public boolean isCollapsed()
  {
    for (final DoubleRange range : this)
    {
      if (range.size() == 0) { return true; }
    }
    return false;
  }
  
  public double[] sample(final Random random)
  {
    final double[] point = new double[size()];
    for (int i = 0; i < point.length; i++)
    {
      point[i] = get(i).sample(random);
    }
    return point;
  }

  public VolumeMetric getVolume()
  {
    int dimension = 0;
    double value = 1;
    for (final DoubleRange range : this)
    {
      final double size = range.size();
      if (0 != size)
      {
        value *= size;
        dimension++;
      }
    }
    return new VolumeMetric(value, dimension);
  }

  public boolean contains(final double[] point)
  {
    for (int i = 0; i < size(); i++)
    {
      if (!get(i).contains(point[i])) { return false; }
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

  public JSONObject toJson() throws JSONException
  {
    final JSONObject json = new JSONObject();
    json.put("dimensions", JsonUtil.toJsonArray(this));
    return json;
  }
  
}