package com.simiacryptus.util;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;

import com.google.common.collect.Multiset;
import com.simiacryptus.lang.LOG;

public class TestUtil
{
  public static interface Visitor<T>
  {
    void run(T object);
  }
  
  public static final Random random = TestUtil.newRandom();
  
  public static final long startTime = System.nanoTime();
  
  public static <T> void assertEqual(final Multiset<T> expected, final Multiset<T> actual)
  {
    for (final T p : actual.elementSet())
    {
      Assert.assertEquals(expected.count(p), actual.count(p));
    }
    for (final T p : expected.elementSet())
    {
      Assert.assertEquals(expected.count(p), actual.count(p));
    }
  }
  
  private static Random newRandom()
  {
    final long nanoTime = System.nanoTime();
    final long seed = (nanoTime >> 32) + (nanoTime << 32);
    final Object[] args =
      { Long.toHexString(seed) };
    LOG.d("Initialized global random seed as 0x%s", args);
    return new Random(seed);
  }
  
  public static void openJson(final JSONObject json) throws IOException, FileNotFoundException, JSONException
  {
    TestUtil.openJson(UUID.randomUUID().toString(), json);
  }
  
  public static void openJson(final String filename, final JSONObject json) throws IOException, FileNotFoundException, JSONException
  {
    final File tempFile = File.createTempFile(filename, ".json");
    final PrintStream out = new PrintStream(tempFile);
    out.print(json.toString(2));
    out.close();
    Desktop.getDesktop().open(tempFile);
  }
  
  public static void visitFiles(final File root, final TestUtil.Visitor<File> visitor)
  {
    for (final File file : root.listFiles())
    {
      if (file.isDirectory())
      {
        TestUtil.visitFiles(file, visitor);
      } else
      {
        visitor.run(file);
      }
    }
  }
  
}
