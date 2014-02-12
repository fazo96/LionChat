/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilz.Utils;

/**
 *
 * @author fazo
 */
public class Security {

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
        //System.out.println("[LIB] Hash of " + x + ": " + hash);
        System.out.println("[LIB] Hash function has been called");
        return hash;
    }
}
