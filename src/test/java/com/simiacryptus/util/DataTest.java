package com.simiacryptus.util;

public interface DataTest<T>
{
  <U extends T> void run(SerializableObjectTester<U> tester) throws Exception;
}