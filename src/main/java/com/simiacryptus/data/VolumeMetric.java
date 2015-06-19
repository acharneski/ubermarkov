package com.simiacryptus.data;

public class VolumeMetric
{
  public final int    dimension;
  public final double value;
  
  public VolumeMetric(final double value, final int dimension)
  {
    super();
    this.value = value;
    this.dimension = dimension;
  }
  
  public VolumeMetric add(final VolumeMetric right)
  {
    if (right.dimension == this.dimension) { return new VolumeMetric(this.value
        * right.value, this.dimension); }
    return right.dimension > this.dimension ? right : this;
  }
  
  public VolumeMetric divide(final VolumeMetric right)
  {
    return new VolumeMetric(this.value / right.value, this.dimension
        - right.dimension);
  }
  
  public VolumeMetric multiply(final double right)
  {
    return new VolumeMetric(this.value * right, this.dimension);
  }
  
  public VolumeMetric multiply(final VolumeMetric right)
  {
    return new VolumeMetric(this.value * right.value, this.dimension
        + right.dimension);
  }
  
  @Override
  public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append("VolumeMetric [dimension=");
    builder.append(this.dimension);
    builder.append(", value=");
    builder.append(this.value);
    builder.append("]");
    return builder.toString();
  }
}