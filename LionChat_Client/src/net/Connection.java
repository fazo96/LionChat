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
 * This class handles network connection to the server.
 *
 * @author fazo
 */
public class Connection {

    private static boolean connected = false; // wether or not the client is connected
    // socket used to send and receive data
    private static Socket socket;
    // thread used to receive data
    private static Thread receiver;
    // object used to send istances to server
    private static ObjectOutputStream oos;
    // object used to receive istances to server
    private static ObjectInputStream ois;

    /**
     * Tries to connect to the server.
     *
     * @param ip the IP address of the server. Hostnames will be resolved using DNS
     * @param port network port used.
     */
    public static void connect(final String ip, final int port) {
        receiver = new Thread() {
            @Override
            public void run() {
                GUI.get().append(GUI.getLanguage().getSentence("tryConnect").print(ip+" "+port));
                try {
                    socket = new Socket(ip, port);
                    connected = true;
                } catch (UnknownHostException ex) {
                    // IP doesn't exist
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                    GUI.get().append("[ERROR] No machine is turned on at the given address (UnknownHostException)\n");
                    connected = false;
                } catch (IOException ex) {
                    // Port closed or connection refused
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                    GUI.get().append("[ERROR] \n" + ex + "\n\n");
                    connected = false;
                }
                if (!connected) {
                    GUI.get().append(GUI.getLanguage().getSentence("pressEnterToReconnect").print());
                    return;
                }
                try {
                    // Set the timeout to 20 seconds. 20 seconds of silence = connection lost
                    socket.setSoTimeout(20000);
                } catch (SocketException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                }
                // Initialize istance streams
                try {
                    oos = new ObjectOutputStream(socket.getOutputStream());
                } catch (IOException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                    GUI.get().append("Connection failed!\n");
                    connected = false;
                    return;
                }
                try {
                    ois = new ObjectInputStream(socket.getInputStream());
                } catch (IOException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                    GUI.get().append("Connection failed\n");
                    connected = false;
                    return;
                }
                // Looks like we're on
                GUI.get().append("Connected!\n");
                Object o = null;
                String s = "";
                // Infinite loop of receiving data!
                while (true) {
                    try {
                        sleep(20); // Trying not to waste all the cpu
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        o = ois.readObject();
                    } catch (IOException ex) {
                        // If this happens connection is probably dead
                        GUI.get().append("[ERRORE] " + ex + "\nCan't read from server. Disconnection imminent\n"+GUI.getLanguage().getSentence("pressEnterToReconnect").print());
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                        connected = false;
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                        break;
                    } catch (ClassNotFoundException ex) {
                        // An object from unknown class has been received, that's weird!
                        GUI.get().append("[ERRORE] ClassNotFoundException.\nThis really shouldn't happen! Contact the developer\n");
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                        continue;
                    }
                    if (o == null) {
                        continue; // We got a null something! There's probably nothing to read
                    }
                    if (o instanceof String) {
                        // We got a string! What a surprise for a chat program
                        // Run it trough the interpreter, which will know what to do with it
                        Interpreter.cmd((String)o);
                    } else if (o instanceof SyncObject);
                    // We got a SyncObject! Means the connection is alive.
                }
                // Well, looks like we're not receiving anymore.
                GUI.get().append("Disconnected!\n");
            }
        };
        receiver.start(); // I freaked out for 20 mins because I forgot this...
    }

    /**
     * Closes the connection and stops everything
     */
    public static void disconnect() {
        //fermo il thread
        receiver.stop();
        receiver = null;
        connected = false;
    }

    /**
     * Sends string to server.
     *
     * @param s the string to send.
     */
    public static void send(String s) {
        if (!connected) {
            return; // Can't send if not connected :(
        }
        try {
            //Tento la scrittura di una stringa via socket
            oos.writeObject(Interpreter.fixToSend(s));
        } catch (IOException ex) { //Invio fallito, connessione probabilmente morta
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            GUI.get().append("[ERROR] Could not send data to server!\nConnection declared dead.\n");
            disconnect();
        }
    }

    /**
     * Checks if the connection is ok
     *
     * @return true if presumed connected.
     */
    public static boolean isConnected() {
        return connected;
    }
}
