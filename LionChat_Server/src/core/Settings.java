package core;

import lang.Language;
import net.Server;
import utilz.Filez;

/**
 * Handles server settings and common variables.
 *
 * @author Fazo
 */
public class Settings {

    private static int port;
    private static String helpMsg, adminHelpMsg, motd, languageID;
    public static Language language;
    public static Group groupGuest, groupUser, groupAdmin;
    public static Channel globalChannel;
    private static boolean init = false;

    /**
     * Initializes everything. Uses settings from file if they're there, or defaults instead.
     */
    public static void init() {
        if (init) {
            return; // don't init if already init!
        }
        // Load default values
        port = 7777;
        helpMsg = "HELP";
        adminHelpMsg = "ADMINHELP";
        motd = "MOTD";
        languageID="en";
        load(); // Try to load from file.
        // Set default groups
        // Please don't remove these assignments.
        groupGuest = new Group("guest", "help chat login who motd");
        groupUser = new Group("user", "c help chat logout who motd");
        groupAdmin = new Group("admin", "*"); // Admin group bypasses every limitation.
        // Defining the global channel
        globalChannel = new Channel("Global");
        globalChannel.setAutodelete(false);
        init = true;
    }

    /**
     * Save every setting.
     */
    public static void save() { //salvataggio dati.
        Filez.writeFile("./settings/net.txt", "" + port);
        Filez.writeFile("./settings/helpMsg.txt", helpMsg);
        Filez.writeFile("./settings/adminHelpMsg.txt", adminHelpMsg);
        Filez.writeFile("./settings/motd.txt", motd);
    }

    /**
     * Load settings from file.
     */
    public static void load() {
        try {
            Server.out("Loading network settings");
            port = Integer.parseInt(Filez.getFileContent("./settings/net.txt"));
            if (port <= 0 || port > 65535) {
                port = 7777;
            }
        } catch (Exception ex) { //Se qualcosa va male vado sul sicuro e ricreo/riscrivo il file.
            Server.out("Failed. Creating file...");
            Filez.writeFile("./settings/net.txt", "" + port);
            port = 7777;
        }
        String a, b, c;
        //Inizio il tentativo di caricare da file.
        Server.out("Caricamnto di helpMsg");
        a = Filez.getFileContent("./settings/helpMsg.txt");
        if (a == null) {
            Server.out("Failed. Creating file...");
            Filez.writeFile("./settings/helpMsg.txt", helpMsg);
        } else {
            helpMsg = a;
        }
        Server.out("Caricamnto di adminHelpMsg");
        b = Filez.getFileContent("./settings/adminHelpMsg.txt");
        if (b == null) {
            Server.out("Failed. Creating file...");
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
        language = new Language(languageID);
    }

    /**
     *
     * @return the "help" message
     */
    public static String getHelpMsg() {
        return helpMsg;
    }

    /**
     *
     * @return the "adminHelp" message
     */
    public static String getAdminHelpMsg() {
        return adminHelpMsg;
    }

    /**
     *
     * @return the message of the day.
     */
    public static String getMotd() {
        return motd;
    }

    /**
     *
     * @return wether the settings are all set.
     */
    public static boolean isInit() {
        return init;
    }

    /**
     *
     * @return the port on which the server is operating.
     */
    public static int getPort() {
        return port;
    }
}
