package de.metanome.algorithms.anelosimus.bloom_filtering;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashFactory {

    public static final HashFactory Instance = new HashFactory();

    static final MessageDigest digestFunction;
    static {
        MessageDigest tmp;
        try {
            tmp = java.security.MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            tmp = null;
        }
        digestFunction = tmp;
    }

    public synchronized byte[] createHash(byte[] data, byte salt) {
        digestFunction.update(salt);
        salt++;
        return digestFunction.digest(data);
    }
}
