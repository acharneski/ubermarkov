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

The 
