package net;

import interf.GUI;
import java.util.ArrayList;
import security.Security;
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
        // don't send password, send hash instead to protect privacy
        if ((s.startsWith("/login") || s.startsWith("/password")) && ss.size() == 3) {
            ss.set(2, Security.hash(ss.get(2)));
        }
        if (s.startsWith("/password") && ss.size() == 3) {
            ss.set(1, Security.hash(ss.get(1)));
            ss.set(2, Security.hash(ss.get(2)));
        }
        return Utils.fromList(ss, " ");
    }

    /**
     * Parses string. Intended to be used for strings received by the server. If
     * it's a command, it executes it, if it's a message, it prints it.
     *
     * @param s string to parse
     */
    public static void cmd(String s) {
        //s = s.trim();
        if (!s.startsWith("/")) {
            GUI.get().append(s);
            return;
        } else {
            // There are not yet server commands.
            System.out.println("[!] Server just sent a mysterious command!");
        }
    }
}
