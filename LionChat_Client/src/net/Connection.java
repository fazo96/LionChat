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

import UI.Client;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import security.Security;
import utilz.SyncObject;

/**
 * This class handles network connection to the server.
 *
 * @author fazo
 */
public class Connection {

    private boolean connected = false;
    // socket used to send and receive data
    private Socket socket;
    // thread used to receive data
    private Thread receiver;
    // object used to send istances to server
    private ObjectOutputStream oos;
    // object used to receive istances to server
    private ObjectInputStream ois;
    // The client generated key pair for encryption
    private KeyPair rsaKeyPair;
    // The key pair generator
    private KeyPairGenerator rsaKeyGen;
    // The server's public key
    private PublicKey rsaServerKey;
    // The AES key
    private Key aesKey;
    // The AES key pair generator
    private KeyGenerator aesKeyGen;
    // The encrypter objects
    private Cipher rsaEncrypter;
    private Cipher aesEncrypter;

    public Connection(final String ip, final int port) {
        connect(ip, port);
    }

    /**
     * Tries to connect to the server.
     *
     * @param ip the IP address of the server. Hostnames will be resolved using
     * DNS
     * @param port network port used.
     */
    public void connect(final String ip, final int port) {
        if (connected) {
            disconnect(); // Must disconnect before reconnecting
        }
        rsaKeyPair = null;
        rsaServerKey = null;
        receiver = new Thread() {
            @Override
            public void run() {
                try {
                    ois = new ObjectInputStream(socket.getInputStream());
                } catch (IOException ex) {
                    Client.get().out().info("Connection failed\n");
                    connected = false;
                    return;
                }
                Object o = null;
                // Infinite loop of receiving data!
                while (true) {
                    try {
                        sleep(20); // Trying not to waste all the cpu
                    } catch (InterruptedException ex) {
                        // Do nothing
                    }
                    try {
                        o = ois.readObject();
                    } catch (IOException ex) {
                        // If this happens connection is probably dead
                        Client.get().out().info("[ERROR] " + ex + "\nCan't read from server. Disconnection imminent\n" + Client.get().getLanguage().getSentence("pressEnterToReconnect").print());
                        connected = false;
                        break;
                    } catch (ClassNotFoundException ex) {
                        // An object from unknown class has been received, that's weird!
                        Client.get().out().info("[ERROR] ClassNotFoundException.\nThis really shouldn't happen! Contact the developer\n");
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
                        rsaServerKey = (PublicKey) o;
                        if (rsaEncrypter == null) {
                            try {
                                rsaEncrypter = Cipher.getInstance("RSA");
                            } catch (Exception ex) {
                                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                                System.exit(-1);
                            }
                        }
                        try {
                            rsaEncrypter.init(Cipher.ENCRYPT_MODE, rsaServerKey);
                        } catch (InvalidKeyException ex) {
                            // Something wrong with the key, send a request
                            rsaServerKey = null;
                            Client.get().getConnection().send(Security.publicKeyRequest);
                        }
                    } else if (o instanceof SealedObject) {
                        // We just got something encrypted
                        if (rsaServerKey == null) {
                            Client.get().out().info("[!] Can't decrypt message from server: no key\n");
                            send(Security.publicKeyRequest); // Send request for key
                            continue;
                        }
                        Object oo = null;
                        try {
                            oo = ((SealedObject) o).getObject(rsaKeyPair.getPrivate());
                        } catch (IOException ex) {
                            continue;
                        } catch (ClassNotFoundException ex) {
                            Client.get().out().error("[FATAL][ENCRYPTED] ClassNotFoundException.\nThis really shouldn't happen! Contact the developer\n");
                            continue;
                        } catch (NoSuchAlgorithmException ex) {
                            Client.get().out().error("[FATAL][ENCRYPTED] IMPOSSIBLE ERROR: NO SUCH ALGORTHM\n");
                            continue;
                        } catch (InvalidKeyException ex) {
                            Client.get().out().error("[FATAL][ENCRYPTED] Can't decrypt message: invalid key\n");
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
                Client.get().out().info("Disconnected!\n");
            }
        };
        initAES();
        Client.get().out().info(Client.get().getLanguage().getSentence("tryConnect").print(ip + " " + port));
        try {
            socket = new Socket(ip, port);
            connected = true;
        } catch (UnknownHostException ex) {
            // IP doesn't exist
            Client.get().out().error("[NETWORK FAIL] No machine is turned on at the given address (UnknownHostException)\n");
            connected = false;
        } catch (IOException ex) {
            // Port closed or connection refused
            Client.get().out().error("[NETWORK FAIL] \n" + ex + "\n\n");
            connected = false;
        }
        if (!connected) {
            Client.get().out().error(Client.get().getLanguage().getSentence("pressEnterToReconnect").print());
            return;
        }
        // Initialize istance streams
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            Client.get().out().error("Connection failed!\n");
            connected = false;
            return;
        }
        // Looks like we're on
        Client.get().out().info("Connected!\n");
        receiver.start(); // I freaked out for 20 mins because I forgot this...
    }

    public void initRSA() {
        // RSA KEY PAIR GENERATION
        Client.get().out().info("Generating Key Pair...\n");
        if (rsaKeyGen == null) {
            try {
                rsaKeyGen = KeyPairGenerator.getInstance("RSA");
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            }
        }
        rsaKeyGen.initialize(2048);
        rsaKeyPair = rsaKeyGen.genKeyPair();
        // Init RSA encrypter
        try {
            rsaEncrypter = Cipher.getInstance("RSA");
        } catch (Exception ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        Client.get().out().info("Key Pair generated!\n");
    }

    public void initAES() {
        Client.get().out().info("Generating AES key...\n");
        if (aesKeyGen == null) {
            try {
                aesKeyGen = KeyGenerator.getInstance("AES");
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            }
        }
        aesKeyGen.init(256);
        aesKey = aesKeyGen.generateKey();
        if (aesEncrypter == null) {
            try {
                aesEncrypter = Cipher.getInstance("AES");
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            } catch (NoSuchPaddingException ex) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            }
        }
        try {
            aesEncrypter.init(Cipher.ENCRYPT_MODE, aesKey);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
    }

    public boolean tryAES() {
        Client.get().out().info("Trying cipher...\n");
        try {
            aesEncrypter.init(Cipher.ENCRYPT_MODE, aesKey);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        SealedObject so = null;
        String s = null, ss = null;
        try {
            so = new SealedObject(s = "If this message if displayed, then encryption works correctly\n", aesEncrypter);
        } catch (Exception ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        try {
            ss = (String) so.getObject(aesKey);
        } catch (Exception ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        if (ss != null && ss.equals(s)) {
            return true;
        }
        return false;
    }

    /**
     * Closes the connection and stops everything
     */
    public void disconnect() {
        receiver.stop();
        receiver = null;
        rsaKeyPair = null;
        rsaServerKey = null;
        connected = false;
    }

    public void sendRSAPublicKey() {
        try {
            oos.writeObject(rsaKeyPair.getPublic()); // SEND PUBLIC KEY
        } catch (IOException ex) {
            Client.get().out().info("Could not send PUBLIC KEY to server!\nConnection declared dead.\n");
            disconnect();
        }
    }

    public void sendAESkey() {
        SealedObject sealedKey;
        if (rsaServerKey == null) {
            // We need that key, if we don't have it we need to request it
            sendUnencrypted(Security.publicKeyRequest);
            return;
        }
        try {
            sealedKey = new SealedObject(aesKey, rsaEncrypter);
        } catch (Exception ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        try {
            oos.writeObject(sealedKey);
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
    }

    /**
     * Sends string to server, without encryption
     *
     * @param s the string to send.
     */
    public void sendUnencrypted(String s) {
        if (!connected) {
            return; // Can't send if not connected :(
        }
        try {
            oos.writeObject(Interpreter.fixToSend(s));
        } catch (IOException ex) {
            // Could not send, connection pipe is broken
            Client.get().out().error("Could not send UNENCRYPTED DATA to server!\nConnection declared dead.\n");
            disconnect();
        }
        Client.get().out().info("[WARNING] This message has been sent as unencrypted: " + s);
    }

    /**
     * Sends string to server (using encryption).
     *
     * @param s the string to send.
     */
    public void send(String s) {
        if (!connected) {
            return; // Can't send if not connected :(
        }
        if (aesKey == null) {
            sendUnencrypted(s); // No choice, we didn't get the server key yet
            Client.get().out().error("[ERROR] NO AES KEY! Initializing AES...\n");
            initAES();
            return;
        }
        SealedObject o = null;
        try {
            o = new SealedObject(Interpreter.fixToSend(s), aesEncrypter);
        } catch (Exception ex) {
            Client.get().out().error("[ERROR][FATAL] Could not ENCRYPT MESSAGE!\n");
            return;
        }
        try {
            oos.writeObject(o);
        } catch (IOException ex) {
            // Could not send, connection is probably dead
            Client.get().out().error("[ERROR] Could not send ENCRYPTED DATA to server!\nConnection declared dead.\n");
            disconnect();
        }
    }

    /**
     * Checks if the connection is ok
     *
     * @return true if presumed connected.
     */
    public boolean isConnected() {
        return connected;
    }
}
