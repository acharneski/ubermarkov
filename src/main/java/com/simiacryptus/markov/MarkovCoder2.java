package com.simiacryptus.markov;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;
import com.simiacryptus.binary.BitInputStream;
import com.simiacryptus.binary.BitOutputStream;
import com.simiacryptus.binary.Bits;
import com.simiacryptus.bitset.CountTreeBitsCollection;
import com.simiacryptus.codes.Gaussian;
import com.simiacryptus.codes.HammingCode;
import com.simiacryptus.codes.HammingSymbol;
import com.simiacryptus.markov.visitors.RemoveZeroTerminals;

public class MarkovCoder2 extends MarkovCoder1
{
  
  public MarkovCoder2(final int depth)
  {
    super(depth);
  }
  
  @Override
  public MarkovModel<Character> decodeModel(final byte[] bytes) throws IOException
  {
    final MarkovModel<Character> dictionary = newModel();
    final BitInputStream bitStream = BitInputStream.toBitStream(bytes);
    
    for (int level = 0; level < dictionary.depth; level++)
    {
      final Collection<MarkovNode<Character>> levelNodes = getLevel(dictionary, level);
      for (final MarkovNode<Character> node : levelNodes)
      {
        final MarkovNode<Character> fallback = node.getFallback();
        if (null == fallback || node == fallback)
        {
          final CountTreeBitsCollection bitsCollection = new CountTreeBitsCollection(dictionary.charCoder.bitLength);
          bitsCollection.read(bitStream);
          for (final Entry<Bits, Integer> e : bitsCollection.getMap().entrySet())
          {
            final Bits bits = e.getKey();
            assert bits.bitLength == dictionary.charCoder.bitLength;
            final Character character = dictionary.charCoder.fromBits(bits);
            node.add(new MarkovPath<Character>(character), e.getValue());
          }
        } else
        {
          final HammingCode<Character> hammingCode = getRemainingCode(fallback);
          final CountTreeBitsCollection bitsCollection = getCodeCollection(hammingCode);
          bitsCollection.read(bitStream, node.getWeight());
          for (final Entry<Bits, Integer> e : bitsCollection.getMap().entrySet())
          {
            final Bits bits = e.getKey();
            final Character character = hammingCode.decode(bits).getValue();
            node.getChild(new MarkovPath<Character>(character)).setWeight(e.getValue());
          }
        }
      }
    }
    
    return dictionary;
  }
  
  @Override
  public byte[] encode(final MarkovModel<Character> dictionary) throws IOException
  {
    normalize(dictionary);
    final MarkovModel<Character> modelCopy = new MarkovModel<Character>(dictionary.depth);
    final MarkovNode<Character> copyChain = new DataNode<Character>(modelCopy);
    copyChain.setWeight(dictionary.root.getWeight());
    final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
    final BitOutputStream out = new BitOutputStream(outBuffer);
    
    for (int level = 0; level < dictionary.depth; level++)
    {
      final Collection<MarkovNode<Character>> levelNodes = getLevel(dictionary, level);
      for (final MarkovNode<Character> node : levelNodes)
      {
        final MarkovNode<Character> copyChild = copyChain.getChild(node.getPath());
        final MarkovNode<Character> fallback = node.getFallback();
        if (null == fallback || fallback == node)
        {
          CountTreeBitsCollection bitsCollection;
          bitsCollection = new CountTreeBitsCollection(dictionary.charCoder.bitLength);
          for (final MarkovNode<Character> child : node.getChildren().values())
          {
            final Bits bits = dictionary.charCoder.toBits(child.getKey());
            assert bits.bitLength == dictionary.charCoder.bitLength;
            bitsCollection.add(bits, child.getWeight());
            copyChild.getChild(child.getKey()).setWeight(child.getWeight());
          }
          bitsCollection.write(out);
        } else
        {
          final MarkovNode<Character> copyChildFallback = copyChild.getFallback();
          final HammingCode<Character> hammingCode = getRemainingCode(copyChildFallback);
          final CountTreeBitsCollection bitsCollection = getCodeCollection(hammingCode);
          for (final MarkovNode<Character> child : node.getChildren().values())
          {
            final Bits bits = hammingCode.encode(child.getKey());
            bitsCollection.add(bits, child.getWeight());
            copyChild.getChild(child.getKey()).setWeight(child.getWeight());
          }
          bitsCollection.write(out, node.getWeight());
        }
      }
    }
    
    out.flush();
    final byte[] bytes = outBuffer.toByteArray();
    assert this.verify(dictionary, bytes);
    return bytes;
  }
  
  protected CountTreeBitsCollection getCodeCollection(final HammingCode<Character> hammingCode)
  {
    final CountTreeBitsCollection bitsCollection = hammingCode.new HammingCodeCollection()
    {
      
      @Override
      protected long readZeroBranchSize(final BitInputStream in, final long total, final Bits code) throws IOException
      {
        if (0 == total)
          return 0;
        final long value;
        if (CountTreeBitsCollection.SERIALIZATION_CHECKS)
        {
          in.expect(SerializationChecks.BeforeCount);
        }
        
        final SortedMap<Bits, Character> zeroCodes = hammingCode.getCodes(code.concatenate(Bits.ZERO));
        final SortedMap<Bits, Character> oneCodes = hammingCode.getCodes(code.concatenate(Bits.ONE));
        long max = 0;
        long min = total;
        for (final Entry<Bits, Character> e : zeroCodes.entrySet())
        {
          max += hammingCode.getWeights().get(e.getValue());
        }
        for (final Entry<Bits, Character> e : oneCodes.entrySet())
        {
          min -= hammingCode.getWeights().get(e.getValue());
        }
        
        if (max > total)
        {
          max = total;
        }
        if (min < 0)
        {
          min = 0;
        }
        
        if (max > min)
        {
          Gaussian gaussian = Gaussian.fromBinomial(0.5, total);
          gaussian = new Gaussian(gaussian.mean - min, gaussian.stdDev);
          value = gaussian.decode(in, max - min) + min;
        } else
        {
          assert max == min;
          value = max;
        }
        
        if (CountTreeBitsCollection.SERIALIZATION_CHECKS)
        {
          in.expect(SerializationChecks.AfterCount);
        }
        return value;
      }
      
      @Override
      protected void writeZeroBranchSize(final BitOutputStream out, final long value, final long total, final Bits code) throws IOException
      {
        assert 0 <= value;
        assert total >= value;
        if (CountTreeBitsCollection.SERIALIZATION_CHECKS)
        {
          out.write(SerializationChecks.BeforeCount);
        }
        
        final SortedMap<Bits, Character> zeroCodes = hammingCode.getCodes(code.concatenate(Bits.ZERO));
        final SortedMap<Bits, Character> oneCodes = hammingCode.getCodes(code.concatenate(Bits.ONE));
        long max = 0;
        long min = total;
        for (final Entry<Bits, Character> e : zeroCodes.entrySet())
        {
          max += hammingCode.getWeights().get(e.getValue());
        }
        for (final Entry<Bits, Character> e : oneCodes.entrySet())
        {
          min -= hammingCode.getWeights().get(e.getValue());
        }
        if (max > total)
        {
          max = total;
        }
        if (min < 0)
        {
          min = 0;
        }
        
        if (max > min)
        {
          Gaussian gaussian = Gaussian.fromBinomial(0.5, total);
          gaussian = new Gaussian(gaussian.mean - min, gaussian.stdDev);
          gaussian.encode(out, value - min, max - min);
        } else
        {
          assert max == min;
        }
        
        if (CountTreeBitsCollection.SERIALIZATION_CHECKS)
        {
          out.write(SerializationChecks.AfterCount);
        }
      }
      
    };
    return bitsCollection;
  }
  
  protected Collection<MarkovNode<Character>> getLevel(final MarkovModel<Character> dictionary, final int level)
  {
    final TreeSet<MarkovNode<Character>> set = new TreeSet<MarkovNode<Character>>((o1, o2) -> {
      final int compareTo = ((Integer) o1.getWeight()).compareTo(o2.getWeight());
      if (0 != compareTo)
        return -compareTo;
      return o1.getPath().compareTo(o2.getPath());
    });
    set.addAll(MarkovUtil.getNodes(dictionary.root, level));
    return set;
  }
  
  protected HammingCode<Character> getRemainingCode(final MarkovNode<Character> fallback)
  {
    final TreeMap<Character, AtomicInteger> symbolCounts = new TreeMap<Character, AtomicInteger>(Maps.transformEntries(fallback.getChildren(),
        (EntryTransformer<Character, MarkovNode<Character>, AtomicInteger>) (key, value) -> new AtomicInteger(value.getWeight())));
    for (final MarkovNode<Character> c : fallback.getFallbackChildren().values())
    {
      for (final Entry<Character, ? extends MarkovNode<Character>> e : c.getChildren().entrySet())
      {
        symbolCounts.get(e.getKey()).addAndGet(-e.getValue().getWeight());
      }
    }
    final NavigableMap<Character, HammingSymbol<Character>> symbolMap = Maps.transformEntries(symbolCounts,
        (EntryTransformer<Character, AtomicInteger, HammingSymbol<Character>>) (key, value) -> new HammingSymbol<Character>(value.get(), key));
    final HammingCode<Character> hammingCode = new HammingCode<Character>(symbolMap.values());
    return hammingCode;
  }
  
  @Override
  protected boolean normalize(final MarkovModel<Character> dictionary)
  {
    final boolean normalize = super.normalize(dictionary);
    if (normalize)
    {
      RemoveZeroTerminals.run(dictionary.root);
    }
    return normalize;
  }
  
  @Override
  protected boolean verify(final MarkovModel<Character> dictionary, final byte[] bytes) throws IOException
  {
    final MarkovModel<Character> decompress = decodeModel(bytes);
    if (dictionary.root.isEquivalent(decompress.root))
      return true;
    else return false;
  }
  
}
