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
package core;

import java.util.ArrayList;
import net.ClientHandler;
import net.Server;
import utilz.Filez;

/**
 * This represents a channel.
 *
 * @author Fazo
 */
public class Channel {

    private static ArrayList<Channel> channels = new ArrayList<Channel>();
    private ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
    private String name = "", password = "";
    private boolean autoDeleteWhenEmpty = true;

    /**
     * Creates a new channel not password protected.
     *
     * @param name il nome del nuovo canale.
     */
    public Channel(String name) {
        this.name = name;
        channels.add(this);
    }

    /**
     * Creates a new password protected channel.
     *
     * @param name new channel's name
     * @param password new channel's password. Null means no password.
     */
    public Channel(String name, String password) {
        this(name);
        this.password = password;
    }

    /**
     * Joins the given client to the channel.
     *
     * @param ch the client to add.
     */
    public void add(ClientHandler ch) {
        if (clients.contains(ch)) {
            Server.out().info(Settings.language.getSentence("addUserError").print());
        }
        ch.send(Settings.language.getSentence("youEntered").print(name));
        send(Settings.language.getSentence("guyEntered").print(ch.getScreenName(false)));
        clients.add(ch);
        ch.getJoinedChannels().add(this);
    }

    /**
     * Removes a client from the channel, if he has joined it. Deletes channel
     * if empty.
     *
     * @param ch the client to remove.
     */
    public void remove(ClientHandler ch) {
        if (clients.contains(ch)) {
            ch.send(Settings.language.getSentence("youExited").print(name));
            ClientHandler.send(Settings.language.getSentence("guyExitedAdmin").print(ch.getScreenName(true) + " " + name), Settings.groupAdmin);
            ch.send(Settings.language.getSentence("youExited").print("\"" + name + "\""));
            ClientHandler.send(Settings.language.getSentence("guyExitedAdmin").print(ch.getScreenName(true) + " " + name), Settings.groupAdmin);
        }
        ch.getJoinedChannels().remove(this);
        clients.remove(ch);
        if (clients.isEmpty()) {
            delete();
        } else {
            send(Settings.language.getSentence("guyExited").print(ch.getScreenName(false) + " " + name));
        }
    }

    /**
     * Kicks everyone out of the channel.
     */
    public void clear() {
        for (ClientHandler ch : clients) {
            ch.getJoinedChannels().remove(this);
        }
        clients.clear();
    }

    /**
     * Saves the channel to file.
     *
     * @throws UnsupportedOperationException if the channel has no password.
     */
    public void save() {
        if (password == "") {
            throw new UnsupportedOperationException("Can't save channel with no password: what's to save?");
            //return; //non si può salvare un canale senza pass
        }
        Filez.writeFile("./channels/" + name + ".dat", password);
    }

    /**
     * Deletes channel and removes connected clients.
     */
    public void delete() {
        channels.remove(this);
        clear();
    }

    /**
     * Sends a message to the clients that joined this channel.
     *
     * @param s il messaggio da inviare.
     */
    public void send(String s) {
        if (s.startsWith("[")) {
            for (ClientHandler c : clients) {
                c.send("[ " + name + " ]" + s);
            }
        } else {
            for (ClientHandler c : clients) {
                c.send("[ " + name + " ] " + s);
            }
        }
    }

    /**
     * Returns the channel list
     *
     * @return the channel list
     */
    public static ArrayList<Channel> getChannels() {
        return channels;
    }

    /**
     * Retuns the first channel with given name. Returns null if there's no
     * channel
     *
     * @param s the name of the required channel
     * @return the istance of the channel with given name, or null if it doesn't
     * exist.
     */
    public static Channel get(String s) {
        for (Channel g : channels) {
            if (g.getName().equals(s)) {
                return g;
            }
        }
        return null;
    }

    /**
     *
     * @return the channel's name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return the list of connected clients to the channel.
     */
    public ArrayList<ClientHandler> getClients() {
        return clients;
    }

    /**
     * Wether the channel ha a password set or not.
     *
     * @return wether the channel ha a password set or not.
     */
    public boolean isPrivate() {
        return password != null;
    }

    /**
     * Returns the channel's password
     *
     * @return the set password, or null if there's no password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set a new channel password
     *
     * @param password the new password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Removes a client from all channels.
     *
     * @param c the client to remove
     * @param includeDefaultChannel wether to remove the client from the default
     * channel or not.
     */
    public static void removeFromAll(ClientHandler c, boolean includeDefaultChannel) {
        for (Channel chan : channels) {
            if (!includeDefaultChannel && chan == Settings.globalChannel) {
                continue;
            }
            chan.getClients().remove(c);
        }
    }

    /**
     * Wether the channel has to be deleted when empty.
     *
     * @return true if the channel has to be deleted from memory when empty.
     */
    public boolean isAutodelete() {
        return autoDeleteWhenEmpty;
    }

    /**
     * Sets wether the channel has to autodestroy from memory when no clients
     * are connected to it.
     *
     * @param hasToAutodelete self-explanatory
     */
    public void setAutodelete(boolean hasToAutodelete) {
        this.autoDeleteWhenEmpty = hasToAutodelete;
    }
}
