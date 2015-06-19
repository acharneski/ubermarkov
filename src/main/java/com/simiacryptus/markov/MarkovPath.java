package com.simiacryptus.markov;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MarkovPath<T extends Comparable<T>> implements
    Comparable<MarkovPath<T>>
{
  public final List<T> path;
  
  public MarkovPath(final List<T> path)
  {
    super();
    final List<T> tempList = new ArrayList<T>();
    for (final T item : path)
    {
      tempList.add(item);
    }
    this.path = Collections.unmodifiableList(tempList);
  }
  
  public MarkovPath(final T... path)
  {
    super();
    final List<T> tempList = new ArrayList<T>();
    for (final T item : path)
    {
      tempList.add(item);
    }
    this.path = Collections.unmodifiableList(tempList);
  }
  
  @Override
  public int compareTo(final MarkovPath<T> o)
  {
    for (int i = 0; i < this.path.size(); i++)
    {
      if (i >= o.path.size()) { return 1; }
      final int compareTo = this.path.get(i).compareTo(o.path.get(i));
      if (0 != compareTo) { return compareTo; }
    }
    return this.path.size() < o.path.size() ? -1 : 0;
  }
  
  @Override
  public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    for (final T item : this.path)
    {
      if (0 < builder.length())
      {
        builder.append(", ");
      }
      String string = format(item);
      builder.append(string);
    }
    return "[" + builder.toString() + "]";
  }

  public static String format(final Object item)
  {
    if (null == item)
    {
      return "null";
    }
    if (item instanceof Character)
    {
      Character character = (Character) item;
      if(character.equals('\0'))
      {
        return "\\0";
      }
      else if(character.equals('\n'))
      {
        return "\\n";
      }
      else if(character.equals('\r'))
      {
        return "\\r";
      }
    }
    return item.toString();
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    MarkovPath<?> other = (MarkovPath<?>) obj;
    if (path == null)
    {
      if (other.path != null) return false;
    }
    else if (!path.equals(other.path)) return false;
    return true;
  }

  public int size()
  {
    return path.size();
  }

  public MarkovPath<T> subPath(int begin)
  {
    return new MarkovPath<T>(path.subList(begin, path.size()));
  }

  public T get(int i)
  {
    return path.get(i);
  }
  
}