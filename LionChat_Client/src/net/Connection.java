package net;

import interf.GUI;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SealedObject;
import utilz.SyncObject;

/**
 * This class handles network connection to the server.
 *
 * @author fazo
 */
public class Connection {

    private static boolean connected = false, useCryptography = true;
    // socket used to send and receive data
    private static Socket socket;
    // thread used to receive data
    private static Thread receiver;
    // object used to send istances to server
    private static ObjectOutputStream oos;
    // object used to receive istances to server
    private static ObjectInputStream ois;
    private static KeyPair keyPair;
    private static KeyPairGenerator keyGen;
    private static PublicKey serverKey;
    private static Cipher encrypter;

    /**
     * Tries to connect to the server.
     *
     * @param ip the IP address of the server. Hostnames will be resolved using
     * DNS
     * @param port network port used.
     */
    public static void connect(final String ip, final int port) {
        keyPair = null;
        serverKey = null;
        receiver = new Thread() {
            @Override
            public void run() {
                try {
                    ois = new ObjectInputStream(socket.getInputStream());
                } catch (IOException ex) {
                    Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                    GUI.get().append("Connection failed\n");
                    connected = false;
                    return;
                }
                Object o = null;
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
                        GUI.get().append("[ERROR] " + ex + "\nCan't read from server. Disconnection imminent\n" + GUI.getLanguage().getSentence("pressEnterToReconnect").print());
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                        connected = false;
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                        break;
                    } catch (ClassNotFoundException ex) {
                        // An object from unknown class has been received, that's weird!
                        GUI.get().append("[ERROR] ClassNotFoundException.\nThis really shouldn't happen! Contact the developer\n");
                        Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                        continue;
                    }
                    if (o == null) {
                        continue; // We got a null something! There's probably nothing to read
                    }
                    if (o instanceof String) {
                        // We got a string! What a surprise for a chat program
                        // Run it trough the interpreter, which will know what to do with it
                        Interpreter.cmd((String) o);
                    } else if (o instanceof PublicKey) {
                        System.out.println("[!][DEBUG] Received server key\n");
                        serverKey = (PublicKey) o;
                        try {
                            encrypter.init(Cipher.ENCRYPT_MODE, serverKey);
                        } catch (InvalidKeyException ex) {
                            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else if (o instanceof SealedObject) {
                        // We just got something encrypted
                        if (serverKey == null) {
                            GUI.get().append("[!] Can't decrypt message from server: no key\n");
                            send("/askKey"); // Send request for key
                            continue;
                        }
                        Object oo = null;
                        try {
                            oo = ((SealedObject) o).getObject(keyPair.getPrivate());
                        } catch (IOException ex) {
                            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                            continue;
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                            GUI.get().append("[ERROR][ENCRYPTED] ClassNotFoundException.\nThis really shouldn't happen! Contact the developer\n");
                            continue;
                        } catch (NoSuchAlgorithmException ex) {
                            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                            GUI.get().append("[ERROR][ENCRYPTED] IMPOSSIBLE ERROR: NO SUCH ALGORTHM\n");
                            continue;
                        } catch (InvalidKeyException ex) {
                            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                            GUI.get().append("[ERROR][ENCRYPTED] Can't decrypt message: invalid key\n");
                            continue;
                        }
                        if (oo == null) {
                            continue;
                        }
                        if (oo instanceof String) {
                            Interpreter.cmd((String) oo);
                        }
                    } else if (o instanceof SyncObject);
                    // We got a SyncObject! Means the connection is alive.
                }
                // Well, looks like we're not receiving anymore.
                GUI.get().append("Disconnected!\n");
            }
        };

        // KEY GENERATION
        if (keyGen == null) {
            try {
                keyGen = KeyPairGenerator.getInstance("RSA");
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            }
        }
        keyPair = keyGen.genKeyPair();
        // Init encrypter and decrypter
        try {
            encrypter = Cipher.getInstance("RSA");
        } catch (Exception ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }

        GUI.get().append(GUI.getLanguage().getSentence("tryConnect").print(ip + " " + port));
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
        // Initialize istance streams
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            GUI.get().append("Connection failed!\n");
            connected = false;
            return;
        }
        sendKey(); // Send our key as soon as possible
        // Looks like we're on
        GUI.get().append("Connected!\n");
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

    public static void sendKey() {
        try {
            oos.writeObject(keyPair.getPublic()); // SEND PUBLIC KEY
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            GUI.get().append("[ERROR] Could not send PUBLIC KEY to server!\nConnection declared dead.\n");
            disconnect();
        }
    }

    /**
     * Sends string to server, without encryption
     *
     * @param s the string to send.
     */
    public static void sendUnencrypted(String s) {
        if (!connected) {
            return; // Can't send if not connected :(
        }
        try {
            oos.writeObject(Interpreter.fixToSend(s));
        } catch (IOException ex) { //Invio fallito, connessione probabilmente morta
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            GUI.get().append("[ERROR] Could not send UNENCRYPTED DATA to server!\nConnection declared dead.\n");
            disconnect();
        }
        GUI.get().append("This message has been sent as unencrypted: "+s);
    }

    /**
     * Sends string to server (using encryption).
     *
     * @param s the string to send.
     */
    public static void send(String s) {
        if (!connected) {
            return; // Can't send if not connected :(
        }
        if (serverKey == null) {
            sendUnencrypted(s); // No choice, we didn't get the server key yet
            sendUnencrypted("/askKey"); // Ask for the key
            GUI.get().append("[ERROR] NO SERVER KEY! Asking for it...\n");
            return;
        }
        SealedObject o = null;
        try {
            o = new SealedObject(Interpreter.fixToSend(s), encrypter);
        } catch (Exception ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            GUI.get().append("[ERROR][FATAL] Could not ENCRYPT MESSAGE!\n");
            return;
        }
        try {
            oos.writeObject(o);
        } catch (IOException ex) { //Invio fallito, connessione probabilmente morta
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            GUI.get().append("[ERROR] Could not send ENCRYPTED DATA to server!\nConnection declared dead.\n");
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
