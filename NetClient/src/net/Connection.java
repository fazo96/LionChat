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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author fazo
 */
public class Connection {

    private static boolean connected = false;
    private static Socket s;
    private static Thread receiver;
    private static ObjectOutputStream oos;
    private static ObjectInputStream ois;

    public static void connect(final String ip, final int port) {
        receiver = new Thread() {
            @Override
            public void run() {
                GUI.get().append("Tentativo di comunicazione...\n");
                try {
                    s = new Socket(ip, port);
                    connected = true;
                } catch (UnknownHostException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                    GUI.get().append("[ERRORE] Server non esiste a quell'indirizzo (UnknownHostException)\n");
                    connected = false;
                } catch (IOException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                    GUI.get().append("[ERRORE] \n" + ex + "\n\n");
                    connected = false;
                }
                if (!connected) {
                    return;
                }
                try {
                    s.setSoTimeout(10000); //10s di timeout
                } catch (SocketException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }
                GUI.get().append("Connesso!\n");
                try {
                    oos = new ObjectOutputStream(s.getOutputStream());
                } catch (IOException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    ois = new ObjectInputStream(s.getInputStream());
                } catch (IOException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }
                Object o = null;
                while (true) {
                    //GUI.get().append("[DEBUG] Started receive loop\n");
                    try {
                        sleep(20);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {//receive loop
                        o = ois.readObject();
                    } catch (IOException ex) {
                        GUI.get().append("[ERROR] "+ex+"\nImpossibile leggere dal server. Disconnessione\nPremi invio per tentare la riconnessione\n");
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                        connected=false;
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex); break;
                    } catch (ClassNotFoundException ex) {
                        GUI.get().append("[ERRORE] ClassNotFoundException.\nPer favore informa immediatamente gli amministratori.\n");
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                        continue;
                    } 
                    //GUI.get().append("[DEBUG] Received something\n");
                    if(o==null) continue;
                    if (o instanceof String) {
                        GUI.get().append((String) o);
                    }else if(o instanceof SyncObject);
                }
                GUI.get().append("Disconnesso!\n");
            }
        };
        receiver.start();
    }

    public static void disconnect() {
        receiver.stop();
        receiver = null;
        connected = false;
    }

    public static void send(String s) {
        if(!connected)return;
        try {
            oos.writeObject(s);
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            GUI.get().append("[ERROR] Tentativo di invio al server fallito!\nPotrebbe essere stata persa la connessione.\n");
        }
    }

    public static boolean isConnected() {
        return connected;
    }
}
