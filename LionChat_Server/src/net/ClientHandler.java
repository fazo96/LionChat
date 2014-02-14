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

import core.Channel;
import core.Command;
import core.Group;
import core.Settings;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import utilz.Filez;
import utilz.SyncObject;
import utilz.Utils;

/**
 * This object represents a connected client (logged in or not)
 *
 * @author fazo
 */
public class ClientHandler {

    // List of all the clients connected
    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<ClientHandler>());
    // List of all the channels joined by this user
    private ArrayList<Channel> joined = new ArrayList<Channel>();
    // Socket that connects to this client
    private Socket socket;
    // Name and password strings for this Client
    private String name = null, password = null;
    // Channel on which the client is writing its messages at the moment
    private Channel writingChannel;
    // Variable used inside the Receiver thread
    private ClientHandler client = this;
    // Stream that receives Objects from the client
    private ObjectInputStream ois;
    // Stream that sends Objects to the client
    private ObjectOutputStream oos;
    // Thread that handles incoming packets
    private Thread receiver;
    // The user's group
    private Group group;
    // Wether this user is connected or not
    private boolean connected = false;
    // The user's public key, sent from him, used to encrypt messages destined to him
    private PublicKey clientKey;
    // The Cipher that encrypts messages for the user
    private Cipher encrypter;

    /**
     * Initializes a new client from the given connected Socket.
     *
     * @param socket a CONNECTED socket to use for communication. connesso e
     * funzionante.
     */
    public ClientHandler(final Socket socket) {
        this.socket = socket;
        Server.out().info(getIP() + " has connected!");
        try {
            encrypter = Cipher.getInstance("RSA");
        } catch (Exception ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            Server.out().info(getIP() + " could not get Cipher instance! This should never ever happen");
        }
        connected = true;
        try {
            ois = new ObjectInputStream(socket.getInputStream()); //creo un oggetto in grado di ricevere le istanze delle classi inviate dal client
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            oos = new ObjectOutputStream(socket.getOutputStream()); //creo un oggetto in grado di inviare le istanze delle classi al client
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!getIP().equals("127.0.0.1") && get(getIP()) != null) { //Questo IP e' già connesso! (non vale per localhost che può connettersi piu volte)
            send(Settings.language.getSentence("ipAlreadyConnected").print(getIP()));
            Server.out().info(getIP() + " tried multiple connections!");
            send(getIP() + " tried multiple connections!\n", Settings.groupAdmin);
            return;
        }
        // Let's send the server key to the client, so he can send encrypted messages
        sendServerKey();
        receiver = new Thread() {
            @Override
            public void run() {
                Object o = null;
                while (true) {
                    try { //25 ms pause between each check
                        sleep(25);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        o = ois.readObject();
                    } catch (IOException ex) {
                        //Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                        disconnect();
                        Server.out().info(getScreenName(true) + " error reading input. Connection closed");
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (o instanceof PublicKey) {
                        // Client just send us his encryption key, save it
                        clientKey = (PublicKey) o;
                        try {
                            // Make sure we use it to encrypt the next messages
                            encrypter.init(Cipher.ENCRYPT_MODE, clientKey);
                        } catch (InvalidKeyException ex) {
                            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (o instanceof SealedObject) {
                        // The client sent us an encrypted Object.
                        Object oo = null;
                        try {
                            // Try to Decrypt the Object contained and store it
                            oo = ((SealedObject) o).getObject(Settings.getKeyPair().getPrivate());
                        } catch (Exception ex) {
                            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                            Server.out().info(getScreenName(true) + " error while decrypting a message. Should never happen!");
                            continue;
                        }
                        if (oo instanceof String) {
                            // Object is a string from the client
                            // Run the string into the interpreter
                            Command.execute((String) oo, client);
                        }
                    }
                    if (o != null && o instanceof String && !((String) o).equals("")) {
                        // Object is a string.
                        // Run the string into the interpreter
                        Command.execute((String) o, client);
                    }
                }
            }
        };
        receiver.setName(getIP()); //rename thread to client IP
        receiver.start(); //start receiver thread
        group = Settings.groupGuest; // set the group to guest
        clients.add(this); // add client to the list
        send(getIP() + " has connected!\n", Settings.groupAdmin); // advertise connection
        Settings.globalChannel.send(Settings.language.getSentence("somebodyConnected").print()); // advertise new connection
        Settings.globalChannel.add(this); // add client to global channel
        setWritingChannel(Settings.globalChannel); // set the client to write to global chanel
    }

    public void sendServerKey() {
        if (!connected) {
            return;
        }
        try {
            Server.out().info("Server public key has been sent to " + getScreenName(true));
            oos.writeObject(Settings.getKeyPair().getPublic());
        } catch (IOException ex) {
            //Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            Server.out().info(getIP() + " error while sending PUBLIC KEY. Connection closed.");
            disconnect();
        }
    }

    /**
     * Sends a string to the client, without using encryption
     *
     * @param msg the string to send.
     * @return true if the message has been sent
     */
    public boolean sendUnencrypted(String msg) {
        if (!connected) {
            return false;
        }
        try {
            oos.writeObject(msg);
        } catch (IOException ex) {
            Server.out().info(getIP() + " error while sending UNENCRYPTED MESSAGE. Connection closed.");
            disconnect();
            return false;
        }
        Server.out().info("This message to " + getScreenName(true) + " has just been sent unencrypted: " + msg);
        return true;
    }

    /**
     * Sends a string to the client, using encryption
     *
     * @param msg the string to send.
     * @return true if the message has been sent
     */
    public boolean send(String msg) {
        if (!connected) {
            return false;
        }
        if (clientKey == null) {
            sendUnencrypted("/askKey"); // Ask client for the key
            sendUnencrypted(msg); // No choice: must sent the message unencrypted
            return false;
        }
        SealedObject o = null;
        try {
            o = new SealedObject(msg, encrypter);
        } catch (Exception ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            Server.out().info(getScreenName(true) + " CLIENT KEY is not valid! Requesting key. Message will be sent unencrypted!");
            sendUnencrypted("/askKey"); // Ask client for the key
            sendUnencrypted(msg);
            return false;
        }
        try {
            //Server.out().info("Sending (encrypted): "+msg+" to "+getScreenName(true));
            oos.writeObject(msg);
        } catch (IOException ex) {
            Server.out().info(getScreenName(true) + " error while sending ENCRYPTED MESSAGE. Connection closed.");
            disconnect();
            return false;
        }
        return true;
    }

    /**
     * Sends a message from this client to given channel
     *
     * @param msg the message to send
     * @param c the channel on which the message will be sent
     */
    public void sendToChannel(String msg, Channel c) {
        for (ClientHandler ch : c.getClients()) {
            if (!ch.isConnected() || !ch.getGroup().can("channel")) {
                c.getClients().remove(ch);
            } else {
                ch.send(msg);
            }
        }
    }

    /**
     * Closes every connection and deletes this istance.
     */
    public void disconnect() {
        if (!connected) {
            return;
        }
        Channel.removeFromAll(this, true);
        if (name == null) {
            Server.out().info("Disconnecting " + getIP());
            send(Settings.language.getSentence("guyDisconnected").print(getScreenName(true)), Settings.groupAdmin);
            send(Settings.language.getSentence("somebodyDisconnected").print(), Settings.groupUser, Settings.groupGuest);
        } else {
            Server.out().info("Disconnecting " + getScreenName(true));
            Settings.globalChannel.send(Settings.language.getSentence("guyDisconnected").print(getScreenName(false)));
        }
        connected = false;
        clients.remove(this);
        receiver.stop();
    }

    /**
     *
     * @return the list of connected clients
     */
    public static List<ClientHandler> getClients() {
        return clients;
    }

    /**
     * Returns the client corresponding to given username or IP, or null if it
     * doesn't exist.
     *
     * @param name the name or IP of the client to search
     * @return the client corresponding to name/IP given, or null if it doesn't
     * exist
     */
    public static ClientHandler get(String name) {
        for (ClientHandler ch : clients) {
            if (ch.isConnected()) {
                // Check if the string matches any name
                if (ch.getName() != null && ch.getName().equals(name)) {
                    return ch;
                }
                // Check if the string matches any IP
                if (ch.getIP().equals(name)) {
                    return ch;
                }

            } else {
                ch.disconnect();
            }
        }
        return null;
    }

    /**
     * Sends a message from this client to a group.
     *
     * @param msg the message to send
     * @param g the group that will receive the message
     */
    public static void send(String msg, Group g) {
        for (ClientHandler cl : clients) {
            if (g == null || cl.getGroup() == g) {
                cl.send(msg);
            }
        }
    }

    /**
     * Sends a message from this client to multiple groups
     *
     * @param msg message to send
     * @param g first group that will receive the message
     * @param g2 second group that will receive the message
     */
    public static void send(String msg, Group g, Group g2) {
        send(msg, g);
        send(msg, g2);
    }

    /**
     * Sends a message from this client to multiple groups
     *
     * @param msg message to send
     * @param g first group that will receive the message
     * @param g2 second group that will receive the message
     * @param g3 third group that will receive the message
     */
    public static void send(String msg, Group g, Group g2, Group g3) {
        send(msg, g);
        send(msg, g2);
        send(msg, g3);
    }

    /**
     * Saves user info to file.
     */
    public void save() {
        if (group == Settings.groupGuest) {
            return;
        }
        try {
            Filez.writeFile("./users/"
                    + name
                    + ".dat", name
                    + "\n" + getPassword()
                    + "\n" + group.getName()
                    + "\n" + getIP());
        } catch (Exception ex) {
            Server.out().info("Could not save user file for " + getScreenName(true));
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a list of clients as string, readable by humans
     *
     * @param showIP wether the list must show ip addresses
     * @return a string with a readable client list
     */
    public static String getClientList(boolean showIP) {
        String list = "Connected users: " + clients.size();
        int i = 0, guests = 0;
        for (ClientHandler ch : clients) {
            i++;
            if (ch.getGroup() == Settings.groupGuest) {
                guests++;
                if (showIP) {
                    list += "\n" + i + " - [ " + ch.getGroup().getName() + " ] "/*(showIP?("[ "+ch.getGroup().getName())+" ] ":"")*/ + ch.getScreenName(true);
                    continue;
                }
            } else {
                list += "\n" + i + " - [ " + ch.getGroup().getName() + " ] "/*(showIP?("[ "+ch.getGroup().getName())+" ] ":"")*/ + ch.getScreenName(showIP);
            }
        }
        if (!showIP) {
            list += "\nGuests: " + guests;
        }
        return list + "\n";
    }

    /**
     * Sends hearthbeat to client, notifying him that the server is alive.
     */
    public void keepAlive() {
        if (oos == null || connected == false) {
            Server.out().info("[DEBUG] Can't keep alive dead connection\n");
            disconnect();
            return;
        }
        try {
            oos.writeObject(new SyncObject());
        } catch (IOException ex) {
            //Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            Server.out().info("Could not keep " + getScreenName(true) + " alive. Disconnecting");
            disconnect();
        }
    }

    /**
     * Tries authentication with given name and password. If the password is
     * wrong, the auth fails. If the user doesn't exist it is created.
     *
     * @param lname username
     * @param pass password
     * @return false if user is already logged in or the password is wrong. True
     * in all other cases
     */
    public boolean login(String lname, String pass) {
        send("Logging you in...\n");
        if (lname == null || pass == null) {
            Server.out().info("[BUG DETECT] Weird bug on login! This should never happen\n");
            return false;
        }
        ArrayList<String> ff = Utils.toList(Filez.getFileContent("./users/" + lname + ".dat"), "\n");
        if (ff == null || ff.size() < 3) { // User doesn't exist, creating him
            setName(lname);
            receiver.setName(getScreenName(true)); // Rename thread
            setPassword(pass);
            setGroup(Settings.groupUser);
            save();
            ClientHandler.send("New user registered: " + getName() /*+ " with password " + getPassword()*/ + "\n", Settings.groupAdmin);
            Server.out().info("New user registered: " + getName() + " with password " + getPassword() + "\n");
            send(Settings.language.getSentence("registeredAs").print(getName()));
            return true;
        } else if (get(lname) != null) { // User is already logged in
            send(Settings.language.getSentence("alreadyLoggedIn").print());
            Server.out().info(getIP() + " tried logging in as " + get(lname).getScreenName(true));
            send(getIP() + " tried logging in as " + get(lname).getScreenName(true) + "\n", Settings.groupAdmin);
            return false;
        } else if (pass.equals(ff.get(1))) { //user wasn't logged in and password is correct
            setName(lname);
            receiver.setName(getScreenName(true)); // Rename thread
            setPassword(pass);
            send("Password correct! Logged in as " + getName() + "\n");
            Group ggg = Group.get(ff.get(2));
            if (ggg == null) {
                setGroup(Settings.groupUser);
            } else {
                setGroup(ggg);
            }
            ClientHandler.send(getScreenName(true) + " has logged in!\n", Settings.groupAdmin);
            ClientHandler.send(getScreenName(false) + " has logged in!\n", Settings.groupGuest, Settings.groupUser);
            save();
            return true;
        } else {
            send("Wrong password\n");
            return false;
        }
    }

    /**
     * Logouts the user.
     */
    public void logout() {
        Channel.removeFromAll(this, false);
        if (group == Settings.groupGuest) {
            return;
        }
        password = null;
        group = Settings.groupGuest;
        send(Settings.language.getSentence("guyLoggedOut").print(getScreenName(false)), Settings.groupGuest, Settings.groupUser);
        send(Settings.language.getSentence("guyLoggedOut").print(getScreenName(true)), Settings.groupAdmin);
        name = null;
        receiver.setName(getIP()); // Rename thread with IP address
    }

    /**
     * Retuns the group that the user is part of.
     *
     * @return the group istance
     */
    public Group getGroup() {
        return group;
    }

    /**
     * Sets the user's group
     *
     * @param group the new group
     */
    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     * Returns a readable string containing the username and/or the ip address
     * of the user.
     *
     * @param showIP wether the string must contain the ip address.
     * @return string
     */
    public String getScreenName(boolean showIP) {
        if (showIP) {
            if (name != null) {
                return name + " ( " + getIP() + " )";
            } else {
                return getIP();
            }
        } else {
            if (name != null) {
                return name;
            } else {
                return "User";
            }
        }
    }

    /**
     * Sends an heartbeat to everybody.
     */
    public static void keepAliveAll() {
        for (ClientHandler ch : clients) {
            ch.keepAlive();
        }
    }

    /**
     * Calls disconnect() on everybody.
     */
    public static void disconnectAll() {
        Server.out().info("Disconnecting everyone...");
        for (ClientHandler c : clients) {
            c.disconnect();
        }
    }

    /**
     * Calls save() on everybody.
     */
    public static void saveAll() {
        Server.out().info("Saving everyone...");
        for (ClientHandler c : clients) {
            c.save();
        }
    }

    /**
     * Returns the list of channels that the user has joined
     *
     * @return the list of channels that the user has joined
     */
    public ArrayList<Channel> getJoinedChannels() {
        return joined;
    }

    /**
     * Returns the user's IP address
     *
     * @return IP address as string
     */
    public final String getIP() {
        return socket.getInetAddress().getHostAddress();
    }

    /**
     * Returns the user's password
     *
     * @return the user's password
     */
    public String getPassword() {
        Server.out().info("[DEBUG] Password for " + getScreenName(true) + " is " + password);
        return password;
    }

    /**
     * Sets a new password for the user
     *
     * @param password the new password for the user
     */
    public void setPassword(String password) {
        Server.out().info("[DEBUG] Password for " + getScreenName(true) + " went from " + this.password + " to " + password);
        this.password = password;
    }

    /**
     * Returns the socket that connects the user to the server
     *
     * @return the socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Returns the channel on which the user was writing
     *
     * @return the channel istance
     */
    public Channel getWritingChannel() {
        return writingChannel;
    }

    /**
     * Changes the channel on which the user is writing
     *
     * @param writingChannel new writing channel
     */
    public void setWritingChannel(Channel writingChannel) {
        this.writingChannel = writingChannel;
    }

    /**
     * Returns true if the user is authenticated
     *
     * @return true if the user is authenticated
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Returns the username
     *
     * @return 'User' if the user is not authenthicated, else is nickname
     */
    public String getName() {
        return name;
    }

    /**
     * Changes the user's nickname. WARNING: the results could be disrupting
     *
     * @param name the new username
     */
    public void setName(String name) {
        this.name = name;
        receiver.setName(getScreenName(true));
    }
}
