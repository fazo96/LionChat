
package core;

import java.util.ArrayList;
import net.Server;

/**
 * I gruppi servono a definire i permessi di ogni utente (cosa può o non può fare o vedere).\n
 * Il sistema di permessi è ancora acerbo e sperimentale, ma è usabile.
 * @author Fazo
 */
public class Group {

    private static ArrayList<Group> groups = new ArrayList<Group>(); //lista dei gruppi, per ora poco utile
    private static Group defaultGroup = null; //Il gruppo default per i client appena connessi
    private String name = "group"; //Nome del gruppo
    // private static ArrayList<String> permissions = new ArrayList<String>(); //lista dei permessi per ogni gruppo
    private String permissions;

    /**
     * Crea un nuovo gruppo con il nome dato e la stringa di permessi dati\n
     * NB: Il sistema di permessi è ancora acerbo e sperimentale
     * @param name il nome del gruppo.
     * @param permissions un elenco di permessi separato da spazi.
     */
    public Group(String name, String permissions) {
        this.name = name; 
        this.permissions = permissions;
        //this.permissions = Utils.toList(permissions, " ");
        if (groups.isEmpty()) { //il primo gruppo diventa il gruppo default
            setDefaultGroup(this);
        } 
        groups.add(this); //aggiungo il gruppo alla lista.
        Server.out("Inizializzato gruppo " + name);
    }
    /**
     * Ritorna l'istanza di un gruppo partendo dal nome.
     * @param s il nome del gruppo di cui si necessita l'istanza.
     * @return l'istanza il cui nome corrisponde a quello del parametro
     */
    public static Group get(String s) { //ritorna l'istanza partendo dal nome
        for (Group g : groups) {
            if (g.getName().equalsIgnoreCase(s)) {
                return g;
            }
        }
        return null;
    }
/**
 * Controlla se il gruppo possiede il permesso indicato
 * @param perm il permesso da controllare (senza spazi!)
 * @return se il gruppo possiede il permesso indicato.
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
 * Ritorna la lista dei gruppi esistenti.
 * @return la lista dei gruppi esistenti. 
 */
    public static ArrayList<Group> getGroups() {
        return groups;
    }
/**
 * Ritorna il gruppo che viene assegnato a ogni utente appena connesso. 
 * @return il gruppo che viene assegnato a ogni utente appena connesso. 
 */
    public static Group getDefaultGroup() {
        return defaultGroup;
    }
/**
 * Imposta il nuovo gruppo di default, al quale ogni utente connesso viene assegnato.
 * @param defaultGroup il nuovo gruppo di default.
 */
    public static void setDefaultGroup(Group defaultGroup) {
        Group.defaultGroup = defaultGroup;
        Server.out(defaultGroup.getName() + " è ora il gruppo predefinito.");
    }
/**
 * 
 * @return il nome del gruppo
 */
    public String getName() {
        return name;
    }
/**
 * Imposta il nuovo nome del gruppo
 * @param name il nuovo nome
 */
    public void setName(String name) {
        this.name = name;
    }
}
