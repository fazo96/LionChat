/*
 LionChat Server/Client library program
 Copyright (C) 2013  Enrico Fasoli

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
 */

package utilz;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fazo
 */
public class Utils {
    public static String getSecureHash(String x){
        //Questa funzione ritorna l'hash SHA1 di una stringa, già convertito in stringa UTF-8
        String hash=null;
        MessageDigest md=null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("[LIB] ERRORE IMPOSSIBILE: SHA-1 NON ESISTE!");
            return null;
        }
        md.reset();
        try {
            hash = new String(md.digest(x.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("[LIB] ERRORE IMPOSSIBILE: UTF-8 NON ESISTE!");
            return null;
        }
        System.out.println("[LIB] Hash di "+x+": "+hash);
        return hash;
    }
    public static int nanoToSec(Long n) { 
        //fa una semplice divisione: da nanoscondi a secondi, passando da Long a Int
        return (int) (n / 1000000000);
    }

    public static boolean isValid(String s) {
        for (char c : s.toCharArray()) {
            if (c != ' ') {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<String> toList(String s, String regex) { //passa da una stringa a una lista.
        if (regex == null) { //il divisore di default è uno spazio
            regex = " ";
        }
        if (s == null) { //se la stringa è nulla, la lista sarà nulla
            return null;
        }
        String b[] = s.split(regex); //divido la stirnga con il regex e la metto in un array
        ArrayList<String> content = new ArrayList<String>(b.length);
        for (String c : b) { //inserisco gli elementi dell'array nell'array dinamico
            content.add(c);
        }
        return content; //restituisco array dinamico
    }

    public static void mergeLists(ArrayList<String> a, ArrayList<String> b) {
        //copia il contenuto di B in a
        for (String c : b) { //aggiungo gli elementi di b ad a
            if (!a.contains(c)) {
                a.add(c);
            }
        }
    }

    public static String fromList(ArrayList<String> ss, String divisor) { 
        //passa da una lista a una stringa
        if (ss == null) { //lista nulla = stringa nulla
            return null;
        }
        if (divisor == null) { //imposto divisore default come uno spazio
            divisor = " ";
        }
        String a = ss.get(0);//aggiungo il primo elemento
        for (int i = 1; i < ss.size(); i++) { 
            //aggiungo uno spazio e l'elemento i
            a += divisor + ss.get(i);
        }
        return a; //ritorno la stringa ricomposta
    }
}
