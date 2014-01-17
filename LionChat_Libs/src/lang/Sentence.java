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
        String[] array = rawRead.split(Lang.sentenceSeparator, 2);
        if (array.length != 2) {
            return;
        }
        name = array[0].trim();
        content = array[1].trim();
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

    /**
     * Ritorna info sulla Sentence, scritte in inglese, leggibili da essere
     * umano.
     *
     * @return una stringa con un resoconto di questa Sentence.
     */
    public String getSentenceInfo() {
        if (!isLoaded()) {
            return "[!] SENTENCE NOT LOADED";
        }
        return "Sentence Name: " + name + "\n--- Content ---\n" + content.replace(separator, "[ITEM]");
    }

    public String print(String a, String regex) {
        if (!isLoaded()) {
            return "[!] Invalid PRINT for uninitialized sentence";
        }
        ArrayList<String> ss = utilz.Utils.toList(a, regex);
        ArrayList<String> cont = getContent();
        if (ss.size() != cont.size() - 1) {
            return "[!] Invalid PRINT for sentence " + name;
        }
        String ret = "";
        for (int i = 0; i < ss.size(); i++) {
            if (content.startsWith(separator)) {
                ret += ss.get(i) + cont.get(i);
            } else {
                ret += cont.get(i) + ss.get(i);
            }
        }
        return null;
    }

    public void print(String a) {
        print(a, " ");
    }
}
