package net;

import interf.GUI;
import java.util.ArrayList;
import utilz.Utils;

/**
 * This class performs (badly coded) parsing.
 *
 * @author Fazo
 */
public class Interpreter {

    /**
     * "Prepares" a string to send it to the server, for example by writing
     * hashes instead of passwords, or by trimming white space in the borders.
     * Every string must be run through this method before being sent.
     *
     * @param s the string to prepare
     * @return prepared string.
     */
    public static String fixToSend(String s) {
        ArrayList<String> ss = Utils.toList(s, " ");
        //è un login/registrazione/cambio pass: invio hash invece di password
        if ((s.startsWith("/login") || s.startsWith("/password")) && ss.size() == 3) {
            ss.set(2, Utils.getSecureHash(ss.get(2))); //rimpiazzo la password con il suo hash
        }
        if (s.startsWith("/password") && ss.size() == 3) {
            //rimpiazzo le password con il loro hash
            ss.set(1, Utils.getSecureHash(ss.get(1)));
            ss.set(2, Utils.getSecureHash(ss.get(2)));
        }
        return Utils.fromList(ss, " ").trim();
    }

    /**
     * Parses string. Intended to be used for strings received by the server. If
     * it's a command, it executes it, if it's a message, it prints it.
     *
     * @param s string to parse
     */
    public static void cmd(String s) {
        s = s.trim();
        if (!s.startsWith("/")) {
            GUI.get().append(s);
            return;
        } else {
            // There are not yet server commands.
            System.out.println("[!] Server just sent a mysterious command!");
        }
    }
}
