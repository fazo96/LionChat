package lang;

import java.util.ArrayList;

/**
 * A Sentence , written in a Language. A Sentence has a version in every
 * language. LionChat mostly uses Sentences to communicate with users, to ensure
 * easy language portability.
 *
 * @see Language
 * @author fazo
 */
public class Sentence {

    public static final String separator = "/A/", newLine = "/N/", noNewLine = "/S/"; //Separatori universali
    private String name = "";
    private String content = "";

    /**
     * Creates a Sentence with given name and content
     *
     * @param name the Sentence name
     * @param content the Sentence content
     */
    public Sentence(String name, String content) {
        this.name = name;
        this.content = content;
    }

    /**
     * Initializes a Sentence by parsing the String given using the (badly coded) parser.
     *
     * @param rawRead
     */
    public Sentence(String rawRead) {
        String[] array = rawRead.split(Language.sentenceSeparator, 2);
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
     * Ritorna il contenuto come stringa non processata
     *
     * @return la stringa del contenuto
     */
    public String getRawContentString() {
        return content;
    }

    /**
     * Ritorna il contenuto come stringa processata
     *
     * @return la stringa del contenuto con modifiche fatte dal parser
     */
    public String getProcessedContentString() {
        return parse(content);
    }

    /**
     * Processa la stringa data
     *
     * @param s la stringa da processare
     * @return la stringa processata
     */
    private String parse(String s) {
        if (!isLoaded()) {
            return error();
        }
        if (!s.endsWith(noNewLine)) {
            return s.replace(newLine, "\n").replace(noNewLine, "") + "\n";
        }
        return s.replace(newLine, "\n").replace(noNewLine, "");
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

    /**
     * Ritorna stringa pronta per la stampa, e permette di inserire contenuto
     * aggiuntivo nella Sentence se essa è stata scritta per supportarlo
     *
     * @param additionalContent le parole aggiuntive da inserire nella frase
     * @param additionalContentSeparator il separatore che separa le parole
     * aggiuntive
     * @return la stringa pronta per la stampa
     */
    public String print(String additionalContent, String additionalContentSeparator) {
        if (!isLoaded()) {
            return error();
        }
        if (!getRawContentString().contains(separator)) {
            return getProcessedContentString();
        }
        ArrayList<String> ad = utilz.Utils.toList(additionalContent, additionalContentSeparator);
        String ret = new String(content);
        for (String s : ad) {
            ret = ret.replaceFirst(separator, s);
        }
        return parse(ret).replace(separator, "[ERR]");
    }

    /**
     * Ritorna stringa pronta per la stampa, e permette di inserire contenuto
     * aggiuntivo nella Sentence se essa è stata scritta per supportarlo. Il
     * parser utilizza uno spazio come separatore.
     *
     * @param additionalContent le parole aggiuntive da inserire nella frase
     * @return la stringa pronta per la stampa
     */
    public String print(String additionalContent) {
        return print(additionalContent, " ");
    }

    /**
     * Ritorna il contenuto pronto per la stampa.
     *
     * @return il contenuto pronto per la stampa
     */
    public String print() {
        if (!isLoaded()) {
            return error();
        }
        return getProcessedContentString().replace(separator, "[ERR]");
    }

    /**
     * Ritorna un messaggio di errore adeguato al problema della sentence
     *
     * @return stringa
     */
    private String error() {
        if (name != null) {
            return "[!] Invalid PRINT for uninitialized sentence " + name;
        } else if (content == null) {
            return "[!] Invalid PRINT for uninitialized sentence with no name";
        }
        return null;
    }
}
