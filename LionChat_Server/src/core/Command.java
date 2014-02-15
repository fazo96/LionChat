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

import net.ClientHandler;
import net.Server;
import security.Security;
import utilz.Utils;

/**
 * The universal intepreter class for the server.
 *
 * @author fazo
 */
public class Command {

    /**
     * Analyzes a string received from the given client, and acts as
     * consequence. If the client is null, then it is assumed the string came
     * from the server console.
     *
     * @param s the string to analyze.
     * @param c the client that sent that. Null if it came from the server
     * console.
     */
    public static void execute(String s, ClientHandler c) {
        s = s.trim(); // trim string before analyzing it
        if (!Settings.isInit()) {
            Settings.init(); //initialize settings if needed.
        }
        if (!Utils.isValid(s)) {
            Server.out().info("[!] INVALID STRING RECEIVED! Very strange!");
            if (c != null) {
                //c.send("/err Invalid string\n");
            }
            return;
        }
        if (!s.startsWith("/")) { // it's a message, not a command
            if (c == null) {
                Server.out().info("[ Server ]: " + s);
                Settings.globalChannel.send("[ Server ]: " + s + "\n");
                return;
            }
            if (!c.getGroup().can("chat")) {
                c.send("[!] You can't use the chat.");
            } else {
                Server.out().info("[" + c.getWritingChannel().getName() + "][ " + c.getScreenName(true) + " ]: " + s);
                c.getWritingChannel().send("[ " + c.getScreenName(false) + " ]: " + s + "\n");
                /*ClientHandler.send("[ " + c.getScreenName(false) + " ]: " + s + "\n", Settings.groupGuest, Settings.groupUser);
                 ClientHandler.send("[ " + c.getScreenName(true) + " ]: " + s + "\n", Settings.groupAdmin);*/
            }
            return;
        }
        String cmd[] = s.split(" "); // it's a command, so split it in words
        int l = cmd.length;
        if (l <= 0) {
            c.send("[!] Empty command!\n");
            return; // empty command
        }

        // COMMANDS
        if (c != null && cmd[0].equalsIgnoreCase(Security.publicKeyRequest)) {
            // The client is asking for the encryption key
            c.sendServerKey(); // Let's send the key
            return;
        }

        // LOGIN
        if (c != null && c.getGroup().can("login") && cmd[0].equalsIgnoreCase("/login")) {
            if (l != 3) { //Wrong number of parameters
                c.send(Settings.language.getSentence("loginUsage").print());
                return;
            }
            c.login(cmd[1], cmd[2]);
            return;
        }

        // ACCOUNT
        if (cmd[0].equalsIgnoreCase("/account")) {
            if (c == null) {
                Server.out().info("You're operating from console! You don't have nor need an account\n");
            } else {
                c.send(Settings.language.getSentence("accountInfo").print(c.getScreenName(false) + " " + c.getGroup().getName() + " " + c.getIP()));
            }
            return;
        }

        // HELP
        if (c != null && c.getGroup().can("help") && cmd[0].equalsIgnoreCase("/help")) { //mostra il messaggio di help
            c.send(Settings.getHelpMsg());
            return;
        }

        // LOGOUT
        if (c != null && c.getGroup().can("logout") && cmd[0].equalsIgnoreCase("/logout")) {
            c.logout();
            return;
        }

        // PASSWORD CHANGE
        if (c != null && c.getGroup().can("logout") && cmd[0].equalsIgnoreCase("/password")) {
            if (l != 3) {
                c.send(Settings.language.getSentence("passwordUsage").print());
                return;
            }
            if (!cmd[1].equals(c.getPassword())) {
                c.send(Settings.language.getSentence("wrongPassword").print());
                return;
            }
            c.setPassword(cmd[2]);
            c.save();
            c.send(Settings.language.getSentence("passwordChangedSuccessfully").print());
            return;
        }
        //if (c == null || c.getGroup() == Settings.groupUser || c.getGroup() == Settings.groupAdmin) {

        // WHO IS ONLINE
        if ((c == null || c.getGroup().can("who")) && cmd[0].equalsIgnoreCase("/who")) { // Prints users list
            if (ClientHandler.getClients().isEmpty()) {
                if (c == null) {
                    Server.out().info("");
                } else {
                    c.send(Settings.language.getSentence("nobodyOnline").print());
                }
                return;
            }
            String list;
            if (c == null || c.getGroup() == Settings.groupAdmin) {
                list = ClientHandler.getClientList(true);
            } else {
                list = ClientHandler.getClientList(false);
            }
            if (c == null) {
                Server.out().info(list);
            } else {
                c.send(list);
            }
            return;
        }

        // GROUP
        if (c != null && cmd[0].equalsIgnoreCase("/group")) {
            c.send(Settings.language.getSentence("group").print(c.getGroup().getName()));
            return;
        }

        // CHANNEL COMMANDS
        if (c != null && c.getGroup().can("c") && (cmd[0].equals("/channel") || cmd[0].equals("/c"))) {
            if (l == 1) {
                c.send(Settings.language.getSentence("cUsage").print());
                return;
            }

            // (CHANNEL) > LIST
            if (cmd[1].equalsIgnoreCase("list")) {
                String tosend = Settings.language.getSentence("yourChannels").print();
                for (Channel ch : c.getJoinedChannels()) {
                    tosend += "\n[ " + ch.getName() + " ]";
                    if (ch == c.getWritingChannel()) {
                        tosend += " (Writing)";
                    }
                }
                c.send(tosend + "\n");
                return;
            }

            // (CHANNEL) > JOIN CHANNEL
            Channel chan;
            if (l >= 3 && cmd[1].equalsIgnoreCase("join")) {
                chan = Channel.get(cmd[2]);
                if (chan == null) { // Channel doesn't exist: creating it
                    chan = new Channel(cmd[2]);
                    if (l == 4) {
                        chan.setPassword(cmd[3]);
                        Server.out().info("Password for chanel \"" + chan.getName() + "\": " + cmd[3]);
                    }
                    chan.add(c);
                    if (chan.isPrivate()) { // Channel created is private
                        if (c.getGroup() != Settings.groupAdmin) {
                            c.send(Settings.language.getSentence("createdChannelWithPassword").print("\"" + chan.getName()) + "\"");
                        }
                        ClientHandler.send(Settings.language.getSentence("createdChannelWithPassword").print("\"" + chan.getName() + "\""), Settings.groupAdmin);
                    } else { // Channel created is public
                        if (c.getGroup() != Settings.groupAdmin) {
                            c.send(Settings.language.getSentence("createdChannel").print("\"" + chan.getName()) + "\"");
                        }
                        ClientHandler.send(Settings.language.getSentence("createdChannel").print("\"" + chan.getName() + "\""), Settings.groupAdmin);
                    }
                    return;
                }
                // The channel exists
                if (!chan.isPrivate()) {
                    chan.add(c);
                    return;
                } else {
                    if (l >= 4 && chan.getPassword().equals(cmd[3])) {
                        chan.add(c);
                        c.send(Settings.language.getSentence("passwordCorrect").print());
                    } else {
                        c.send(Settings.language.getSentence("wrongChannelPassword").print());
                    }
                    return;
                }
            }

            // (CHANNEL) > LEAVE CHANNEL
            if (cmd[1].equalsIgnoreCase("leave")) {
                if (c.getWritingChannel() == Settings.globalChannel) {
                    c.send(Settings.language.getSentence("cantExitGlobal").print());
                    return;
                }
                c.getWritingChannel().remove(c);
                c.setWritingChannel(Settings.globalChannel);
                return;
            }

            // (CHANNEL) > SET WRITING CHANNEL
            // If the first parameter is not a command, try to set the writing channel for the user
            chan = Channel.get(cmd[1]);
            //check if the channel exist and the user is a member of the channel

            if (chan == null || !chan.getClients().contains(c)) {
                c.send(Settings.language.getSentence("channelError").print());
                return;
            }
            c.setWritingChannel(chan);
            c.send(Settings.language.getSentence("writingOn").print(chan.getName()));
            return;
        }

        // HASH A STRING
        if ((c == null || c.getGroup().can("hash")) && cmd[0].equalsIgnoreCase("/hash")) { //hash di una stringa
            if (cmd.length != 2) {
                if (c == null) {
                    Server.out().info(Settings.language.getSentence("hashUsage").print());
                } else {
                    c.send(Settings.language.getSentence("hashUsage").print());
                }
                return;
            }
            if (c == null) {
                Server.out().info("hash of " + cmd[1] + ": " + Security.hash(cmd[1]));
            } else {
                c.send("hash of " + cmd[1] + ": " + Security.hash(cmd[1]) + "\n");
            }
            return;
        }

        // ADMIN HELP
        if ((c == null || c.getGroup().can("ahelp")) && cmd[0].equalsIgnoreCase("/adminhelp")) {
            if (c == null) {
                System.out.print(Settings.getAdminHelpMsg());
            } else {
                c.send(Settings.getAdminHelpMsg());
            }
            return;
        }

        // MANUAL GARBAGE COLLECTOR CALL
        if ((c == null || c.getGroup().can("gc")) && cmd[0].equalsIgnoreCase("/gc")) {
            if (c == null) {
                Server.out().info("Calling garbage collector...");
            } else {
                c.send("Calling garbage collector...\n");
            }
            Runtime.getRuntime().gc();
            if (c == null) {
                Server.out().info("Done.");
            } else {
                c.send("Done.\n");
            }
            return;
        }

        // SETS A USER'S GROUP
        if ((c == null || c.getGroup().can("setgroup")) && cmd[0].equalsIgnoreCase("/setGroup")) {
            if (l != 3) {
                if (c == null) {
                    Server.out().info(Settings.language.getSentence("setGroupUsage").print());
                } else {
                    c.send(Settings.language.getSentence("setGroupUsage").print());
                }
                return;
            }
            ClientHandler ch;
            if ((ch = ClientHandler.get(cmd[1])) == null) {
                if (c == null) {
                    Server.out().info(Settings.language.getSentence("userNotFound").print());
                } else {
                    c.send(Settings.language.getSentence("userNotFound").print());
                }
                return;
            }
            Group g = Group.get(cmd[2]);
            if (g == null) {
                if (c == null) {
                    Server.out().info(Settings.language.getSentence("groupNotFound").print());
                } else {
                    c.send(Settings.language.getSentence("groupNotFound").print());
                }
                return;
            } else if (g == ch.getGroup()) {
                if (c == null) {
                    Server.out().info(Settings.language.getSentence("alreadyIsInGroup").print(c.getScreenName(false) + " " + g.getName()));
                } else {
                    c.send(Settings.language.getSentence("alreadyIsInGroup").print(c.getScreenName(false) + " " + g.getName()));
                }
                return;
            } else if (g == Settings.groupGuest) { // user must be logged out.
                if (c == null) {
                    Server.out().info("Forcing logout for " + ch.getScreenName(true));
                }
                ClientHandler.send("Forcing logout for " + ch.getScreenName(false) + "\n", Settings.groupAdmin);
                ch.save();
                ch.logout();
                return;
            } else { //everything fine.
                if (c == null) {
                    Server.out().info(ch.getScreenName(true) + " is now part of group " + g.getName());
                }
                ClientHandler.send(ch.getScreenName(true) + " is now part of group " + g.getName() + "\n", Settings.groupAdmin);
                ch.setGroup(g);
                ch.save();
                return;
            }
        }

        // SERVER STATUS
        if ((c == null || c.getGroup().can("status")) && cmd[0].equalsIgnoreCase("/status")) {
            //Date d= Server.getTimePassed();
            if (c == null) {
                Server.out().info(Server.getStatus());
            } else {
                c.send(Server.getStatus() + "\n");
            }
            return;
        }

        // STOP THE SERVER
        if ((c == null || c.getGroup().can("stop")) && cmd[0].equalsIgnoreCase("/stop")) {
            if (c == null) {
                Server.out().info("Server shutting down by console command");
            } else {
                c.send("Server shutting down by your command\n");
                if (c.getGroup() == Settings.groupUser) {
                    ClientHandler.send("Server shutting down by user: " + c.getScreenName(true) + "\n", Settings.groupAdmin);
                }
            }
            ClientHandler.send("Server shutting down by console command", Settings.groupAdmin);
            Server.stop();
            return;
        }

        // KICK
        if ((c == null || c.getGroup().can("kick")) && cmd[0].equalsIgnoreCase("/kick")) {
            if (l == 1) { // Kick all
                if (c != null) {
                    c.send(Settings.language.getSentence("disconnectsEverybody").print(c.getScreenName(true)), Settings.groupAdmin);
                    if (c.getGroup() == Settings.groupUser) {
                        Settings.globalChannel.send(Settings.language.getSentence("disconnectsEverybody").print(c.getScreenName(false)));
                    }
                }
                Server.out().info("Disconnecting everybody!");
                ClientHandler.disconnectAll();
                return;
            } else if (l == 2) { // Kick specific user
                ClientHandler ch;
                if ((ch = ClientHandler.get(cmd[1])) != null) {
                    if (c != null) {
                        c.send(c.getScreenName(true) + " disconnects " + ch.getScreenName(true) + "\n", Settings.groupAdmin);
                        Server.out().info(c.getScreenName(true) + " disconnects " + ch.getScreenName(true));
                    } else {
                        Server.out().info("Disconnecting " + ch.getScreenName(true));
                    }
                    ch.disconnect();
                } else if (c == null) {
                    Server.out().info(Settings.language.getSentence("userNotFound").print());
                } else {
                    c.send(Settings.language.getSentence("userNotFound").print());
                }
            } else if (c == null) {
                Server.out().info(Settings.language.getSentence("kickUsage").print());
            } else {
                c.send(Settings.language.getSentence("kickUsage").print());
            }
            return;
        }

        //SAVE
        if ((c == null || c.getGroup().can("save")) && cmd[0].equalsIgnoreCase("/save")) {
            if (c != null) {
                c.send("Saving server data...\n");
            }
            Server.save();
            if (c != null) {
                c.send("Server data saved!\n");
            }
            return;
        }

        // Command not found
        if (c == null) {
            Server.out().info("Unknown server command or command not usable by server console");
        } else {
            c.send(Settings.language.getSentence("unknownCommand").print());
        }
    }

    /**
     * Don't use this.
     *
     * @throws UnsupportedOperationException can't istance this class
     */
    private Command() {
        throw new UnsupportedOperationException("can't istance this class");
    }
}
