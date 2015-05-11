package com.simiacryptus.markov;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import junit.framework.Assert;

import com.simiacryptus.binary.BitInputStream;
import com.simiacryptus.binary.BitOutputStream;
import com.simiacryptus.binary.Bits;
import com.simiacryptus.bitset.CountTreeBitsCollection;
import com.simiacryptus.codes.HammingCode;
import com.simiacryptus.markov.visitors.VerifyMarkovChainProperties;

public class MarkovCoder1 implements MarkovCoder
{
  
  private enum MarkovPredictionChecks
  {
    EndChar, StartChar
  }
  
  private static final boolean SERIALIZATION_CHECKS = false;
  
  @SuppressWarnings("unchecked")
  public static <T extends Comparable<T>> TreeMap<MarkovPath<T>, Long> getChains(final MarkovNode<T> markovNode)
  {
    final TreeMap<MarkovPath<T>, Long> map = new TreeMap<MarkovPath<T>, Long>();
    long terminalCount = markovNode.getWeight();
    for (final MarkovNode<T> c : markovNode.getChildren().values())
    {
      terminalCount -= c.getWeight();
      for (final Entry<MarkovPath<T>, Long> e : MarkovCoder1.getChains(c).entrySet())
      {
        final ArrayList<T> path = new ArrayList<T>();
        path.add(c.getKey());
        path.addAll(e.getKey().path);
        map.put(new MarkovPath<T>(path), e.getValue());
      }
    }
    if (0 < terminalCount)
    {
      map.put(new MarkovPath<T>(), terminalCount);
    }
    return map;
  }
  
  public final int depth;
  
  public MarkovCoder1(final int depth)
  {
    super();
    this.depth = depth;
  }
  
  /*
   * (non-Javadoc)
   * @see com.simiacryptus.prototype.markov.MarkovCoder#decode(com.simiacryptus.prototype .markov.MarkovModel, byte[])
   */
  @Override
  public MarkovModel<Character> decodeModel(final byte[] bytes) throws IOException
  {
    final MarkovModel<Character> dictionary = newModel();
    final CountTreeBitsCollection bitsCollection = new CountTreeBitsCollection(bytes, dictionary.depth * dictionary.charCoder.bitLength);
    populateModel(bitsCollection, dictionary);
    return dictionary;
  }
  
  /*
   * (non-Javadoc)
   * @see com.simiacryptus.prototype.markov.MarkovCoder#encode(com.simiacryptus.prototype .markov.MarkovModel)
   */
  @Override
  public byte[] encode(final MarkovModel<Character> dictionary) throws IOException
  {
    normalize(dictionary);
    final int bitDepth = dictionary.depth * dictionary.charCoder.bitLength;
    final CountTreeBitsCollection bitsCollection = new CountTreeBitsCollection(bitDepth);
    populateBitsCollection(dictionary, bitsCollection);
    final byte[] bytes = bitsCollection.toBytes();
    assert this.verify(dictionary, bytes);
    return bytes;
  }
  
  /*
   * (non-Javadoc)
   * @see com.simiacryptus.prototype.markov.MarkovCoder#encode(com.simiacryptus.prototype .markov.MarkovModel, java.io.File)
   */
  @Override
  public byte[] encode(final MarkovModel<Character> dictionary, final File file) throws FileNotFoundException, IOException
  {
    normalize(dictionary);
    final byte[] byteArray;
    {
      final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
      final InputStreamReader reader = new InputStreamReader(new FileInputStream(file), Charset.defaultCharset());
      final BitOutputStream out = new BitOutputStream(outBuffer);
      try
      {
        int read;
        final LinkedList<Character> sequence = new LinkedList<Character>();
        while (sequence.size() < dictionary.depth - 1)
        {
          sequence.add(dictionary.charCoder.nullValue);
        }
        while (0 <= (read = reader.read()))
        {
          final HammingCode<Character> hammingCode = MarkovUtil.getHammingCode(dictionary.root.getChild(new MarkovPath<Character>(sequence)));
          final char c = (char) read;
          final Bits encode = hammingCode.encode(c);
          assert hammingCode.decode(encode).equals(c) || hammingCode.verifyIndexes();
          if (MarkovCoder1.SERIALIZATION_CHECKS)
          {
            out.write(MarkovPredictionChecks.StartChar);
          }
          out.write(encode);
          if (MarkovCoder1.SERIALIZATION_CHECKS)
          {
            out.write(MarkovPredictionChecks.EndChar);
          }
          sequence.add(c);
          if (sequence.size() > dictionary.depth - 1)
          {
            sequence.remove();
          }
        }
      } finally
      {
        reader.close();
        out.flush();
        byteArray = outBuffer.toByteArray();
      }
    }
    return byteArray;
  }
  
  @Override
  public MarkovModel<Character> newModel()
  {
    return new MarkovModel<Character>(this.depth);
  }
  
  protected boolean normalize(final MarkovModel<Character> dictionary)
  {
    if (dictionary.isNormalized)
      return false;
    dictionary.isNormalized = true;
    VerifyMarkovChainProperties.run(dictionary.root);
    return true;
  }
  
  protected void populateBitsCollection(final MarkovModel<Character> dictionary, final CountTreeBitsCollection bitsCollection)
  {
    for (final Entry<MarkovPath<Character>, Long> l : MarkovCoder1.getChains(dictionary.root).entrySet())
    {
      Bits bits = Bits.NULL;
      for (final Character c : l.getKey().path)
      {
        bits = bits.concatenate(dictionary.charCoder.toBits(c));
      }
      assert bits.bitLength == bitsCollection.bitDepth;
      bitsCollection.add(bits, (int) (long) l.getValue());
    }
  }
  
  protected void populateModel(final CountTreeBitsCollection bitsCollection, final MarkovModel<Character> dictionary)
  {
    for (final Entry<Bits, Integer> e : bitsCollection.getMap().entrySet())
    {
      final Bits bits = e.getKey();
      assert bits.bitLength == bitsCollection.bitDepth;
      final List<Character> sequence = new ArrayList<Character>();
      for (int i = 0; i < bits.bitLength / dictionary.charCoder.bitLength; i++)
      {
        final Bits range = bits.range(i * dictionary.charCoder.bitLength, dictionary.charCoder.bitLength);
        final Character valueOf = dictionary.charCoder.fromBits(range);
        sequence.add(valueOf);
      }
      dictionary.add(sequence, e.getValue());
    }
  }
  
  /*
   * (non-Javadoc)
   * @see com.simiacryptus.prototype.markov.MarkovCoder#study(com.simiacryptus.prototype .markov.MarkovModel, java.io.File)
   */
  @Override
  public void study(final MarkovModel<Character> dictionary, final File file) throws FileNotFoundException, IOException
  {
    long position = 0;
    final InputStreamReader reader = new InputStreamReader(new FileInputStream(file), Charset.defaultCharset());
    try
    {
      dictionary.isNormalized = false;
      int read;
      final LinkedList<Character> sequence = new LinkedList<Character>();
      while (sequence.size() < dictionary.depth)
      {
        sequence.add(dictionary.charCoder.nullValue);
      }
      while (0 <= (read = reader.read()))
      {
        final char c = (char) read;
        sequence.add(c);
        if (sequence.size() > dictionary.depth)
        {
          sequence.remove();
        }
        if (sequence.size() == dictionary.depth)
        {
          dictionary.add(sequence, position++);
        }
      }
      while (!sequence.get(0).equals(dictionary.charCoder.nullValue))
      {
        sequence.add(dictionary.charCoder.nullValue);
        if (sequence.size() > dictionary.depth)
        {
          sequence.remove();
        }
        dictionary.add(sequence, position++);
      }
    } finally
    {
      reader.close();
    }
  }
  
  protected boolean verify(final MarkovModel<Character> dictionary, final byte[] bytes) throws IOException
  {
    final CountTreeBitsCollection bitsCollection = new CountTreeBitsCollection(bytes, dictionary.depth * dictionary.charCoder.bitLength);
    final MarkovModel<Character> decompress = newModel();
    populateModel(bitsCollection, decompress);
    assert dictionary.root.equals(decompress.root);
    return dictionary.root.equals(decompress.root);
  }
  
  /*
   * (non-Javadoc)
   * @see com.simiacryptus.prototype.markov.MarkovCoder#verify(com.simiacryptus.prototype .markov.MarkovModel, java.io.File, byte[])
   */
  @Override
  public void verify(final MarkovModel<Character> dictionary, final File file, final byte[] byteArray) throws FileNotFoundException, IOException
  {
    normalize(dictionary);
    final InputStreamReader reader = new InputStreamReader(new FileInputStream(file), Charset.defaultCharset());
    final BitInputStream in = new BitInputStream(new ByteArrayInputStream(byteArray));
    try
    {
      int read;
      final LinkedList<Character> sequence = new LinkedList<Character>();
      while (sequence.size() < dictionary.depth - 1)
      {
        sequence.add(dictionary.charCoder.nullValue);
      }
      while (0 <= (read = reader.read()))
      {
        final HammingCode<Character> hammingCode = MarkovUtil.getHammingCode(dictionary.root.getChild(new MarkovPath<Character>(sequence)));
        if (MarkovCoder1.SERIALIZATION_CHECKS)
        {
          in.expect(MarkovPredictionChecks.StartChar);
        }
        final char expected = hammingCode.decode(in);
        if (MarkovCoder1.SERIALIZATION_CHECKS)
        {
          in.expect(MarkovPredictionChecks.EndChar);
        }
        final char c = (char) read;
        Assert.assertEquals(expected, c);
        sequence.add(c);
        if (sequence.size() > dictionary.depth - 1)
        {
          sequence.remove();
        }
      }
    } finally
    {
      reader.close();
      in.close();
    }
  }
}