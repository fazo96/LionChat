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

import java.util.ArrayList;

/**
 * This class handles output messages
 *
 * @author fazo
 */
public class Out {
    private ArrayList<IOListener> listeners;

    /**
     * Create a new Output Handler
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
        for (IOListener l : listeners) {
            if (l == null) {
                listeners.remove(l);
            } else {
                l.onInfo(s);
            }
        }
    }

    /**
     * Write a log message of the given verbosity level
     *
     * @param s the message
     * @param level the level of verbosity
     */
    public void log(String s, int level) {
        for (IOListener l : listeners) {
            if (l == null) {
                listeners.remove(l);
            } else {
                l.onLog(s, level);
            }
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

        /**
         * Handle a onLog / debug message of the given verbosity level
         *
         * @param log the string
         * @param level the level of verbosity
         */
        public void onLog(String log, int level);
    }
}
