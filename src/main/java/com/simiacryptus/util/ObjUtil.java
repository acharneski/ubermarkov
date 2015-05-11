package com.simiacryptus.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ObjUtil
{

  public static class ObjId
  {
    Object obj;

    public ObjId(final Object obj)
    {
      this.obj = obj;
    }

    @Override
    public boolean equals(final Object obj)
    {
      if (null == obj) return false;
      if (!(obj instanceof ObjId)) return false;
      return this.obj == ((ObjId) obj).obj;
    }

    @Override
    public int hashCode()
    {
      return System.identityHashCode(this.obj);
    }

    @Override
    public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append(this.obj.getClass().getSimpleName());
      builder.append("@");
      builder.append(Integer.toHexString(System.identityHashCode(this.obj)));
      return builder.toString();
    }

  }

  private static JSONArray toArray(final Iterable<?> value, final HashSet<ObjId> stack) throws IllegalArgumentException, JSONException, IllegalAccessException
  {
    final JSONArray array = new JSONArray();
    for (final Object v : value)
    {
      if (null == v)
      {
        array.put((Object) null);
      }
      else
      {
        array.put(ObjUtil.toJson(v, stack));
      }
    }
    return array;
  }

  private static JSONArray toArray(final Object[] x, final HashSet<ObjId> stack) throws IllegalArgumentException, JSONException, IllegalAccessException
  {
    final JSONArray array = new JSONArray();
    for (final Object v : x)
    {
      if (null == v)
      {
        array.put((Object) null);
      }
      else
      {
        array.put(ObjUtil.toJson(v, stack));
      }
    }
    return array;
  }

  public static JSONObject toJson(final Object obj) throws JSONException, IllegalArgumentException, IllegalAccessException
  {
    return ObjUtil.toJson(obj, new HashSet<ObjId>());
  }

  public static JSONObject toJson(final Object obj, final HashSet<ObjId> stack) throws JSONException, IllegalArgumentException, IllegalAccessException
  {
    final ObjId id = new ObjId(obj);
    if (!stack.add(id)) return null;
    JSONObject json;
    try
    {
      json = new JSONObject();
      json.put("_id", new ObjId(obj).toString());
      if (List.class.isAssignableFrom(obj.getClass()))
      {
        json.put("elements", ObjUtil.toArray((List<?>) obj, stack));
      }
      else if (Map.class.isAssignableFrom(obj.getClass()))
      {
        json.put("entries", ObjUtil.toArray(((Map<?, ?>) obj).entrySet(), stack));
      }
      for (final Field f : obj.getClass().getDeclaredFields())
      {
        if (0 != (f.getModifiers() & Modifier.STATIC))
        {
          continue;
        }
        if (0 != (f.getModifiers() & Modifier.PRIVATE))
        {
          continue;
        }
        f.setAccessible(true);
        final Object value = f.get(obj);
        if (null == value)
        {
          continue;
        }
        if (f.getType().isPrimitive())
        {
          if (double.class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), (double) (Double) value);
          }
          else if (Double.class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), (double) (Double) value);
          }
          else if (int.class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), (int) (Integer) value);
          }
          else if (Integer.class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), (int) (Integer) value);
          }
          else if (Long.class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), (long) (Long) value);
          }
          else if (long.class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), (long) (Long) value);
          }
          else if (Boolean.class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), (boolean) (Boolean) value);
          }
          else if (boolean.class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), (boolean) (Boolean) value);
          }
          else throw new RuntimeException(f.toString());
        }
        else if (String.class.isAssignableFrom(f.getType()))
        {
          json.put(f.getName(), value);
        }
        else if (value.getClass().isArray())
        {
          if (int[].class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), value);
          }
          else if (double[].class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), value);
          }
          else if (char[].class.isAssignableFrom(f.getType()))
          {
            json.put(f.getName(), value);
          }
          else
          {
            json.put(f.getName(), ObjUtil.toArray((Object[]) value, stack));
          }
        }
        else
        {
          assert !f.getType().isPrimitive();
          json.put(f.getName(), ObjUtil.toJson(value, stack));
        }
      }
    } finally
    {
      stack.remove(id);
    }
    return json;
  }

}
