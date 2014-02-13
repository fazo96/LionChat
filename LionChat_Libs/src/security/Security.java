/*
 LionChat
 Copyright (C) 2014 Enrico Fasoli ( fazius2009 at gmail dot com )

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
