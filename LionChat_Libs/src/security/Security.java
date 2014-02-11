/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package security;

import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilz.Utils;

/**
 *
 * @author fazo
 */
public class Security {

    private KeyPair pair;
    private Signature signer;

    public Security() {
        this("DSA", "SHA1PRNG", "SHA1withDSA");
    }

    public Security(String keyAlgorithm, String randomAlgorithm, String signatureAlgorithm) {
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance(keyAlgorithm);
        } catch (NoSuchAlgorithmException ex) {
            // FATAL: CAN'T FIND ALGORITHM
            Logger.getLogger(Security.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-4);
        }
        SecureRandom random = null;
        try {
            random = SecureRandom.getInstance(randomAlgorithm);
        } catch (NoSuchAlgorithmException ex) {
            // FATAL: CAN'T FIND ALGORITHM
            Logger.getLogger(Security.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-5);
        }
        keyGen.initialize(1024, random);
        pair = keyGen.genKeyPair();
        try {
            signer = Signature.getInstance(signatureAlgorithm);
        } catch (NoSuchAlgorithmException ex) {
            // FATAL: CAN'T FIND ALGORITHM
            Logger.getLogger(Security.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-6);
        }
    }

    public KeyPair getKeyPair() {
        return pair;
    }

    /**
     * Returns SHA512 hash of the given string (without trimming the string).
     * IMPORTANT: newline characters are removed from the hash!
     *
     * @param x the string to hash.
     * @return the hash of the given string.
     */
    public static String hash(String x) {
        // I chose not to trim the string before hashing.
        String hash = null;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("[LIB] IMPOSSIBLE ERROR: SHA-512 NON EXISTANT!");
            return null;
        }
        md.reset();
        try {
            hash = new String(md.digest(x.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("[LIB] IMPOSSIBLE ERROR: UTF-8 NON EXISTANT!");
            return null;
        }
        if (hash.contains("\n")) {
            System.out.println("[LIB][WARNING] Had to remove \\n from string hash");
            hash = hash.replace("\n", ""); //rimuovo eventuali \n.
        }
        //System.out.println("[LIB] Hash of " + x + ": " + hash);
        System.out.println("[LIB] Hash function has been called");
        return hash;
    }
}
