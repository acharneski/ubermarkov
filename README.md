# ubermarkov

Some experiments with markov chains, with applications to text compression

## Background

Markov chains are well known tools to model text by breaking the text down into N-tuples and forming a lookup index to infer from the (N-1) preceeding characters, what the following character will be.

Text compression, outside the LZ-family compressors, can be achieved using the statistical model given by markov trees using a variety of encoding mechanisms. This type of compression is generally known as [PPM](https://en.wikipedia.org/wiki/Prediction_by_partial_matching) coding. This coding can be achived via [Arithmetic Coding](https://en.wikipedia.org/wiki/Arithmetic_coding), [Huffman Codes](https://en.wikipedia.org/wiki/Huffman_coding), [Hu-Tucker Codes](http://www-math.mit.edu/~shor/PAM/hu-tucker_algorithm.html) and similar. This is not widely used for a variety of reasons, one of which is that once you've encoded your text, you still need to store your markov tree to decode it.

## Introduction

This project explores text compression using a PPM scheme, with particular focus on encoding the markov tree that models the text. As a result, our program can encode text into extremely small byte strings, and encode the entropy captured in the markov tree very efficiently.

The main encoding for the tree happens here: https://github.com/acharneski/ubermarkov/blob/5f2361f90e9282a0ba4a5373489060268810bddc/src/main/java/com/simiacryptus/markov/MarkovCoder2.java#L81
Essentially, it models a markov tree as a series of 1-level-shallower trees, which are used to extrapolate expectations about the next phase of encoding (which handles the next layer of tree).

## Running it

The project can be run via junit tests, for example a benchmark test run against a [git repository](https://github.com/acharneski/ubermarkov/blob/5f2361f90e9282a0ba4a5373489060268810bddc/src/test/java/com/simiacryptus/markov/MarkovTest.java#L266) or a [wikipedia dump](https://github.com/acharneski/ubermarkov/blob/5f2361f90e9282a0ba4a5373489060268810bddc/src/test/java/com/simiacryptus/markov/MarkovTest.java#L55).

Example output looks like:

```
Studying f9797cafd33accd73b7b2b622cf2d73e5fc470da/src/com/esotericsoftware/kryo/Generics.java
Compressing f9797cafd33accd73b7b2b622cf2d73e5fc470da/src/com/esotericsoftware/kryo/Generics.java (3255 bytes)...613 bytes (81.167%)...verified!
Markov chain compressed to 2092 bytes...verified markov tree encoding!
Studying f9797cafd33accd73b7b2b622cf2d73e5fc470da/src/com/esotericsoftware/kryo/Kryo.java
Compressing f9797cafd33accd73b7b2b622cf2d73e5fc470da/src/com/esotericsoftware/kryo/Kryo.java (55701 bytes)...12968 bytes (76.719%)...verified!
Markov chain compressed to 5390 bytes...verified markov tree encoding!
```

This says that each blob, i.e. Kryo.java, was compressed from 55KB to 12.9KB, or 76% space reduction. Additionally, the dictionary that is being (cumulatively) used was encoded in 5KB. 

In this benchmark the dictionary is shared and thus the cost of encoding it for benchmark purposes could be shared, but even counting the dictionary and this single item, the encoding is only 18k to compress 55k, or 3x compression. Other settings, with other datasets, yield much higher results. Overall, the compression performance of this method appears to be quite good. However, other characteristics such as memory use and cpu cost, will likely prohibit this from being a directly popular method.

## See also

1. https://github.com/acharneski/lztree - Another compression-related research project 
