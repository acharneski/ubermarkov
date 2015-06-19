package com.simiacryptus.util;

import java.io.IOException;

public abstract class SerializableObjectTester<T>
{
  public abstract T deserialize(byte[] data) throws IOException;
  
  public void initialize(final T object) throws Exception
  {
  };
  
  public abstract T newObject();
  
  public abstract byte[] serialize(T object) throws IOException;
  
  public abstract boolean testEquality(T expected, T actual);
}