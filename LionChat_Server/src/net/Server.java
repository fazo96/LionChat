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

import core.Command;
import core.Settings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Thread.sleep;
import java.net.ServerSocket;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the Server main class and entry point.
 *
 * @author fazo
 */
public class Server {

    private static Thread listener; //thread that accepts client connections
    private static Thread keepAlive; //thread that keeps connections alive.
    private static Date startDate = null;
    private static boolean autoGC = false;
    private static Long offset = 0L;

    /**
     * Main (entry point)
     *
     * @param args command line arguments, useless for now.
     */
    public static void main(String args[]) {
        Thread.currentThread().setName("Main Thread");
        Server.out("TimeZone: " + Locale.ITALY);
        startDate = Calendar.getInstance(Locale.ITALY).getTime();
        Server.out("loading settings...");
        Settings.init();
        Server.out("Server using port " + Settings.getPort());
        keepAlive = new Thread() {
            public void run() {
                offset = System.currentTimeMillis() / 1000;
                while (true) {
                    ClientHandler.keepAliveAll();
                    try {
                        sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (((System.currentTimeMillis() / 1000) - offset) > 500) {
                        if (autoGC) {
                            out("Calling out gc...");
                            System.gc();
                            out("Done");
                        } else {
                            out("AutoGC is disabled: skipping\n");
                        }
                        offset = System.currentTimeMillis() / 1000;
                    }
                }
            }
        };
        keepAlive.setName("KeepAlive Thread");
        //keepAlive.start();
        listener = new Thread() {
            @Override
            public void run() { // this runs in the listener thread
                ServerSocket ss = null;
                try {
                    System.out.println("Awating a new connection...");
                    ss = new ServerSocket(Settings.getPort());
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                while (true) {
                    try {
                        new ClientHandler(ss.accept()); // Accetto la connessione e creo il client
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        listener.setName("Listener Thread");
        listener.start();
        BufferedReader t = new BufferedReader(new InputStreamReader(System.in));
        String cmd;
        boolean b;
        while (true) {
            try { // read terminal commands
                Command.execute(t.readLine(), null); // execute
            } catch (IOException ex) {
                Server.out("[!] Could not read from console");
            }
        }
    }

    /**
     *
     * @return time and date at the starting off of the server
     */
    public static Date getStartDate() {
        return startDate;
    }

    /**
     *
     * @return how much time passed since server started
     */
    public static Date getTimePassed() {
        Long a = startDate.getTime();
        Long b = Calendar.getInstance(Locale.ITALY).getTime().getTime();
        return new Date(b - a);
    }

    /**
     *
     * @return information about the server status
     */
    public static String getStatus() {
        return "Server started at: " + Server.getStartDate().toString()
                //+"\nUptime: " + d.getHours()+":"+d.getHours()+":"+d.getSeconds()//(Utils.nanoToSec(System.nanoTime()) - Server.getStartTime())
                + "\nNumber of users: " + ClientHandler.getClients().size()
                + "\nMemory used: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 100000) + "MB"
                + " / " + (Runtime.getRuntime().totalMemory() / 100000) + "MB"
                + "\nMax memory: " + (Runtime.getRuntime().maxMemory() / 100000) + "MB"
                + "\nRunning threads (Should be 2 + 1 for every user): " + Thread.activeCount()
                + "\nCPU Cores: " + Runtime.getRuntime().availableProcessors();
    }

    /**
     * Save everything on file.
     */
    public static void save() {
        System.out.println("Saving everything...");
        ClientHandler.saveAll();
        Settings.save();
        System.out.println("Done");
    }

    /**
     * Stops the server but saves first.
     */
    public static void stop() {
        save();
        System.out.println("Shutting down...");
        Runtime.getRuntime().exit(0);
    }

    /**
     * Prints on the console (I suggest you use this, not System.out.print)
     *
     * @param s the string to print
     */
    public static void out(String s) {
        if (s.endsWith("\n")) {
            System.out.print(s);
        } else {
            System.out.println(s);
        }
    }
}
