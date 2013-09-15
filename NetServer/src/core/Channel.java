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
package core;

import java.util.ArrayList;
import net.ClientHandler;
import net.Server;
import utilz.Filez;
import utilz.Utils;

/**
 *
 * @author Fazo
 */
public class Channel {

    private static ArrayList<Channel> channels = new ArrayList<Channel>(); //lista canali
    private ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>(); //client connessi al canale
    private String name = "", password = "";

    public Channel(String name) {
        this.name = name;
        channels.add(this);
    }

    public Channel(String name, String password) {
        this(name);
        this.password = password;
    }

    public void add(ClientHandler ch) {
        ch.send("Sei entrato nel canale " + name + "\n");
        send(ch.getScreenName(true) + " è entrato nel canale!\n");
        clients.add(ch);
        ch.getJoinedChannels().add(this);
    }

    public void remove(ClientHandler ch) {
        if (clients.contains(ch)) {
            ch.send("Sei uscito dal canale " + name + "\n");
            ClientHandler.send(ch.getScreenName(true) + " è uscito dal canale " + name + "\n", Settings.groupAdmin);
        }
        ch.getJoinedChannels().remove(this);
        clients.remove(ch);
        if (clients.isEmpty()) {
            delete();
        }
    }

    public void clear() {
        for (ClientHandler ch : clients) {
            ch.getJoinedChannels().remove(this);
        }
        clients.clear();
    }

    public void save() {
        if (password == "") {
            return; //non si può salvare un canale senza pass
        }
        Filez.writeFile("./channels/" + name + ".dat", password);
    }

    public void delete() {
        channels.remove(this);
        clear();
    }

    public void send(String s) {
        for (ClientHandler c : clients) {
            c.send("[ " + name + " ]" + s);
            Server.out(s);
        }
    }

    public static ArrayList<Channel> getChannels() {
        return channels;
    }

    public static Channel get(String s) { //ritorna l'istanza partendo dal nome
        for (Channel g : channels) {
            if (g.getName().equals(s)) {
                return g;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public ArrayList<ClientHandler> getClients() {
        return clients;
    }

    public boolean isPrivate() {
        if (password == null) {
            return true;
        }
        return false;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static void removeFromAll(ClientHandler c) {
        for (Channel chan : Channel.getChannels()) {
            chan.remove(c);
        }
    }
}
