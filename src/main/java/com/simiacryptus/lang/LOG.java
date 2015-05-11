package com.simiacryptus.lang;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.function.Consumer;

import org.apache.commons.io.output.ByteArrayOutputStream;

import com.simiacryptus.util.Util;

public class LOG
{

  public enum Severity
  {
    Debug
  }

  private static final long startTime = System.nanoTime();

  public static void d(final Consumer<PrintStream> c)
  {
    LOG.d(Util.print(c));
  }

  public static void d(final String msg, final Object... args)
  {
    LOG.preprocessArgs(args);
    LOG.log(Severity.Debug, msg, args);
  }

  public static void d(final Throwable e, final String msg, final Object... args)
  {
    LOG.d(msg + "\n  " + LOG.toString(e).replace("\n", "\n  "), args);
  }

  private static void log(final Severity debug, final String msg, final Object[] args)
  {
    final String formatted = String.format(msg, args);
    final StackTraceElement caller = Arrays.stream(Thread.currentThread().getStackTrace()).filter((stack) -> {
      Class<?> clazz;
      try
      {
        clazz = Class.forName(stack.getClassName());
      } catch (final Exception e)
      {
        return true;
      }
      if (clazz == Thread.class) return false;
      if (clazz == LOG.class) return false;
      return true;
    }).findFirst().get();
    final double time = (System.nanoTime() - LOG.startTime) / 1000000000.;
    final String line = String.format("[%.5f] (%s:%s) %s", time, caller.getFileName(), caller.getLineNumber(), formatted.replaceAll("\n", "\n\t"));
    System.out.println(line);
  }

  private static void preprocessArgs(final Object... args)
  {
    for (int i = 0; i < args.length; i++)
    {
      if (null == args[i])
      {
        continue;
      }
      final Class<? extends Object> c = args[i].getClass();
      if (c.isArray())
      {
        if (args[i] instanceof double[])
        {
          args[i] = LOG.toString((double[]) args[i]);
        }
        else if (args[i] instanceof int[])
        {
          args[i] = Arrays.toString((int[]) args[i]);
        }
        else if (args[i] instanceof long[])
        {
          args[i] = Arrays.toString((long[]) args[i]);
        }
        else if (args[i] instanceof byte[])
        {
          args[i] = Arrays.toString((byte[]) args[i]);
        }
        else
        {
          args[i] = Arrays.toString((Object[]) args[i]);
        }
      }
    }
  }

  private static String toString(final double[] point)
  {
    final StringBuffer sb = new StringBuffer();
    for (final double v : point)
    {
      if (0 < sb.length())
      {
        sb.append(", ");
      }
      sb.append(String.format("%.3f", v));
    }
    return "[" + sb.toString() + "]";
  }

  public static String toString(final Throwable e)
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final PrintStream s = new PrintStream(out);
    try
    {
      e.printStackTrace(s);
    } finally
    {
      s.close();
    }
    final String exception = out.toString();
    return exception;
  }

}
