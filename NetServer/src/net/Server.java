package net;

import core.Cmd;
import core.Settings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Thread.sleep;
import java.net.ServerSocket;
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
    private static Long startTime=null; //contiene in nanosecondi la data e l'ora in cui è partito il server

    public static void main(String args[]) {
        System.out.println("Tento di caricare configurazione precendente.");
        Settings.init();
        System.out.println("NetServer avviato sulla porta " + Settings.getPort());
        keepAlive = new Thread() {
            public void run() {
                while (true) {
                    ClientHandler.keepAliveAll();
                    try {
                        sleep(300);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }; keepAlive.start(); //disabilitato per inutilità
        listener = new Thread() {
            @Override
            public void run() { //Funzione che viene eseguita nel thread separato.
                ServerSocket ss = null;
                startTime=System.nanoTime(); //dichiaro server avviato e salvo l'ora in una variabile
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

    public static Long getStartTime() {
        return startTime;
    }
 public static void save(){
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
    public static void out(String s){
        System.out.println(s);
        
    }
}
