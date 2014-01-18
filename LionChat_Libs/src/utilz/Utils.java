package utilz;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility varie.
 *
 * @author fazo
 */
public class Utils {

    /**
     * Ritorna l'hash SHA-512 della stringa data in formato stringa standard
     * (UTF-8).
     *
     * @param x la stringa da usare per creare l'hash.
     * @return l'hash della stringa data.
     */
    public static String getSecureHash(String x) {
        //Questa funzione ritorna l'hash SHA1 di una stringa, già convertito in stringa UTF-8
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
        System.out.println("[LIB] Hash of " + x + ": " + hash);
        return hash;
    }

    /**
     * Converte i nanosecondi dati in secondi.
     *
     * @param n nanosecondi da convertire in formato Long.
     * @return la quantità in secondi.
     */
    public static int nanoToSec(Long n) {
        //fa una semplice divisione: da nanoscondi a secondi, passando da Long a Int
        return (int) (n / 1000000000);
    }

    /**
     * Ritorna se la stringa è valida: una stringa è considerata valida se non
     * contiene solo spazi e non è vuota.
     *
     * @param s la stringa da controllare
     * @return true se è valida.
     */
    public static boolean isValid(String s) {
        if (s == null) {
            return false;
        }
        if (s.length() == 0) {
            return false;
        }
        for (char c : s.toCharArray()) {
            if (c != ' ') {
                return true;
            }
        }
        return false;
    }

    /**
     * Trasforma una stringa in una lista di stringhe.
     *
     * @param s la stringa da trasformare.
     * @param regex il divisore da usare per dividere la stringa in parti.
     * @return la lista di stringhe.
     */
    public static ArrayList<String> toList(String s, String regex) { //passa da una stringa a una lista.
        if (regex == null) { //il divisore di default è uno spazio
            regex = " ";
        }
        if (s == null) { //se la stringa è nulla, la lista sarà nulla
            return null;
        }
        String b[] = s.split(regex); //divido la stirnga con il regex e la metto in un array
        if(b.length==0)return null;
        ArrayList<String> content = new ArrayList<String>(b.length);
        for (String c : b) { //inserisco gli elementi dell'array nell'array dinamico
            content.add(c);
        }
        return content; //restituisco array dinamico
    }

    /**
     * Aggiunge gli elementi di B in A.
     *
     * @param a la lista a cui aggiungere gli elementi.
     * @param b la lista da cui aggiungere gli elementi.
     */
    public static void mergeLists(ArrayList<String> a, ArrayList<String> b) {
        //copia il contenuto di B in a
        for (String c : b) { //aggiungo gli elementi di b ad a
            if (!a.contains(c)) {
                a.add(c);
            }
        }
    }

    /**
     * Trasforma una lista di stringhe in una stringa unica.
     *
     * @param ss la lista da trasformare
     * @param divisor i caratteri da inserire tra un elemento e l'altro.
     * @return la stringa ottenuta.
     */
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
