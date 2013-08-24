/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import net.Server;
import utilz.Filez;

/**
 *
 * @author Fazo
 */
public class Settings {

    private static int port;
    private static String helpMsg, adminHelpMsg, motd;
    public static Group groupGuest, groupUser, groupAdmin;
    private static boolean init = false;

    public static void init() {
        if (init) {
            return; //inutile fare roba un'altra volta se è già stata fatta
        }
        //Caricamento variabili default
        port = 7777;
        helpMsg = "HELPZ";
        adminHelpMsg = "ADMINHELPZ";
        motd = "MOTDZ";
        load(); //Tentativo di caricare da file. Se fallisce, rimangono i default
        //IMPOSTAZIONE PERMESSI! Inizializzazione gruppi. Per ora non supporta caricamento da file.
        groupGuest=new Group("guest","help chat login chi motd");
        groupUser=new Group("user","help chat logout chi motd");
        groupAdmin=new Group("admin","*"); //il gruppo admin bypassa ogni controllo dei permessi, quindi è inutile impostarli.
        init=true;
    }

    public static void save() { //salvataggio dati.
        Filez.writeFile("./settings/helpMsg.txt", helpMsg);
        Filez.writeFile("./settings/adminHelpMsg.txt", adminHelpMsg);
        Filez.writeFile("./settings/motd.txt", motd);
    }

    public static void load() {
        try {
            Server.out("Caricamnto di net");
            port = Integer.parseInt(Filez.getFileContent("./settings/net.txt"));
            if (port <= 0 || port > 65535) {
                port = 7777;
            }
        } catch (Exception ex) { //Se qualcosa va male vado sul sicuro e ricreo/riscrivo il file.
            Server.out("Fallito. Creazione file");
            Filez.writeFile("./settings/net.txt", "" + port);
            port = 7777;
        }
        String a, b, c;
        //Inizio il tentativo di caricare da file.
        Server.out("Caricamnto di helpMsg");
        a = Filez.getFileContent("./settings/helpMsg.txt");
        if (a == null) {
            Server.out("Fallito: creazione file.");
            Filez.writeFile("./settings/helpMsg.txt", helpMsg);
        } else {
            helpMsg = a;
        }
        Server.out("Caricamnto di adminHelpMsg");
        b = Filez.getFileContent("./settings/adminHelpMsg.txt");
        if (b == null) {
            Server.out("Fallito: creazione file.");
            Filez.writeFile("./settings/adminHelpMsg.txt", adminHelpMsg);
        } else {
            adminHelpMsg = b;
        }
        Server.out("Caricamnto di adminHelpMsg");
        c = Filez.getFileContent("./settings/motd.txt");
        if (c == null) {
            Filez.writeFile("./settings/motd.txt", motd);
        } else {
            motd = c;
        }
    }
    
    //Da qui in poi solo getters
    
    public static String getHelpMsg() {
        return helpMsg;
    }

    public static String getAdminHelpMsg() {
        return adminHelpMsg;
    }

    public static String getMotd() {
        return motd;
    }

    public static boolean isInit() {
        return init;
    }

    public static int getPort() {
        return port;
    }
}
