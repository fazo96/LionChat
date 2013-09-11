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

/**
 *
 * @author Fazo
 */
public class Channel {

    private static ArrayList<Channel> channels = new ArrayList<Channel>(); //lista canali
    private ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>(); //client connessi al canale
    private String name = "";

    public Channel(String name) {
        this.name = name;
        channels.add(this);
    }

    public void delete() {
        channels.remove(this);
        clients.clear();
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
}
