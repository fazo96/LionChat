package core;

import java.util.ArrayList;
import net.Server;

/**
 * Groups define the permissions that a user has.
 *
 * @author Fazo
 */
public class Group {

    private static ArrayList<Group> groups = new ArrayList<Group>(); //lista dei gruppi, per ora poco utile
    private static Group defaultGroup = null; //Il gruppo default per i client appena connessi
    private String name = "group"; //Nome del gruppo
    // private static ArrayList<String> permissions = new ArrayList<String>(); //lista dei permessi per ogni gruppo
    private String permissions;

    /**
     * Creates new group with given permissions.
     *
     * @param name the group's name
     * @param permissions a string containing the list of permissions separated
     * by spaces
     */
    public Group(String name, String permissions) {
        this.name = name;
        this.permissions = permissions;
        if (groups.isEmpty()) { // the first group is the defualt group
            setDefaultGroup(this);
        }
        groups.add(this); // add the group to the list
        Server.out("Intitializing group:" + name);
    }

    /**
     * Returns the istance of the given group name, or null if it doesn't exist.
     *
     * @param s the name of the group to look for.
     * @return the istance corresponding to the name given.
     */
    public static Group get(String s) {
        for (Group g : groups) {
            if (g.getName().equalsIgnoreCase(s)) {
                return g;
            }
        }
        return null;
    }

    /**
     * Checks if this group's members have the given permission
     *
     * @param perm the permission to check (no spaces!)
     * @return wether the group has the given permission.
     */
    public boolean can(String perm) {
        if (this == Settings.groupAdmin) {
            return true;
        }
        if (permissions.contains(perm)) {
            return true;
        }
        return false;
    }

    /**
     * Returns the list of existing groups
     *
     * @return the list of existing groups
     */
    public static ArrayList<Group> getGroups() {
        return groups;
    }

    /**
     * Returns the group that is assigned to every connected user.
     *
     * @return the default group
     */
    public static Group getDefaultGroup() {
        return defaultGroup;
    }

    /**
     * Sets the new default group.
     *
     * @param defaultGroup new default group.
     */
    public static void setDefaultGroup(Group defaultGroup) {
        Group.defaultGroup = defaultGroup;
        Server.out(defaultGroup.getName() + " is now the default group.");
    }

    /**
     *
     * @return group's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets a new name.
     *
     * @param name new name
     */
    public void setName(String name) {
        this.name = name;
    }
}
