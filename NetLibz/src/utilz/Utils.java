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

    public static int nanoToSec(Long n) { //fa una semplice divisione: da nanoscondi a secondi
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
        if (regex == null) {
            regex = " ";
        }
        if (s == null) {
            return null;
        }
        ArrayList<String> content = new ArrayList<String>();
        String b[] = s.split(regex);
        for (String c : b) {
            content.add(c);
        }
        return content;
    }

    public static void mergeLists(ArrayList<String> a, ArrayList<String> b) {
        //copia il contenuto di B in a
        for (String c : b) {
            if (!a.contains(c)) {
                a.add(c);
            }
        }
    }

    public static String fromList(ArrayList<String> ss, String divisor) { //passa da una lista a una stringa
        if (ss == null) {
            return null;
        }
        if (divisor == null) {
            divisor = " ";
        }
        String a = ss.get(0);
        for (int i = 1; i < ss.size(); i++) {
            a += divisor + ss.get(i);
        }
        return a;
    }
}
