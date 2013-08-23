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
        port = 7777;
        helpMsg = "HELPZ";
        adminHelpMsg = "ADMINHELPZ";
        motd = "MOTDZ";
        load();
        //IMPOSTAZIONE PERMESSI!
        groupGuest=new Group("guest","help chat login chi motd");
        groupUser=new Group("user","help chat logout chi motd");
        groupAdmin=new Group("admin","*"); //il gruppo admin bypassa ogni controllo dei permessi.
        init=true;
    }

    public static void save() {
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
        } catch (Exception ex) {
            Server.out("Fallito. Creazione file");
            Filez.writeFile("./settings/net.txt", "" + port);
            port = 7777;
        }
        String a, b, c;
        //boolean successfull = false;
        //do { //cerco di salvare impostazioni predefinite se il caricamento fallisce
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
        c = Filez.getFileContent("./settings/motd.txt");
        if (c == null) {
            Filez.writeFile("./settings/motd.txt", motd);
        } else {
            motd = c;
        }
        //if(a!=null&&b!=null&&c!=null)successfull=true;
        //} while(a==null||b==null||c==null); //(!successfull);
    }

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
