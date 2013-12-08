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

/**
 * Questa classe è l'entry point del programma server.
 *
 * @author fazo
 */
public class Server {

    private static Thread listener; //thread che accetta le connessioni dei client
    private static Thread keepAlive; //thread che invia dati a tutti e fa svariati controlli.
    private static Date startDate = null;
    private static boolean autoGC = false;
    private static Long offset = 0L;

    /**
     * Metodo main (entry point), inzializza la maggior parte dei thread e delle
     * variabili.
     *
     * @param args argomenti dalla riga di comando. Il programma li ignora
     * completamente.
     */
    public static void main(String args[]) {
        Thread.currentThread().setName("Main Thread");
        Server.out("TimeZone: " + Locale.ITALY);
        startDate = Calendar.getInstance(Locale.ITALY).getTime();
        Server.out("Tento di caricare configurazione precendente.");
        Settings.init();
        Server.out("NetServer avviato sulla porta " + Settings.getPort());
        keepAlive = new Thread() {
            public void run() {
                offset = System.currentTimeMillis() / 1000;
                while (true) {
                    ClientHandler.keepAliveAll();
                    try {
                        sleep(300);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (((System.currentTimeMillis() / 1000) - offset) > 500) {
                        if (autoGC) {
                            out("Chiamata automatica garbage collector...");
                            System.gc();
                            out("Chiamata eseguita.");
                        } else {
                            out("AutoGC disattivato: skipping\n");
                        }
                        offset = System.currentTimeMillis() / 1000;
                    }
                }
            }
        };
        keepAlive.setName("KeepAlive Thread");
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

    /**
     *
     * @return la data e l'ora in cui il server è stato avviato.
     */
    public static Date getStartDate() {
        return startDate;
    }

    /**
     *
     * @return il tempo passato da quando il server è stato avviato.
     */
    public static Date getTimePassed() {
        Long a = startDate.getTime();
        Long b = Calendar.getInstance(Locale.ITALY).getTime().getTime();
        return new Date(b - a);
    }

    /**
     *
     * @return informazioni varie sul server e il suo stato, sottoforma di
     * stringa multi-riga.
     */
    public static String getStatus() {
        return "Server avviato a: " + Server.getStartDate().toString()
                //+"\nUptime: " + d.getHours()+":"+d.getHours()+":"+d.getSeconds()//(Utils.nanoToSec(System.nanoTime()) - Server.getStartTime())
                + "\nMemoria Usata: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 100000) + "MB"
                + " / " + (Runtime.getRuntime().totalMemory() / 100000) + "MB"
                + "\nMemoria Massima: " + (Runtime.getRuntime().maxMemory() / 100000) + "MB"
                + "\nThread in esecuzione (Dovrebbero essere 3 + 1 per utente connesso): " + Thread.activeCount()
                + "\nCPU Cores disponibili: " + Runtime.getRuntime().availableProcessors();
    }

    /**
     * Salva tutti i dati dei client e del server su file.
     */
    public static void save() {
        System.out.println("Salvataggio dati in corso...");
        ClientHandler.saveAll();
        Settings.save();
        System.out.println("Dati salvati.");
    }

    /**
     * Ferma il server in maniera sicura, salvando prima tutto.
     */
    public static void stop() {
        save();
        System.out.println("Shutting down...");
        Runtime.getRuntime().exit(0);
    }

    /**
     * Stampa su console del server la stringa data senza causare problemi di
     * lettura.
     *
     * @param s la stringa da stampare sulla console
     */
    public static void out(String s) {
        if (s.endsWith("\n")) {
            System.out.print(s);
        } else {
            System.out.println(s);
        }
    }
}
