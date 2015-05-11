package com.simiacryptus.bitset;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;
import com.simiacryptus.binary.BitInputStream;
import com.simiacryptus.binary.BitOutputStream;
import com.simiacryptus.binary.Bits;
import com.simiacryptus.codes.Gaussian;

public class CountTreeBitsCollection extends BitsCollection<TreeMap<Bits, AtomicInteger>>
{

  public static class BranchCounts
  {
    public long oneCount;
    public Bits path;
    public long size;
    public long terminals;
    public long zeroCount;

    public BranchCounts(final Bits path, final long size)
    {
      this.path = path;
      this.size = size;
    }

    public BranchCounts(final Bits path, final long size, final long terminals, final long zeroCount, final long oneCount)
    {
      this.path = path;
      this.size = size;
      this.terminals = terminals;
      this.zeroCount = zeroCount;
      this.oneCount = oneCount;
    }
  }

  public static enum SerializationChecks
  {
    AfterCount, AfterTerminal, BeforeCount, BeforeTerminal, EndTree, StartTree
  }

  public static boolean SERIALIZATION_CHECKS = false;

  public static <T> T isNull(final T value, final T defaultValue)
  {
    return null == value ? defaultValue : value;
  }

  private boolean useBinomials = true;

  public CountTreeBitsCollection()
  {
    super(new TreeMap<Bits, AtomicInteger>());
  }

  public CountTreeBitsCollection(final BitInputStream bitStream) throws IOException
  {
    this();
    this.read(bitStream);
  }

  public CountTreeBitsCollection(final BitInputStream bitStream, final int bitDepth) throws IOException
  {
    this(bitDepth);
    this.read(bitStream);
  }

  public CountTreeBitsCollection(final byte[] data) throws IOException
  {
    this(BitInputStream.toBitStream(data));
  }

  public CountTreeBitsCollection(final byte[] data, final int bitDepth) throws IOException
  {
    this(BitInputStream.toBitStream(data), bitDepth);
  }

  public CountTreeBitsCollection(final int bitDepth)
  {
    super(bitDepth, new TreeMap<Bits, AtomicInteger>());
  }

  public TreeMap<Bits, Long> computeSums()
  {
    final TreeMap<Bits, Long> sums = new TreeMap<Bits, Long>();
    long total = 0;
    for (final Entry<Bits, AtomicInteger> e : this.map.entrySet())
    {
      sums.put(e.getKey(), total += e.getValue().get());
    }
    return sums;
  }

  @Override
  public void read(final BitInputStream in) throws IOException
  {
    getMap().clear();
    final long size = in.readVarLong();
    if (0 < size)
    {
      this.read(in, Bits.NULL, size);
    }
  }

  private void read(final BitInputStream in, final Bits code, final long size) throws IOException
  {
    if (CountTreeBitsCollection.SERIALIZATION_CHECKS)
    {
      in.expect(SerializationChecks.StartTree);
    }
    final BranchCounts branchCounts = readBranchCounts(in, code, size);
    if (0 < branchCounts.terminals)
    {
      this.map.put(code, new AtomicInteger((int) branchCounts.terminals));
    }
    if (0 < branchCounts.zeroCount)
    {
      this.read(in, code.concatenate(Bits.ZERO), branchCounts.zeroCount);
    }
    // Get one-suffixed branch
    if (branchCounts.oneCount > 0)
    {
      this.read(in, code.concatenate(Bits.ONE), branchCounts.oneCount);
    }
    if (CountTreeBitsCollection.SERIALIZATION_CHECKS)
    {
      in.expect(SerializationChecks.EndTree);
    }
  }

  public void read(final BitInputStream in, final int size) throws IOException
  {
    getMap().clear();
    if (0 < size)
    {
      this.read(in, Bits.NULL, size);
    }
  }

  protected BranchCounts readBranchCounts(final BitInputStream in, final Bits code, final long size) throws IOException
  {
    final BranchCounts branchCounts = new BranchCounts(code, size);
    final CodeType currentCodeType = getType(code);
    long maximum = size;

    // Get terminals
    if (currentCodeType == CodeType.Unknown)
    {
      branchCounts.terminals = readTerminalCount(in, maximum);
    }
    else if (currentCodeType == CodeType.Terminal)
    {
      branchCounts.terminals = size;
    }
    else
    {
      branchCounts.terminals = 0;
    }
    maximum -= branchCounts.terminals;

    // Get zero-suffixed branch
    if (maximum > 0)
    {
      assert Thread.currentThread().getStackTrace().length < 100;
      branchCounts.zeroCount = readZeroBranchSize(in, maximum, code);
    }
    maximum -= branchCounts.zeroCount;
    branchCounts.oneCount = maximum;
    return branchCounts;
  }

  protected long readTerminalCount(final BitInputStream in, final long size) throws IOException
  {
    if (CountTreeBitsCollection.SERIALIZATION_CHECKS)
    {
      in.expect(SerializationChecks.BeforeTerminal);
    }
    final long readBoundedLong = in.readBoundedLong(1 + size);
    if (CountTreeBitsCollection.SERIALIZATION_CHECKS)
    {
      in.expect(SerializationChecks.AfterTerminal);
    }
    return readBoundedLong;
  }

  protected long readZeroBranchSize(final BitInputStream in, final long max, final Bits code) throws IOException
  {
    if (0 == max) return 0;
    final long value;
    if (CountTreeBitsCollection.SERIALIZATION_CHECKS)
    {
      in.expect(SerializationChecks.BeforeCount);
    }
    if (this.useBinomials)
    {
      value = Gaussian.fromBinomial(0.5, max).decode(in, max);
    }
    else
    {
      value = in.readBoundedLong(1 + max);
    }
    if (CountTreeBitsCollection.SERIALIZATION_CHECKS)
    {
      in.expect(SerializationChecks.AfterCount);
    }
    return value;
  }

  public CountTreeBitsCollection setUseBinomials(final boolean useBinomials)
  {
    this.useBinomials = useBinomials;
    return this;
  }

  public long sum(final Collection<Long> values)
  {
    long total = 0;
    for (final Long v : values)
    {
      total += v;
    }
    return total;
  }

  public byte[] toBytes() throws IOException
  {
    final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
    final BitOutputStream out = new BitOutputStream(outBuffer);
    this.write(out);
    out.flush();
    return outBuffer.toByteArray();
  }

  public boolean useBinomials()
  {
    return this.useBinomials;
  }

  @Override
  public void write(final BitOutputStream out) throws IOException
  {
    final TreeMap<Bits, Long> sums = computeSums();
    final long value = 0 == sums.size() ? 0 : sums.lastEntry().getValue();
    out.writeVarLong(value);
    if (0 < value)
    {
      this.write(out, Bits.NULL, sums);
    }
  }

  private void write(final BitOutputStream out, final Bits currentCode, final NavigableMap<Bits, Long> sums) throws IOException
  {
    final Entry<Bits, Long> firstEntry = sums.firstEntry();
    final NavigableMap<Bits, Long> remainder = sums.tailMap(currentCode, false);
    final Bits splitCode = currentCode.concatenate(Bits.ONE);
    final NavigableMap<Bits, Long> zeroMap = remainder.headMap(splitCode, false);
    final NavigableMap<Bits, Long> oneMap = remainder.tailMap(splitCode, true);

    final int firstEntryCount = this.map.get(firstEntry.getKey()).get();
    final long baseCount = firstEntry.getValue() - firstEntryCount;
    final long endCount = sums.lastEntry().getValue();
    final long size = endCount - baseCount;

    final long terminals = firstEntry.getKey().equals(currentCode) ? firstEntryCount : 0;
    final long zeroCount = 0 == zeroMap.size() ? 0 : zeroMap.lastEntry().getValue() - baseCount - terminals;
    final long oneCount = size - terminals - zeroCount;

    final EntryTransformer<Bits, Long, Long> transformer = (key, value) -> (long) CountTreeBitsCollection.this.map.get(key).get();
    assert size == sum(Maps.transformEntries(sums, transformer).values());
    assert zeroCount == sum(Maps.transformEntries(zeroMap, transformer).values());
    assert oneCount == sum(Maps.transformEntries(oneMap, transformer).values());

    final BranchCounts branchCounts = new BranchCounts(currentCode, size, terminals, zeroCount, oneCount);

    if (CountTreeBitsCollection.SERIALIZATION_CHECKS)
    {
      out.write(SerializationChecks.StartTree);
    }
    writeBranchCounts(branchCounts, out);
    if (0 < zeroCount)
    {
      this.write(out, currentCode.concatenate(Bits.ZERO), zeroMap);
    }
    if (0 < oneCount)
    {
      this.write(out, currentCode.concatenate(Bits.ONE), oneMap);
    }
    if (CountTreeBitsCollection.SERIALIZATION_CHECKS)
    {
      out.write(SerializationChecks.EndTree);
    }
  }

  public void write(final BitOutputStream out, final int size) throws IOException
  {
    final TreeMap<Bits, Long> sums = computeSums();
    final long value = 0 == sums.size() ? 0 : sums.lastEntry().getValue();
    if (value != size) throw new RuntimeException();
    if (0 < value)
    {
      this.write(out, Bits.NULL, sums);
    }
  }

  protected void writeBranchCounts(final BranchCounts branch, final BitOutputStream out) throws IOException
  {
    final CodeType currentCodeType = getType(branch.path);
    long maximum = branch.size;
    assert maximum >= branch.terminals;
    if (currentCodeType == CodeType.Unknown)
    {
      writeTerminalCount(out, branch.terminals, maximum);
    }
    else if (currentCodeType == CodeType.Terminal)
    {
      assert branch.size == branch.terminals;
      assert 0 == branch.zeroCount;
      assert 0 == branch.oneCount;
    }
    else if (currentCodeType == CodeType.Prefix)
    {
      assert 0 == branch.terminals;
    }
    maximum -= branch.terminals;

    assert maximum >= branch.zeroCount;
    if (0 < maximum)
    {
      writeZeroBranchSize(out, branch.zeroCount, maximum, branch.path);
      maximum -= branch.zeroCount;
    }
    else
    {
      assert 0 == branch.zeroCount;
    }
    assert maximum == branch.oneCount;
  }

  protected void writeTerminalCount(final BitOutputStream out, final long value, final long max) throws IOException
  {
    assert 0 <= value;
    assert max >= value;
    if (CountTreeBitsCollection.SERIALIZATION_CHECKS)
    {
      out.write(SerializationChecks.BeforeTerminal);
    }
    out.writeBoundedLong(value, 1 + max);
    if (CountTreeBitsCollection.SERIALIZATION_CHECKS)
    {
      out.write(SerializationChecks.AfterTerminal);
    }
  }

  protected void writeZeroBranchSize(final BitOutputStream out, final long value, final long max, final Bits bits) throws IOException
  {
    assert 0 <= value;
    assert max >= value;
    if (CountTreeBitsCollection.SERIALIZATION_CHECKS)
    {
      out.write(SerializationChecks.BeforeCount);
    }
    if (this.useBinomials)
    {
      Gaussian.fromBinomial(0.5, max).encode(out, value, max);
    }
    else
    {
      out.writeBoundedLong(value, 1 + max);
    }
    if (CountTreeBitsCollection.SERIALIZATION_CHECKS)
    {
      out.write(SerializationChecks.AfterCount);
    }
  }

}