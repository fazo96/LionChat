package lang;

import java.util.ArrayList;

/**
 * Un record di una frase scritta in una determinata lingua
 *
 * @author fazo
 */
public class Sentence {

    public static final String separator = "/*/"; //Separatore universale
    private String name = "";
    private String content = "";

    /**
     * Crea una frase con nome e contenuto dati
     *
     * @param name il nome della frase (no spazi)
     * @param content il contenuto della frase
     */
    public Sentence(String name, String content) {
        this.name = name;
        this.content = content;
    }

    /**
     * Inizializza una Sentence facendo il parsing del nome con i content.
     *
     * @param rawRead
     */
    public Sentence(String rawRead) {
        ArrayList<String> ss = utilz.Utils.toList(rawRead, Lang.separator);
        if (ss.size() != 2) {
            return;
        }
        name = ss.get(0);
        content = ss.get(1);
    }

    public String getName() {
        return name;
    }

    /**
     * Ritorna il contenuto come stringa
     *
     * @return la stringa del contenuto
     */
    public String getContentString() {
        return content;
    }

    /**
     * Ritorna il contenuto diviso in parti in modo da poter inserire facilmente
     * altre informazioni.
     *
     * @return il contenuto sottoforma di array dinamico di stringhe
     */
    public ArrayList<String> getContent() {
        return utilz.Utils.toList(content, separator);
    }
/**
     * Funzione che comunica se la frase è caricata correttamente
     *
     * @return true se la frase è utilizzabile
     */
    public boolean isLoaded() {
        return name != null && content != null;
    }
}
