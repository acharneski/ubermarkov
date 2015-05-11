package com.simiacryptus.lang;

@SuppressWarnings("serial")
public class NotImplementedException extends RuntimeException
{

  // Show a warning whenever this is referenced - We shoud implement it!
  @Deprecated
  public NotImplementedException()
  {
    super();
  }

  // Show a warning whenever this is referenced - We shoud implement it!
  @Deprecated
  public NotImplementedException(final String arg0)
  {
    super(arg0);
  }

}
