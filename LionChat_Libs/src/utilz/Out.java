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
package utilz;

import java.io.PrintStream;
import java.util.ArrayList;

/**
 * This class handles output messages
 *
 * @author fazo
 */
public class Out {

    private ArrayList<IOListener> listeners;
    private boolean writeOnConsole = true;

    /**
     * Create a new Output Handler that also writes to Stdout and Stderr by
     * default.
     */
    public Out() {
        listeners = new ArrayList<IOListener>();
    }

    /**
     * Write an error message to all the listeners
     *
     * @param s string describing
     */
    public void error(String s) {
        if (writeOnConsole) {
            write("[!] " + s, System.err);
        }
        for (IOListener l : listeners) {
            if (l == null) {
                listeners.remove(l);
            } else {
                l.onError(s);
            }
        }

    }

    /**
     * Write an info message to all the listeners
     *
     * @param s
     */
    public void info(String s) {
        if (writeOnConsole) {
            write(s, System.out);
        }
        for (IOListener l : listeners) {
            if (l == null) {
                listeners.remove(l);
            } else {
                l.onInfo(s);
            }
        }
    }

    private void write(String s, PrintStream ps) {
        if (s.endsWith("\n")) {
            ps.print(s);
        } else {
            ps.println(s);
        }
    }

    /**
     * Get the listeners that are receiving output messages
     *
     * @return the listeners
     */
    public ArrayList<IOListener> getListeners() {
        return listeners;
    }

    /**
     * If the Out instance is also writing on Stoud and Stderr
     *
     * @return
     */
    public boolean isWritingOnConsole() {
        return writeOnConsole;
    }

    /**
     * Decide if the messages should be written to Stdout e Stderr (default is
     * yes)
     *
     * @param writeOnConsole
     */
    public void setWriteOnConsole(boolean writeOnConsole) {
        this.writeOnConsole = writeOnConsole;
    }

    /**
     * Interface that listens to output messages and handles what to do with
     * them.
     */
    public interface IOListener {

        /**
         * Handle an output info message
         *
         * @param info the string
         */
        public void onInfo(String info);

        /**
         * Handle an output error message
         *
         * @param error the string
         */
        public void onError(String error);
    }
}
