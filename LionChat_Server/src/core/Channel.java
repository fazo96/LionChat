package core;

import java.util.ArrayList;
import net.ClientHandler;
import net.Server;
import utilz.Filez;

/**
 * Un canale della chat utilizzabile dagli utenti.
 *
 * @author Fazo
 */
public class Channel {

    private static ArrayList<Channel> channels = new ArrayList<Channel>(); //lista canali
    private ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>(); //client connessi al canale
    private String name = "", password = ""; //Nome e password del canale
    private boolean autoDelete = true; //Indica se il canale deve essere eliminato quando è vuoto

    /**
     * Crea un nuovo canale pubblico.
     *
     * @param name il nome del nuovo canale.
     */
    public Channel(String name) {
        this.name = name;
        channels.add(this);
    }

    /**
     * Crea un nuovo canale con password (privato).
     *
     * @param name il nome del nuovo canale.
     * @param password la sua password. Immettere null per creare un canale
     * pubblico.
     */
    public Channel(String name, String password) {
        this(name);
        this.password = password;
    }

    /**
     * Aggiunge un client al canale e ne notifica i partecipanti.
     *
     * @param ch Il client da aggiungere
     */
    public void add(ClientHandler ch) {
        if (clients.contains(ch)) {
            Server.out("[!] Tentativo di aggiungere al gruppo un utente già presente!");
        }
        ch.send("Sei entrato nel canale " + name + "\n");
        send(ch.getScreenName(true) + " è entrato nel canale!\n");
        clients.add(ch);
        ch.getJoinedChannels().add(this);
    }

    /**
     * Rimuove un client dal canale e ne notifica i partecipanti.
     *
     * @param ch Il client da rimuovere dal canale.
     */
    public void remove(ClientHandler ch) {
        if (clients.contains(ch)) {
            ch.send("Sei uscito dal canale " + name + "\n");
            ClientHandler.send(ch.getScreenName(true) + " è uscito dal canale " + name + "\n", Settings.groupAdmin);
        }
        ch.getJoinedChannels().remove(this);
        clients.remove(ch);
        if (clients.isEmpty()) {
            delete();
        }
    }

    /**
     * Rimuove tutti i client dal canale.
     */
    public void clear() {
        for (ClientHandler ch : clients) {
            ch.getJoinedChannels().remove(this);
        }
        clients.clear();
    }

    /**
     * Salva il canale.
     *
     * @throws UnsupportedOperationException se il canale non ha password
     */
    public void save() {
        if (password == "") {
            throw new UnsupportedOperationException("Non si può salvare un canale senza password!");
            //return; //non si può salvare un canale senza pass
        }
        Filez.writeFile("./channels/" + name + ".dat", password);
    }

    /**
     * Cancella il canale e ne rimuove i clients connessi.
     */
    public void delete() {
        channels.remove(this);
        clear();
    }

    /**
     * invia un messaggio ai partecipanti del canale.
     *
     * @param s il messaggio da inviare.
     */
    public void send(String s) {
        for (ClientHandler c : clients) {
            c.send("[ " + name + " ]" + s);
        }
    }

    /**
     *
     * @return la lista dei canali.
     */
    public static ArrayList<Channel> getChannels() {
        return channels;
    }

    /**
     *
     * @param s il nome del canale di cui si richiede l'istanza.
     * @return l'istanza che corrisponde al nome inserito, null altrimenti.
     */
    public static Channel get(String s) {
        for (Channel g : channels) {
            if (g.getName().equals(s)) {
                return g;
            }
        }
        return null;
    }

    /**
     *
     * @return il nome del canale
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return la lista di client connessi al canale .
     */
    public ArrayList<ClientHandler> getClients() {
        return clients;
    }

    /**
     * Indica se il canale è pubblico o privato.
     *
     * @return se il canale ha una password impostata oppure no.
     */
    public boolean isPrivate() {
        if (password == null) {
            return true;
        }
        return false;
    }

    /**
     * Ritorna la password del canale.
     *
     * @return la password impostata, null se non è stata inserita e il canale è
     * pubblico.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Imposta/cambia la password del canale.
     *
     * @param password la nuova password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Rimuove il client dato da ogni canale.
     *
     * @param c il client da rimuovere
     */
    public static void removeFromAll(ClientHandler c) {
        for (Channel chan : Channel.getChannels()) {
            chan.remove(c);
        }
    }

    /**
     * Indica se il canale viene cancellato quando è vuoto.
     *
     * @return true se la condizione è vera.
     */
    public boolean isAutodelete() {
        return autoDelete;
    }

    /**
     * Imposta la cancellazione automatica del canale quando è vuoto.
     *
     * @param autodelete il nuovo valore della condizione.
     */
    public void setAutodelete(boolean autodelete) {
        this.autoDelete = autodelete;
    }
}
