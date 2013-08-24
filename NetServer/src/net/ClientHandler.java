package net;

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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author fazo
 */
public class ClientHandler {

    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<ClientHandler>());
    private Socket s;
    private String name = null, password = null;
    private ClientHandler client = this;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private Thread receiver;
    private Group group;
    private boolean connected = false, logged = false;

    public ClientHandler(final Socket s) {
        this.s = s;
        /*try {
         this.s.setKeepAlive(true);
         } catch (SocketException ex) {
         Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
         }*/
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

        receiver = new Thread() {
            @Override
            public void run() {
                Object o = null;
                while (true) {
                    try {
                        o = ois.readObject();
                    } catch (IOException ex) {
                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                        disconnect();
                        if (logged) {
                            Server.out(getIP() + " errore nella lettura. Connessione chiusa.");
                        } else {
                            Server.out(name + " ( " + getIP() + " ) errore nella lettura. Connessione chiusa.");
                        }
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
        receiver.start(); //Faccio partire il thread che riceve i messaggi del client
        group = Settings.groupGuest; //imposto il gruppo guest
        clients.add(this); //Aggiungo questo client alla lista dei client
        send(getIP() + " si è connesso!\n", Settings.groupAdmin);
        sendToAll("Qualcuno si è connesso!\n");
        Server.out("Inizializzati Input e Output streams per " + getIP() + " con successo e aggiunto alla lista client.\n");
        if (getIP().equals("127.0.0.1")) {
            setGroup(Settings.groupAdmin);
            send("Benvenuto localhost! Poteri admin consegnati.\n");
        }
    }

    public boolean send(String msg) {
        if (!connected) {
            return false;
        }
        try {
            //Server.out("Sending: "+msg+" to "+s.getInetAddress());
            oos.writeObject(msg);
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            Server.out(s.getInetAddress() + " errore nell'invio. Connessione chiusa.");
            disconnect();
            return false;
        }
        return true;
    }

    public void disconnect() {
        if (!connected) {
            return;
        }
        if (!logged) {
            Server.out("Disconnetto " + getIP());
            sendToAll(getIP() + " si è disconnesso!\n");
        } else {
            Server.out("Disconnetto " + name + " (" + getIP() + ")");
            sendToAll(name + " si è disconnesso!\n");
        }
        connected = false;
        clients.remove(this);
        receiver.stop();
    }

    public static List<ClientHandler> getClients() {
        return clients;
    }

    public static ClientHandler get(String n) {
        for (ClientHandler ch : clients) {
            if (ch.isConnected()) {
                if (ch.isLoggedIn() && ch.getName().equals(n)) {
                    return ch;
                } else if (ch.getIP().equals(n)) {
                    return ch;
                }

            } else {
                ch.disconnect();
            }
        }
        return null;
    }

    public static void sendToAll(String msg) {
        send(msg, null);
    }

    public static void send(String msg, Group g) {
        if (g == null) {
            Server.out("[SEND][ALL] " + msg);
        } else {
            Server.out("[SEND][" + g.getName() + "] " + msg);
        }
        for (ClientHandler cl : clients) {
            if (g == null || cl.getGroup() == g) {
                cl.send(msg);
            }
        }
    }

    public static void send(String msg, Group g, Group g2) {
        send(msg, g);
        send(msg, g2);
    }

    public static void send(String msg, Group g, Group g2, Group g3) {
        send(msg, g);
        send(msg, g2);
        send(msg, g3);
    }

    public void save() {
        if (!logged) {
            //Server.out("Non puoi salvare un utente sloggato "+getScreenName(true)+ "!");
            return;
        }
        Filez.writeFile("./utenti/" + name + ".dat", name + " " + password + " " + group.getName() + " " + getIP()); //scrivo su file
    }

    public void keepAlive() {
        if (oos == null) {
            Server.out("[DEBUG] Tentativo di tenere viva una connessine nulla\n");
            return;
        }
        try {
            oos.writeObject(new SyncObject());
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            Server.out("Impossibile mantenere la connessione con " + getScreenName(true) + ". Disconnessione");
            disconnect();
        }
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getScreenName(boolean showIP) {
        if (showIP) {
            if (logged) {
                return name + " ( " + getIP() + " )";
            } else {
                return getIP();
            }
        } else {
            if (logged) {
                return name;
            } else {
                return "Utente";
            }
        }
    }

    public static void keepAliveAll() {
        for (ClientHandler ch : clients) {
            ch.keepAlive();
        }
    }

    public static void disconnectAll() {
        Server.out("Disconnetto tutti...");
        for (ClientHandler c : clients) {
            c.disconnect();
        }
    }

    public static void saveAll() {
        Server.out("Salvo tutti gli utenti...");
        for (ClientHandler c : clients) {
            c.save();
        }
    }

    public String getIP() {
        return s.getInetAddress().getHostAddress();
        //return s.getInetAddress().toString().split("\\")[1];
        //return s.getInetAddress().toString().substring(1);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Socket getSocket() {
        return s;
    }

    /*public void setAdmin(boolean admin) {
     if (this.admin == true && admin == false) {
     send("Il tuo stato da amministratore è stato revocato!\n");
     sendToAdmins(getScreenName(true) + " non è più admin!\n");
     } else if (this.admin == false && admin == true) {
     send("Ora sei un amministratore!\n");
     sendToAdmins(getScreenName(true) + " è ora un admin!\n");
     }
     this.admin = admin;
     save();
     }*/
    public boolean isConnected() {
        return connected;
    }

    public boolean isLoggedIn() {
        return logged;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLoggedIn(boolean logged) {
        this.logged = logged;
    }
}
