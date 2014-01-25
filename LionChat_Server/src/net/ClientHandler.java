package net;

import core.Channel;
import core.Cmd;
import core.Group;
import core.Settings;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilz.Filez;
import utilz.SyncObject;
import utilz.Utils;

/**
 * This object represents a connected client (logged in or not)
 *
 * @author fazo
 */
public class ClientHandler {

    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<ClientHandler>());
    private ArrayList<Channel> joined = new ArrayList<Channel>();
    private Socket s;
    private String name = null, password = null;
    private Channel writingChannel;
    private ClientHandler client = this;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private Thread receiver;
    private Group group;
    private boolean connected = false;

    /**
     * Initializes a new client.
     *
     * @param s a CONNECTED socket to use for communication. connesso e
     * funzionante.
     */
    public ClientHandler(final Socket s) {
        this.s = s;
        Server.out(getIP() + " has connected!");
        connected = true;
        try {
            ois = new ObjectInputStream(s.getInputStream()); //creo un oggetto in grado di ricevere le istanze delle classi inviate dal client
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            oos = new ObjectOutputStream(s.getOutputStream()); //creo un oggetto in grado di inviare le istanze delle classi al client
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!getIP().equals("127.0.0.1") && get(getIP()) != null) { //Questo IP e' già connesso! (non vale per localhost che può connettersi piu volte)
            send(Settings.language.getSentence("ipAlreadyConnected").print(getIP()));
            Server.out(getIP() + " tried multiple connections!");
            send(getIP() + " tried multiple connections!\n", Settings.groupAdmin);
            return;
        }
        receiver = new Thread() {
            @Override
            public void run() {
                Object o = null;
                while (true) {
                    try { //25 ms pause
                        sleep(25);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        o = ois.readObject();
                    } catch (IOException ex) {
                        //Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                        disconnect();
                        Server.out(getScreenName(true) + " error reading input. Connection closed");
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    //Server.out("Ricevuto messaggio");
                    if (o != null && o instanceof String && !((String) o).equals("")) {
                        Cmd.cmd((String) o, client);
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

    /**
     * Sends a string to the client
     *
     * @param msg the string to send.
     * @return true if the message has been sent
     */
    public boolean send(String msg) {
        if (!connected) {
            return false;
        }
        try {
            //Server.out("Sending: "+msg+" to "+s.getInetAddress());
            oos.writeObject(msg);
        } catch (IOException ex) {
            //Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            Server.out(getIP() + " error while sending. Connection closed.");
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
            Server.out("Disconnecting " + getIP());
            send(Settings.language.getSentence("guyDisconnected").print(getScreenName(true)), Settings.groupAdmin);
            send(Settings.language.getSentence("somebodyDisconnected").print(), Settings.groupUser, Settings.groupGuest);
        } else {
            Server.out("Disconnecting " + getScreenName(true));
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
                //Controllo se il nome combacia alla stringa data
                if (ch.getName() != null && ch.getName().equals(name)) {
                    return ch;
                }
                //Controllo se l'IP combacia alla stringa data
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
        /*if (g == null) {
         Server.out("[SEND][ALL] " + msg);
         } else Server.out("[SEND][" + g.getName() + "] " + msg);*/
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
        //Server.out("[SEND][" + g.getName() + "]["+g2.getName()+"] " + msg);
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
        //Server.out("[SEND][" + g.getName() + "]["+g2.getName()+"]["+g3.getName()+"] " + msg);
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
            Server.out("Could not save user file for " + getScreenName(true));
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
            Server.out("[DEBUG] Can't keep alive dead connection\n");
            disconnect();
            return;
        }
        try {
            oos.writeObject(new SyncObject());
        } catch (IOException ex) {
            //Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            Server.out("Could not keep " + getScreenName(true) + " alive. Disconnecting");
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
            Server.out("[BUG DETECT] Weird bug on login! This should never happen\n");
            return false;
        }
        ArrayList<String> ff = Utils.toList(Filez.getFileContent("./users/" + lname + ".dat"), "\n");
        if (ff == null || ff.size() < 3) { // User doesn't exist, creating him
            setName(lname);
            receiver.setName(getScreenName(true)); // Rename thread
            setPassword(pass);
            setGroup(Settings.groupUser);
            save();
            ClientHandler.send("New user registered: " + getName() /*+ " with password " + getPassword()*/+ "\n", Settings.groupAdmin);
            Server.out("New user registered: " + getName() + " with password " + getPassword() + "\n");
            send(Settings.language.getSentence("registeredAs").print(getName()));
            return true;
        } else if (get(lname) != null) { // User is already logged in
            send(Settings.language.getSentence("alreadyLoggedIn").print());
            Server.out(getIP() + " tried logging in as " + get(lname).getScreenName(true));
            send(getIP() + " tried logging in as " + get(lname).getScreenName(true), Settings.groupAdmin);
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
        Server.out("Disconnecting everyone...");
        for (ClientHandler c : clients) {
            c.disconnect();
        }
    }

    /**
     * Calls save() on everybody.
     */
    public static void saveAll() {
        Server.out("Saving everyone...");
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
        return s.getInetAddress().getHostAddress();
    }

    /**
     * Returns the user's password
     *
     * @return the user's password
     */
    public String getPassword() {
        Server.out("[DEBUG] Password for " + getScreenName(true) + " is " + password);
        return password;
    }

    /**
     * Sets a new password for the user
     *
     * @param password the new password for the user
     */
    public void setPassword(String password) {
        Server.out("[DEBUG] Password for " + getScreenName(true) + " went from " + this.password + " to " + password);
        this.password = password;
    }

    /**
     * Returns the socket that connects the user to the server
     *
     * @return the socket
     */
    public Socket getSocket() {
        return s;
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
