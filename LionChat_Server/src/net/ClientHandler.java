package net;

import core.Channel;
import core.Cmd;
import core.Group;
import core.Settings;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
        Server.out("Yay! " + getIP() + " si è connesso!");
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
            send("[ERRORE] Il tuo IP ( " + getIP() + " ) è già connesso!\n");
            Server.out(getIP() + " ha tentato di conenttersi con più di un client alla volta!");
            send(getIP() + " ha tentato di conenttersi con più di un client alla volta!\n", Settings.groupAdmin);
            return;
        }
        receiver = new Thread() {
            @Override
            public void run() {
                Object o = null;
                while (true) {
                    try { //25 ms di pausa
                        sleep(25);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        o = ois.readObject();
                    } catch (IOException ex) {
                        //Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                        disconnect();
                        Server.out(getScreenName(true) + " errore nella lettura. Connessione chiusa.");
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
        receiver.setName(getIP()); //rinomino il thread con l'ip del client assegnato
        receiver.start(); //Faccio partire il thread che riceve i messaggi del client
        group = Settings.groupGuest; //imposto il gruppo guest
        clients.add(this); //Aggiungo questo client alla lista dei client
        Settings.globalChannel.add(this);
        setWritingChannel(Settings.globalChannel);
        send(getIP() + " si è connesso!\n", Settings.groupAdmin);
        Settings.globalChannel.send("Qualcuno si è connesso!\n");
        Server.out("Inizializzati Input e Output streams per " + getIP() + " con successo e aggiunto alla lista client.\n");
        /*if (getIP().equals("127.0.0.1")) {
         setGroup(Settings.groupAdmin);
         send("Benvenuto localhost! Poteri admin consegnati.\n");
         }*/
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
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
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
        Channel.removeFromAll(this);
        if (name == null) {
            Server.out("Disconnetto " + getIP());
            send(Settings.language.getSentence("guyDisconnected").print(getScreenName(true)), Settings.groupAdmin);
            send("Utente si è disconnesso!\n", Settings.groupUser, Settings.groupGuest);
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
            //Server.out("Non puoi salvare un utente sloggato "+getScreenName(true)+ "!");
            return;
        }
        try {
            Filez.writeFile("./utenti/"
                    + name
                    + ".dat", name
                    + "\n" + getPassword()
                    + "\n" + group.getName()
                    + "\n" + getIP()); //scrivo su file
        } catch (Exception ex) {
            Server.out("Errore nel salvare il file per l'utente " + getScreenName(true));
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
        String list = "Utenti connessi: " + clients.size();
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
        if (oos == null) {
            Server.out("[DEBUG] Tentativo di tenere viva una connessine nulla\n");
            return;
        }
        try {
            oos.writeObject(new SyncObject());
        } catch (IOException ex) {
            //Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            Server.out("Impossibile mantenere la connessione con " + getScreenName(true) + ". Disconnessione");
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
        if (lname == null || pass == null) {
            send("[BUG DETECT] Errore nel login\n");
            return false;
        }
        ArrayList<String> ff = Utils.toList(Filez.getFileContent("./utenti/" + lname + ".dat"), "\n");
        if (ff == null || ff.size() < 3) { //utente non esiste, creo
            setName(lname);
            receiver.setName(getScreenName(true)); //Rinomino thread con nome e IP dell'utente
            setPassword(pass);
            setGroup(Settings.groupUser);
            save();
            ClientHandler.send("Registrato nuovo utente " + getName() + " con password " + getPassword() + "\n", Settings.groupAdmin);
            Server.out("Registrato nuovo utente " + getName() + " con password " + getPassword() + "\n");
            send("Registrato come nuovo utente: " + getName() + "\n");
            save();
            return true;
        } else if (get(lname) != null) { //utente già loggato
            send("Qualcuno è già loggato con quell'account! Prova con un'altro nome.\nSe pensi che ti sia stato rubato l'account contatta gli amministratori.\n");
            Server.out(getIP() + " ha tentato di loggarsi con l'account di " + get(lname).getScreenName(true));
            send(getIP() + " ha tentato di loggarsi con l'account di " + get(lname).getScreenName(true), Settings.groupAdmin);
            return false;
        } else if (pass.equals(ff.get(1))) { //password corretta
            setName(lname);
            receiver.setName(getScreenName(true)); //Rinomino thread con nome e IP dell'utente
            setPassword(pass);
            send("Password corretta! Connesso come " + getName() + "\n");
            Group ggg = Group.get(ff.get(2));
            if (ggg == null) {
                setGroup(Settings.groupUser);
            } else {
                setGroup(ggg);
            }
            ClientHandler.send(getScreenName(true) + " si è loggato!\n", Settings.groupAdmin);
            ClientHandler.send(getScreenName(false) + " si è loggato!\n", Settings.groupGuest, Settings.groupUser);
            save();
            return true;
        } else {
            send("Password errata!\n");
            return false;
        }
    }

    /**
     * Logouts the user.
     */
    public void logout() {
        Channel.removeFromAll(this);
        if (group == Settings.groupGuest) {
            return;
        }
        password = null;
        group = Settings.groupGuest;
        send(getScreenName(false) + " ha eseguito il logout\n", Settings.groupGuest, Settings.groupUser);
        send(getScreenName(true) + " ha eseguito il logout\n", Settings.groupAdmin);
        name = null;
        receiver.setName(getIP()); //Rinomino il thread con l'IP
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
        Server.out("Disconnetto tutti...");
        for (ClientHandler c : clients) {
            c.disconnect();
        }
    }

    /**
     * Calls save() on everybody.
     */
    public static void saveAll() {
        Server.out("Salvo tutti gli utenti...");
        for (ClientHandler c : clients) {
            c.save();
        }
    }

    /**
     * Ritorna i canali a cui l'utente appartiene.
     *
     * @return lista dinamica di canali in cui l'utente è presente.
     */
    public ArrayList<Channel> getJoinedChannels() {
        return joined;
    }

    /**
     * Ritorna l'indirizzo IP dell'utente.
     *
     * @return l'indirizzo IP dell'utente in formato stringa.
     */
    public final String getIP() {
        return s.getInetAddress().getHostAddress();
    }

    /**
     * Ritorna la password con cui l'utente è autenticato.
     *
     * @return password con cui l'utente è autenticato, null altrimenti.
     */
    public String getPassword() {
        Server.out("[DEBUG]Password per " + getScreenName(true) + " e' " + password);
        return password;
    }

    /**
     * Cambia password all'utente.
     *
     * @param password la nuova password di questo account.
     */
    public void setPassword(String password) {
        Server.out("[DEBUG] Password per " + getScreenName(true) + " è cambiata da " + this.password + " a " + password);
        this.password = password;
    }

    /**
     * Ritorna l'oggetto socket usato per comunicare con l'utente.
     *
     * @return il socket.
     */
    public Socket getSocket() {
        return s;
    }

    /**
     * Ritorna il canale su cui l'utente sta scrivendo.
     *
     * @return l'istanza del canale.
     */
    public Channel getWritingChannel() {
        return writingChannel;
    }

    /**
     * Cambia il canale su cui l'utente scrive.
     *
     * @param writingChannel nuovo valore.
     */
    public void setWritingChannel(Channel writingChannel) {
        this.writingChannel = writingChannel;
    }

    /**
     * Se l'utente è autenticato.
     *
     * @return true se l'utente è autenticato.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Ritorna il nome dell'utente.
     *
     * @return 'Utente' se l'utente non è autenticato, il suo nickname
     * altrimenti.
     */
    public String getName() {
        return name;
    }

    /**
     * Cambia nickname all'utente.
     *
     * @param name il nuovo nome da dare a questo utente.
     */
    public void setName(String name) {
        this.name = name;
        receiver.setName(getScreenName(true));
    }
}
