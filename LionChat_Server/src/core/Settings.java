package core;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import parsing.Language;
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
    private static KeyPair keyPair;
    private static Cipher decrypter;

    /**
     * Initializes everything. Uses settings from file if they're there, or
     * defaults instead.
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
        languageID = "en";
        load(); // Try to load from file.
        // Set default groups
        // Please don't remove these assignments.
        groupGuest = new Group("guest", "help chat login who motd");
        groupUser = new Group("user", "c help chat logout who motd");
        groupAdmin = new Group("admin", "*"); // Admin group bypasses every limitation.
        // Defining the global channel
        globalChannel = new Channel("Global");
        globalChannel.setAutodelete(false);

        // SECURITY STUFF
        
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        keyPair = keyGen.genKeyPair();
        try {
            decrypter = Cipher.getInstance("RSA");
            decrypter.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        } catch (Exception ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }

        init = true;
    }

    /**
     * Save every setting.
     */
    public static void save() {
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
        } catch (Exception ex) {
            Server.out("Failed. Creating file...");
            Filez.writeFile("./settings/net.txt", "" + port);
            port = 7777;
        }
        String a, b, c;
        Server.out("Loading helpMsg");
        a = Filez.getFileContent("./settings/helpMsg.txt");
        if (a == null) {
            Server.out("Failed. Creating file...");
            Filez.writeFile("./settings/helpMsg.txt", helpMsg);
        } else {
            helpMsg = a;
        }
        Server.out("Loading adminHelpMsg");
        b = Filez.getFileContent("./settings/adminHelpMsg.txt");
        if (b == null) {
            Server.out("Failed. Creating file...");
            Filez.writeFile("./settings/adminHelpMsg.txt", adminHelpMsg);
        } else {
            adminHelpMsg = b;
        }
        Server.out("Loading adminHelpMsg");
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
        return "\n" + helpMsg + "\n";
    }

    /**
     *
     * @return the "adminHelp" message
     */
    public static String getAdminHelpMsg() {
        return "\n" + adminHelpMsg + "\n";
    }

    /**
     *
     * @return the message of the day.
     */
    public static String getMotd() {
        return "\n" + motd + "\n";
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

    public static KeyPair getKeyPair() {
        return keyPair;
    }

}
