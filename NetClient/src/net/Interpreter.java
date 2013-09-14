/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net;

import java.util.ArrayList;
import utilz.Utils;

/**
 *
 * @author Fazo
 */
public class Interpreter {

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
