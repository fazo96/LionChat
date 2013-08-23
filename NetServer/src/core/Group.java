/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.ArrayList;
import net.Server;
import utilz.Utils;

/**
 *
 * @author Fazo
 */
public class Group {

    private static ArrayList<Group> groups = new ArrayList<Group>();
    private static Group defaultGroup = null;
    private String name = "group";
    private static ArrayList<String> permissions = new ArrayList<String>();
    //private String permissions = "";

    public Group(String name, String permissions) {
        this.name = name;
        this.permissions = Utils.toList(permissions, " ");
        if (groups.isEmpty()) {
            setDefaultGroup(this);
        }
        groups.add(this);
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

    public static ArrayList<String> getPermissions() {
        return permissions;
    }
}
