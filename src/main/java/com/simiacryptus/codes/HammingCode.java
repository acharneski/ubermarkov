package com.simiacryptus.codes;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import com.simiacryptus.binary.BitInputStream;
import com.simiacryptus.binary.Bits;
import com.simiacryptus.bitset.CountTreeBitsCollection;

public class HammingCode<T extends Comparable<T>>
{
  public class HammingCodeCollection extends CountTreeBitsCollection
  {
    public HammingCodeCollection()
    {
      super();
    }

    public HammingCodeCollection(final BitInputStream data) throws IOException
    {
      super(data);
    }

    public HammingCodeCollection(final byte[] data) throws IOException
    {
      super(data);
    }

    @Override
    public CodeType getType(final Bits bits)
    {
      final Entry<Bits, T> code = HammingCode.this.decode(bits);
      if (null == code) return CodeType.Prefix;
      assert bits.equals(code.getKey());
      return CodeType.Terminal;
    }
  }

  private static class SubCode<X extends Comparable<X>> implements Comparable<SubCode<X>>
  {
    final TreeMap<Bits, X> codes;
    final long count;
    final TreeMap<X, Bits> index;

    public SubCode(final long count, final X item)
    {
      super();
      this.count = count;
      this.codes = new TreeMap<Bits, X>();
      this.index = new TreeMap<X, Bits>();
      this.codes.put(Bits.NULL, item);
      this.index.put(item, Bits.NULL);
    }

    public SubCode(final SubCode<X> zero, final SubCode<X> one)
    {
      super();
      this.count = zero.count + one.count;
      this.codes = new TreeMap<Bits, X>();
      this.index = new TreeMap<X, Bits>();
      for (final Entry<Bits, X> e : zero.codes.entrySet())
      {
        assert !one.index.containsKey(e.getValue());
        final Bits code = Bits.ZERO.concatenate(e.getKey());
        this.assertNull(this.codes.put(code, e.getValue()));
        this.assertNull(this.index.put(e.getValue(), code));
      }
      for (final Entry<Bits, X> e : one.codes.entrySet())
      {
        assert !zero.index.containsKey(e.getValue());
        final Bits code = Bits.ONE.concatenate(e.getKey());
        this.assertNull(this.codes.put(code, e.getValue()));
        this.assertNull(this.index.put(e.getValue(), code));
      }
    }

    private void assertNull(final Object obj)
    {
      assert null == obj;
    }

    @Override
    public int compareTo(final SubCode<X> o)
    {
      if (this.count < o.count) return -1;
      if (this.count > o.count) return 1;
      final int compareTo = this.index.firstKey().compareTo(o.index.firstKey());
      assert 0 != compareTo;
      return compareTo;
    }
  }

  public static boolean isPrefixFreeCode(final Set<Bits> keySet)
  {
    final TreeSet<Bits> check = new TreeSet<Bits>();
    for (final Bits code : keySet)
    {
      final Bits ceiling = check.ceiling(code);
      if (null != ceiling && (ceiling.startsWith(code) || code.startsWith(ceiling))) return false;
      final Bits floor = check.floor(code);
      if (null != floor && (floor.startsWith(code) || code.startsWith(floor))) return false;
      check.add(code);
    }
    return true;
  }

  protected final TreeMap<Bits, T> forwardIndex = new TreeMap<Bits, T>();
  protected final HashMap<T, Bits> reverseIndex = new HashMap<T, Bits>();
  protected final long totalWeight;
  protected final HashMap<T, Integer> weights = new HashMap<T, Integer>();

  public HammingCode(final Collection<HammingSymbol<T>> symbols)
  {
    if (0 < symbols.size())
    {
      final TreeSet<SubCode<T>> assemblySet = new TreeSet<SubCode<T>>();
      for (final HammingSymbol<T> s : symbols)
      {
        this.weights.put(s.key, s.count);
        assemblySet.add(new SubCode<T>(s.count, s.key));
      }
      while (assemblySet.size() > 1)
      {
        final SubCode<T> zero = assemblySet.pollFirst();
        final SubCode<T> one = assemblySet.pollFirst();
        assemblySet.add(new SubCode<T>(zero, one));
      }
      final SubCode<T> root = assemblySet.first();
      this.forwardIndex.putAll(root.codes);
      this.reverseIndex.putAll(root.index);
      this.totalWeight = root.count;
    }
    else
    {
      this.totalWeight = 0;
    }
    assert this.verifyIndexes();
    assert this.forwardIndex.size() == symbols.size();
  }

  public int codeSize()
  {
    return this.forwardIndex.size();
  }

  public T decode(final BitInputStream in) throws IOException
  {
    Bits remainder = in.readAhead(0);
    Entry<Bits, T> entry = this.forwardIndex.floorEntry(remainder);
    while (entry == null || !remainder.startsWith(entry.getKey()))
    {
      remainder = in.readAhead();
      entry = this.forwardIndex.floorEntry(remainder);
    }
    in.read(entry.getKey().bitLength);
    return entry.getValue();
  }

  public Entry<Bits, T> decode(final Bits data)
  {
    if (null == data) throw new IllegalArgumentException();
    Entry<Bits, T> entry = this.forwardIndex.floorEntry(data);
    // TestUtil.openJson(new JSONObject(forwardIndex));
    if (entry != null && !data.startsWith(entry.getKey()))
    {
      entry = null;
    }
    // assert(null != entry || verifyIndexes());
    return entry;
  }

  public Bits encode(final T key)
  {
    final Bits bits = this.reverseIndex.get(key);
    assert null != bits || this.verifyIndexes();
    return bits;
  }

  public SortedMap<Bits, T> getCodes(final Bits fromKey)
  {
    final Bits next = fromKey.next();
    final SortedMap<Bits, T> subMap = null == next ? this.forwardIndex.tailMap(fromKey) : this.forwardIndex.subMap(fromKey, next);
    return subMap;
  }

  public CountTreeBitsCollection getSetEncoder()
  {
    return new HammingCodeCollection();
  }

  public CountTreeBitsCollection getSetEncoder(final BitInputStream data) throws IOException
  {
    return new HammingCodeCollection(data);
  }

  public CountTreeBitsCollection getSetEncoder(final byte[] data) throws IOException
  {
    return new HammingCodeCollection(data);
  }

  public Map<T, Integer> getWeights()
  {
    return Collections.unmodifiableMap(this.weights);
  }

  public int totalWeight()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  public boolean verifyIndexes()
  {
    if (!HammingCode.isPrefixFreeCode(this.forwardIndex.keySet())) return false;
    for (final Entry<Bits, T> e : this.forwardIndex.entrySet())
    {
      if (!e.getKey().equals(this.reverseIndex.get(e.getValue()))) return false;
      if (!e.getValue().equals(this.forwardIndex.get(e.getKey()))) return false;
    }
    for (final Entry<T, Bits> e : this.reverseIndex.entrySet())
    {
      if (!e.getKey().equals(this.forwardIndex.get(e.getValue()))) return false;
      if (!e.getValue().equals(this.reverseIndex.get(e.getKey()))) return false;
    }
    if (this.reverseIndex.size() != this.forwardIndex.size()) return false;
    return true;
  }

}