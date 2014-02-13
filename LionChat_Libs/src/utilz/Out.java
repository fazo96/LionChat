/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
