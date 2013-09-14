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

import interf.GUI;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilz.SyncObject;

/**
 *
 * @author fazo
 */
public class Connection {

    private static boolean connected = false; //indica se il client è connesso
    //è l'oggetto che permette lo scambio dati via rete
    private static Socket socket; 
    //thread che riceve e processa i dati dal server
    private static Thread receiver; 
    //oggetto che permette l'invio di istanze al server
    private static ObjectOutputStream oos; 
    //oggetto che permette la ricezione di istanze dal server
    private static ObjectInputStream ois;

    //tenta la connessione e inizializza tutto il necessario
    public static void connect(final String ip, final int port) {
        receiver = new Thread() {
            @Override
            public void run() {
                GUI.get().append("Tentativo di comunicazione...\n");
                try {
                    socket = new Socket(ip, port);
                    connected = true;
                } catch (UnknownHostException ex) {
                    //E' l'eccezione che viene tirata se la macchina a quell'IP è
                    //spenta o non esiste
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                    GUI.get().append("[ERRORE] Server non esiste a quell'indirizzo (UnknownHostException)\n");
                    connected = false;
                } catch (IOException ex) {
                    //Un errore di porta, oppure connection refused. Di solito accada
                    //quando la macchina è accesa ma il programma server non è in
                    //esecuzione
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                    GUI.get().append("[ERRORE] \n" + ex + "\n\n");
                    connected = false;
                }
                if (!connected) {
                    //Se il tentativo di connessione è fallito, è inutile fare
                    //tutto il resto
                    return;
                }
                try {
                    //Questa istruzione imposta che se non si riceve niente per 10 sec,
                    //la connessione è data per persa
                    socket.setSoTimeout(10000); //10s di timeout
                } catch (SocketException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    oos = new ObjectOutputStream(socket.getOutputStream());
                } catch (IOException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                    GUI.get().append("Connessione fallita!\n");
                    connected=false;
                    return;
                }
                try {
                    ois = new ObjectInputStream(socket.getInputStream());
                } catch (IOException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                    GUI.get().append("Connessione fallita!\n");
                    connected=false;
                    return;
                }
                GUI.get().append("Connesso!\n");
                Object o = null; String s="";
                while (true) {
                    //GUI.get().append("[DEBUG] Started receive loop\n");
                    try {
                        sleep(20); //20 secondi di pausa per non usare 100% cpu
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {//receive loop
                        o = ois.readObject();
                    } catch (IOException ex) {
                        //Errore di lettura dal socket. Connessione morta probabilmente
                        GUI.get().append("[ERRORE] "+ex+"\nImpossibile leggere dal server. Disconnessione\nPremi invio per tentare la riconnessione\n");
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                        connected=false;
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex); break;
                    } catch (ClassNotFoundException ex) {
                        //E' stato letto un oggetto di classe sconosciuta.
                        GUI.get().append("[ERRORE] ClassNotFoundException.\nPer favore informa immediatamente gli amministratori.\n");
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                        continue;
                    } 
                    //GUI.get().append("[DEBUG] Received something\n");
                    if(o==null) continue; //oggetto nullo: salto
                    if (o instanceof String) { 
                        s=(String)o; //se la stringa inizia con lo / rimuovo la prima parola
                        if(s.startsWith("/")){ 
                            String a[]=s.split(" ", 2);
                            if(a.length!=2&&a.length!=1){ System.out.println("Errore nel processare stringa con comando"); continue; } 
                            Interpreter.cmd(a[0]); //eseguo comando
                            if(a.length==2)GUI.get().append(a[1]);
                        } 
                        //l'oggetto è una stringa, la stampo nella GUI
                        GUI.get().append(s);
                    }else if(o instanceof SyncObject); //se non metto questa istruzione, il programma darà ClassNotFoundException.
                }
                //Il ciclo infinito si è rotto. Si è verificata disconnessione
                GUI.get().append("Disconnesso!\n"); 
            }
        };
        receiver.start();
    }

    public static void disconnect() {
        //fermo il thread
        receiver.stop();
        receiver = null;
        connected = false;
    }

    public static void send(String s) {
        if(!connected)return; //se non sono connesso non faccio nulla
        try {
            //Tento la scrittura di una stringa via socket
            oos.writeObject(Interpreter.fixToSend(s));
        } catch (IOException ex) { //Invio fallito, connessione probabilmente morta
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            GUI.get().append("[ERROR] Tentativo di invio al server fallito!\nConnessione persa.\n");
            disconnect();
        }
    }

    public static boolean isConnected() {
        return connected;
    }
}
