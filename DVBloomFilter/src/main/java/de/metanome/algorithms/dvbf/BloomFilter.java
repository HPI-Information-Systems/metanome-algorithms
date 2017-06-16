package de.metanome.algorithms.dvbf;
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 *@author Hazar.Harmouch
 *
 *Updated to provide cardinality estimation
 *Referances:
 *S. J. Swamidass and P. Baldi. Mathematical correction for ngerprint similarity measures to improve chemical retrieval. Journal of chemical information and modeling, 47(3):952-964, 2007
 *O. Papapetrou, W. Siberski, and W. Nejdl. Cardinality estimation and dynamic length adaptation for Bloom filters. Distributed and Parallel Databases, 28(2):119{156, 2010
 *
 */


import java.io.UnsupportedEncodingException;
import java.util.BitSet;

public class BloomFilter {

  private BitSet filter_; //the bitmap
  int hashCount; //number of hash functions


  public BloomFilter(int numElements, int bucketsPerElement) {
    int nbits=numElements * bucketsPerElement + 20;
    if(nbits<0)
    {nbits=Integer.MAX_VALUE;}
    hashCount=BloomCalculations.computeBestK(bucketsPerElement);
    filter_ =   new BitSet(nbits);
  }


//-----------------------------------------------------------
//empty the filter  
  public void clear() {
    filter_.clear();
  }

  //size of the bloom filter
  public int buckets() {
    return filter_.size();
  }

  //number of 0 bits
  int emptyBuckets() {
    int n = 0;
    for (int i = 0; i < buckets(); i++) {
      if (!filter_.get(i)) {
        n++;
      }
    }
    return n;
  }
//number of 1 bits
  int fullBuckets() {
    int n = 0;
    for (int i = 0; i < buckets(); i++) {
      if (filter_.get(i)) {
        n++;
      }
    }
    return n;
  }

 //------------------membership test----------------------------
  public boolean isPresent(String key) {
    for (int bucketIndex : getHashBuckets(key)) {
      if (!filter_.get(bucketIndex)) {
        return false;
      }
    }
    return true;
  }
//----------------------add elements to the filter--------------------
  /*
   * @param key -- value whose hash is used to fill the filter_. This is a general purpose API.
   */
  public void add(String key) {
    if(key!=null)
    for (int bucketIndex : getHashBuckets(key)) {
      filter_.set(bucketIndex);
    }
  }

//-----------------------------------------------

  public int getHashCount() {
    return hashCount;
  }

  BitSet filter() {
    return filter_;
  }

//----------------------------------------------
  // Murmur is faster than an SHA-based approach and provides as-good collision
  // resistance. The combinatorial generation approach described in
  // http://www.eecs.harvard.edu/~kirsch/pubs/bbbf/esa06.pdf
  // does prove to work in actual tests, and is obviously faster
  // than performing further iterations of murmur.
  int[] getHashBuckets(String key, int hashCount, int max) {
    byte[] b;
    try {
      b = key.getBytes("UTF-16");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    return getHashBuckets(b, hashCount, max);
  }

  int[] getHashBuckets(byte[] b, int hashCount, int max) {
    int[] result = new int[hashCount];
    int hash1 = MurmurHash.hash(b, b.length, 0);
    int hash2 = MurmurHash.hash(b, b.length, hash1);
    for (int i = 0; i < hashCount; i++) {
      result[i] = Math.abs((hash1 + i * hash2) % max);
    }
    return result;
  }

  public int[] getHashBuckets(String key) {
    return getHashBuckets(key, hashCount, buckets());
  }


  public long cardinality_Swamidass() 
  { int b=buckets();
   int m=hashCount;
   int x=fullBuckets();
       return (long) (-1* b/m* Math.log(1 - x / ((double) b))); }

  public long cardinality_Papapetrou() 
  {int b=buckets();
  int m=hashCount;
  int x=fullBuckets();
  return (long) (    (Math.log(1 - x / ((double) b)))    / (m  *   (Math.log(1 - 1 / ((double) b))))) ; }

}
