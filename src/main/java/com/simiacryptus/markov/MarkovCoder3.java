package com.simiacryptus.markov;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;
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
import com.simiacryptus.markov.visitors.VerifyMarkovChainProperties;

public class MarkovCoder3 extends MarkovCoder1
{
  
  public MarkovCoder3(final int depth)
  {
    super(depth);
  }
  
  @Override
  public MarkovModel<Character> decodeModel(final byte[] bytes) throws IOException
  {
    final MarkovModel<Character> dictionary = this.newModel();
    final BitInputStream bitStream = BitInputStream.toBitStream(bytes);
    
    for (int level = 0; level < dictionary.depth; level++)
    {
      for (final MarkovNode<Character> node : MarkovUtil.getNodes(dictionary.root, level))
      {
        final MarkovNode<Character> fallback = node.getFallback();
        if (null == fallback || node == fallback)
        {
          final CountTreeBitsCollection bitsCollection = new CountTreeBitsCollection(
              dictionary.charCoder.bitLength);
          bitsCollection.read(bitStream);
          for (final Entry<Bits, Integer> e : bitsCollection.getMap()
              .entrySet())
          {
            final Bits bits = e.getKey();
            assert bits.bitLength == dictionary.charCoder.bitLength;
            final Character character = dictionary.charCoder.fromBits(bits);
            node.add(new MarkovPath<Character>(character), e.getValue());
          }
          node.setWeight(1);
        }
        else
        {
          final HammingCode<Character> hammingCode = this
              .getRemainingCode(fallback);
          final CountTreeBitsCollection bitsCollection = this
              .getCodeCollection(hammingCode);
          bitsCollection.read(bitStream,
              (int) bitStream.readBoundedLong(hammingCode.codeSize() + 1));
          for (final Entry<Bits, Integer> e : bitsCollection.getMap()
              .entrySet())
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
    this.normalize(dictionary);
    MarkovModel<Character> modelCopy = new MarkovModel<Character>(dictionary.depth);
    final MarkovNode<Character> copyChain = new DataNode<Character>(modelCopy);
    copyChain.setWeight(dictionary.root.getWeight());
    final ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
    final BitOutputStream out = new BitOutputStream(outBuffer);
    
    for (int level = 0; level < dictionary.depth; level++)
    {
      for (final MarkovNode<Character> node : MarkovUtil.getNodes(dictionary.root, level))
      {
        final MarkovNode<Character> copyChild = copyChain.getChild(node.getPath());
        final MarkovNode<Character> fallback = node.getFallback();
        if (null == fallback || fallback == node)
        {
          CountTreeBitsCollection bitsCollection;
          bitsCollection = new CountTreeBitsCollection(
              dictionary.charCoder.bitLength);
          for (final MarkovNode<Character> child : node.getChildren().values())
          {
            final Bits bits = dictionary.charCoder.toBits(child.getKey());
            assert bits.bitLength == dictionary.charCoder.bitLength;
            bitsCollection.add(bits, child.getWeight());
            copyChild.getChild(child.getKey()).setWeight(child.getWeight());
          }
          bitsCollection.write(out);
        }
        else
        {
          MarkovNode<Character> fallback2 = copyChild.getFallback();
          final HammingCode<Character> hammingCode = this
              .getRemainingCode(fallback2);
          final CountTreeBitsCollection bitsCollection = this
              .getCodeCollection(hammingCode);
          long sum = 0;
          for (final MarkovNode<Character> child : node.getChildren().values())
          {
            Character key = child.getKey();
            final Bits bits = hammingCode.encode(key);
            bitsCollection.add(bits, child.getWeight());
            copyChild.getChild(key).setWeight(child.getWeight());
            sum += child.getWeight();
          }
          out.writeBoundedLong(sum, hammingCode.codeSize() + 1);
          bitsCollection.write(out, (int) sum);
        }
      }
    }
    
    out.flush();
    final byte[] bytes = outBuffer.toByteArray();
    assert this.verify(dictionary, bytes);
    return bytes;
  }
  
  protected CountTreeBitsCollection getCodeCollection(
      final HammingCode<Character> hammingCode)
  {
    final CountTreeBitsCollection bitsCollection = hammingCode.new HammingCodeCollection() {
      
      @Override
      protected long readZeroBranchSize(final BitInputStream in,
          final long total, final Bits code) throws IOException
      {
        if (0 == total) { return 0; }
        final long value;
        if (SERIALIZATION_CHECKS)
        {
          in.expect(SerializationChecks.BeforeCount);
        }
        
        final SortedMap<Bits, Character> zeroCodes = hammingCode.getCodes(code
            .concatenate(Bits.ZERO));
        final SortedMap<Bits, Character> oneCodes = hammingCode.getCodes(code
            .concatenate(Bits.ONE));
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
        }
        else
        {
          assert max == min;
          value = max;
        }
        
        if (SERIALIZATION_CHECKS)
        {
          in.expect(SerializationChecks.AfterCount);
        }
        return value;
      }
      
      @Override
      protected void writeZeroBranchSize(final BitOutputStream out,
          final long value, final long total, final Bits code)
          throws IOException
      {
        assert 0 <= value;
        assert total >= value;
        if (SERIALIZATION_CHECKS)
        {
          out.write(SerializationChecks.BeforeCount);
        }
        
        final SortedMap<Bits, Character> zeroCodes = hammingCode.getCodes(code
            .concatenate(Bits.ZERO));
        final SortedMap<Bits, Character> oneCodes = hammingCode.getCodes(code
            .concatenate(Bits.ONE));
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
        }
        else
        {
          assert max == min;
        }
        
        if (SERIALIZATION_CHECKS)
        {
          out.write(SerializationChecks.AfterCount);
        }
      }
      
    };
    return bitsCollection;
  }
  
  protected HammingCode<Character> getRemainingCode(
      final MarkovNode<Character> fallback)
  {
    final TreeMap<Character, AtomicInteger> symbolCounts = new TreeMap<Character, AtomicInteger>(
        Maps.transformEntries(
            fallback.getChildren(),
            new EntryTransformer<Character, MarkovNode<Character>, AtomicInteger>() {
              @Override
              public AtomicInteger transformEntry(final Character key,
                  final MarkovNode<Character> value)
              {
                return new AtomicInteger(value.getWeight());
              }
            }));
    final NavigableMap<Character, HammingSymbol<Character>> symbolMap = Maps
        .transformEntries(
            symbolCounts,
            new EntryTransformer<Character, AtomicInteger, HammingSymbol<Character>>() {
              @Override
              public HammingSymbol<Character> transformEntry(
                  final Character key, final AtomicInteger value)
              {
                return new HammingSymbol<Character>(value.get(), key);
              }
            });
    return new HammingCode<Character>(symbolMap.values());
  }
  
  @Override
  protected boolean normalize(final MarkovModel<Character> dictionary)
  {
    if (dictionary.isNormalized) { return false; }
    dictionary.isNormalized = true;
    final VerifyMarkovChainProperties verifyMarkovChainProperties = new VerifyMarkovChainProperties();
    verifyMarkovChainProperties.verifyCountSums = false;
    verifyMarkovChainProperties.visitUp(dictionary.root);
    RemoveZeroTerminals.run(dictionary.root);
    new MarkovVisitor<Character>() {
      @Override
      public void visit(final MarkovNode<Character> node)
      {
        node.setWeight(1);
      }
    }.visitUp(dictionary.root);
    return true;
  }
  
  @Override
  protected boolean verify(final MarkovModel<Character> dictionary, final byte[] bytes)
      throws IOException
  {
    final MarkovModel<Character> decompress = this.decodeModel(bytes);
    if(dictionary.root.isEquivalent(decompress.root))
    {
      return true;
    }
    else
    {
      return false;
    }
  }
  
}
