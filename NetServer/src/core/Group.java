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
import net.Server;

/**
 *
 * @author Fazo
 */
public class Group {

    private static ArrayList<Group> groups = new ArrayList<Group>(); //lista dei gruppi, per ora poco utile
    private static Group defaultGroup = null; //Il gruppo default per i client appena connessi
    private String name = "group"; //Nome del gruppo
   // private static ArrayList<String> permissions = new ArrayList<String>(); //lista dei permessi per ogni gruppo

    //la rimozione dei gruppi non è supportata perchè per ora sarebbe un'inutilità e una
    //perdita di tempo da programmare.
    //La maggior parte di questo codice non è usato, più avanti se sarà utile ok, altrimenti
    //sarà rimosso
    
    public Group(String name/*, String permissions*/) {
        this.name = name;
        //this.permissions = Utils.toList(permissions, " ");
        if (groups.isEmpty()) { //il primo gruppo diventa il gruppo default
            setDefaultGroup(this);
        }
        groups.add(this); //aggiungo il gruppo alla lista.
        Server.out("Inizializzato gruppo " + name);
    }

    public static Group get(String s) { //ritorna l'istanza partendo dal nome
        for (Group g : groups) {
            if (g.getName().equalsIgnoreCase(s)) {
                return g;
            }
        }
        return null;
    }

    public static ArrayList<Group> getGroups() {
        return groups;
    }

    public static Group getDefaultGroup() {
        return defaultGroup;
    }

    public static void setDefaultGroup(Group defaultGroup) {
        Group.defaultGroup = defaultGroup;
        Server.out(defaultGroup.getName() + " è ora il gruppo predefinito.");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /*public static ArrayList<String> getPermissions() {
        return permissions;
    }*/
}
