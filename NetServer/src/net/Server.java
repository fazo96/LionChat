/*
 LionChat Server/Client desktop chat application
 Copyright (C) 2013  Enrico Fasoli

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
 */

package net;

import core.Cmd;
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author fazo
 */
public class Server {

    private static Thread listener; //thread che accetta le connessioni dei client
    private static Thread keepAlive; //thread che invia dati a tutti e fa svariati controlli.
    private static Date startDate = null;
    private static Long offset = 0L;

    public static void main(String args[]) {
        Thread.currentThread().setName("Main Thread");
        Server.out("TimeZone: " + Locale.ITALY);
        startDate = Calendar.getInstance(Locale.ITALY).getTime();
        Server.out("Tento di caricare configurazione precendente.");
        Settings.init();
        Server.out("NetServer avviato sulla porta " + Settings.getPort());
        keepAlive = new Thread() {
            public void run() {
                offset=System.currentTimeMillis()/1000;
                while (true) {
                    ClientHandler.keepAliveAll();
                    try {
                        sleep(300);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if((System.currentTimeMillis()/1000)-(offset)>500){
                        out("Chiamata automatica garbage collector...");
                        System.gc();
                        out("Chiamata eseguita.");
                        offset=System.currentTimeMillis()/1000;
                    }
                }
            }
        }; keepAlive.setName("KeepAlive Thread");
        keepAlive.start(); 
        listener = new Thread() {
            @Override
            public void run() { //Funzione che viene eseguita nel thread separato.
                ServerSocket ss = null;
                try {
                    System.out.println("Aspetto connessione...");
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
        listener.start(); //Faccio partire il listener 
        BufferedReader t = new BufferedReader(new InputStreamReader(System.in));
        String cmd;
        boolean b;
        while (true) {
            try { //leggo all'infinito i comandi scritti dal terminale.
                Cmd.cmd(t.readLine(), null); //li eseguo come comandi server.
            } catch (IOException ex) { //errore di lettura dal terminale
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static Date getStartDate() {
        return startDate;
    }

    public static Date getTimePassed() {
        Long a = startDate.getTime();
        Long b = Calendar.getInstance(Locale.ITALY).getTime().getTime();
        return new Date(b - a);
    }

    public static String getStatus() {
        return "Server avviato a: " + Server.getStartDate().toString()
                //+"\nUptime: " + d.getHours()+":"+d.getHours()+":"+d.getSeconds()//(Utils.nanoToSec(System.nanoTime()) - Server.getStartTime())
                + "\nMemoria Usata: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 100000) + "MB"
                + " / " + (Runtime.getRuntime().totalMemory() / 100000) + "MB"
                + "\nMemoria Massima: " + (Runtime.getRuntime().maxMemory() / 100000) + "MB"
                + "\nThread in esecuzione (Dovrebbero essere 3 + 1 per utente connesso): " + Thread.activeCount()
                + "\nCPU Cores disponibili: " + Runtime.getRuntime().availableProcessors();
    }

    public static void save() {
        System.out.println("Salvataggio dati in corso...");
        ClientHandler.saveAll();
        Settings.save();
        System.out.println("Dati salvati.");
    }

    public static void stop() {
        save();
        System.out.println("Shutting down...");
        Runtime.getRuntime().exit(0);
    }

    public static void out(String s) {
        if (s.endsWith("\n")) {
            System.out.print(s);
        } else {
            System.out.println(s);
        }
    }
}
