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
package net;

import UI.Client;
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
            Client.get().out().info(s);
        } else if (s.equals("/askKey")) {
            // If the server asks for the key, send it
            Client.get().getConnection().sendKey();
        } else {
            // Unknown command
            Client.get().out().error("[!] Server just sent a mysterious command:\n" + s);
        }
    }
}
