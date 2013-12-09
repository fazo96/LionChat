package net;

import java.util.ArrayList;
import utilz.Utils;

/**
 * Questa classe interpreta i comandi lato client.
 *
 * @author Fazo
 */
public class Interpreter {

    /**
     * "Prepara" una stringa per l'invio al server, ad esempio invia l'hash
     * della password invece della password vera e propria, TUTTE le stringe
     * dovrebbero essere fatte passare per questo metodo prima di essere
     * inviate!
     *
     * @param s la stringa da "preparare"
     * @return la stringa preparata.
     */
    public static String fixToSend(String s) {
        ArrayList<String> ss = Utils.toList(s, " ");
        //è un login/registrazione/cambio pass: invio hash invece di password
        if ((s.startsWith("/login") || s.startsWith("/password")) && ss.size() == 3) {
            ss.set(2, Utils.getSecureHash(ss.get(2))); //rimpiazzo la password con il suo hash
        }
        return Utils.fromList(ss, " ");
    }

    public static void cmd(String s) { //interpreto comando client
    }
}
