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
    private boolean connected = false;

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
                    try { //25 ms di pausa
                        sleep(25);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        o = ois.readObject();
                    } catch (IOException ex) {
                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
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
        send(getIP() + " si è connesso!\n", Settings.groupAdmin);
        sendToAll("Qualcuno si è connesso!\n");
        Server.out("Inizializzati Input e Output streams per " + getIP() + " con successo e aggiunto alla lista client.\n");
        /*if (getIP().equals("127.0.0.1")) {
         setGroup(Settings.groupAdmin);
         send("Benvenuto localhost! Poteri admin consegnati.\n");
         }*/
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

    public void sendToChannel(String msg, Channel c) {
        for (ClientHandler ch : c.getClients()) {
            if (!ch.isConnected()||!ch.getGroup().can("channel")) {
                c.getClients().remove(ch);
            } else {
                ch.send(msg);
            }
        }
    }

    public void disconnect() {
        if (!connected) {
            return;
        }
        if (name == null) {
            Server.out("Disconnetto " + getIP());
            send(getIP() + " si è disconnesso!\n", Settings.groupAdmin);
            send("Utente si è disconnesso!\n", Settings.groupUser, Settings.groupGuest);
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
                //Controllo se il nome combacia alla stringa data
                if (ch.getName() != null && ch.getName().equals(n)) {
                    return ch;
                }
                //Controllo se l'IP combacia alla stringa data
                if (ch.getIP().equals(n)) {
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
        for (ClientHandler cl : clients) {
            if (g == null || cl.getGroup() == g) {
                cl.send(msg);
            }
        }
        /*if (g == null) {
         Server.out("[SEND][ALL] " + msg);
         } else Server.out("[SEND][" + g.getName() + "] " + msg);*/
    }

    public static void send(String msg, Group g, Group g2) {
        send(msg, g);
        send(msg, g2);
        //Server.out("[SEND][" + g.getName() + "]["+g2.getName()+"] " + msg);
    }

    public static void send(String msg, Group g, Group g2, Group g3) {
        send(msg, g);
        send(msg, g2);
        send(msg, g3);
        //Server.out("[SEND][" + g.getName() + "]["+g2.getName()+"]["+g3.getName()+"] " + msg);
    }

    public void save() {
        if (group == Settings.groupGuest) {
            //Server.out("Non puoi salvare un utente sloggato "+getScreenName(true)+ "!");
            return;
        }
        try {
            Filez.writeFile("./utenti/"
                    + name
                    + ".dat", name
                    + " " + getPassword()
                    + " " + group.getName()
                    + " " + getIP()); //scrivo su file
        } catch (Exception ex) {
            Server.out("Errore nel salvare il file per l'utente " + getScreenName(true));
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getClientList(boolean showIP) {
        String list = "";
        int i = 0;
        for (ClientHandler ch : ClientHandler.getClients()) {
            i++;
            if (showIP) {
                list += i + " - " + ch.getScreenName(true) + "\n";
            } else {
                list += i + " - " + ch.getScreenName(false) + "\n";
            }
        }
        return list;
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

    public boolean login(String lname, String pass) {
        if (lname == null || pass == null) {
            send("[BUG DETECT] Errore nel login\n");
            return false;
        }
        ArrayList<String> ff = Utils.toList(Filez.getFileContent("./utenti/" + lname + ".dat"), " ");
        if (ff == null || ff.size() < 3) {
            setName(lname);
            setPassword(pass);
            setGroup(Settings.groupUser);
            save();
            ClientHandler.send("Registrato nuovo utente " + getName() + " con password " + getPassword() + "\n", Settings.groupAdmin);
            Server.out("Registrato nuovo utente " + getName() + " con password " + getPassword() + "\n");
            send("Registrato come nuovo utente: " + getScreenName(false) + "\n");
            return true;
        } else if (pass.equals(ff.get(1))) {
            setName(lname);
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
        } else {
            send("Password errata!\n");
        }
        return true;
    }

    public void logout() {
        if (group == Settings.groupGuest) {
            return;
        }
        password = null;
        group = Settings.groupGuest;
        send(getScreenName(false) + " ha eseguito il logout\n", Settings.groupGuest, Settings.groupUser);
        send(getScreenName(true) + " ha eseguito il logout\n", Settings.groupAdmin);
        name = null;

    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

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
        Server.out("[DEBUG]Password per " + getScreenName(true) + " e' " + password);
        return password;
    }

    public void setPassword(String password) {
        Server.out("[DEBUG] Password per " + getScreenName(true) + " è cambiata da " + this.password + " a " + password);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        receiver.setName(getScreenName(true));
    }
}
