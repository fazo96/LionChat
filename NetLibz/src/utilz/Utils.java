/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilz;

import java.util.ArrayList;

/**
 *
 * @author fazo
 */
public class Utils {

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
