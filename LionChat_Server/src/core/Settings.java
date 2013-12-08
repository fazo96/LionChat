package core;

import net.Server;
import utilz.Filez;

/**
 * Questa classe gestisce le impostazioni generali del software.
 *
 * @author Fazo
 */
public class Settings {

    private static int port;
    private static String helpMsg, adminHelpMsg, motd;
    public static Group groupGuest, groupUser, groupAdmin;
    public static Channel globalChannel;
    private static boolean init = false;

    /**
     * Carica tutte le variabili: tenta di caricare da file, se non riesce
     * imposta i valori di default.
     */
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
        //ATTENZIONE!!!!!!! QUESTI TRE GRUPPI SONO OBBLIGATORI E IL PROGRAMMA POTREBBE NON COMPILARE SE UNO DI QUESTI VIENE RIMOSSO
        groupGuest = new Group("guest", "help chat login who motd");
        groupUser = new Group("user", "c help chat logout who motd");
        groupAdmin = new Group("admin", "*"); //il gruppo admin bypassa ogni controllo dei permessi, quindi è inutile impostarli.
        //INIZIALIZZO CANALE GLOBAL
        globalChannel = new Channel("Global");
        globalChannel.setAutodelete(false);
        init = true;
    }

    /**
     * Salva su file tutte le impostazioni.
     */
    public static void save() { //salvataggio dati.
        Filez.writeFile("./settings/net.txt", "" + port);
        Filez.writeFile("./settings/helpMsg.txt", helpMsg);
        Filez.writeFile("./settings/adminHelpMsg.txt", adminHelpMsg);
        Filez.writeFile("./settings/motd.txt", motd);
    }

    /**
     * Carica da file tutte le impostazioni.
     */
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

    /**
     *
     * @return il messaggio visualizzato quando l'utente richiama la pagina di
     * aiuto.
     */
    public static String getHelpMsg() {
        return helpMsg;
    }

    /**
     *
     * @return il messaggio visualizzato quando l'admin richiama la pagina di
     * aiuto per amministratori.
     */
    public static String getAdminHelpMsg() {
        return adminHelpMsg;
    }

    /**
     *
     * @return il messaggio del giorno.
     */
    public static String getMotd() {
        return motd;
    }

    /**
     *
     * @return se il programma è correttamente inizializzato.
     */
    public static boolean isInit() {
        return init;
    }

    /**
     *
     * @return la porta di rete su cui opera il server.
     */
    public static int getPort() {
        return port;
    }
}
