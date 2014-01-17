/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lang;

import java.util.ArrayList;

/**
 * Un oggetto che descrive e contiene le informazioni di una lingua disponibile
 * nel programma.
 *
 * @author fazo
 */
public class Lang {

    public static final String sentenceSeparator = ":", comment = "#", title = "---"; //Separatore universale
    private ArrayList<Sentence> sentences;
    private String id = null, name = null;

    /**
     * Inizializza e carica in memoria la lingua con l'id dato.
     *
     * @param id l'identificativo della lingua e nome del file che la contiene.
     */
    public Lang(String id) {
        this.id = id;
        sentences = new ArrayList<Sentence>();
        load();
    }

    /**
     * Carica da file le informazioni della lingua. Il file deve chiamarsi con
     * l'id della lingua, senza estensione. Viene tentato il caricamento
     * automaticamente al momento dell'inizializzazione della lingua
     *
     * @return se è riuscita l'operazione o no
     */
    public boolean load() {
        String fileContent = utilz.Filez.getFileContent("./lang/" + id);
        if (fileContent == null) {
            System.out.println("[LIB] Tried to load lang \""+id+"\" but null file");
            return false;
        }
        System.out.println("[LIB] Loading \""+id+"\" ...");
        for (String s : utilz.Utils.toList(fileContent, "\n")) {
            if (s == null || s == "") {
                System.out.println("[LIB][Lang] Skipping an empty line");
                continue;
            }
            s = s.trim(); System.out.println("[LIB] Processing Langfile String: \""+s+"\"");
            if (s.startsWith(comment)) {
                System.out.println("[LIB][Lang] Skipping comment line");
                continue;
            } else if (s.contains(comment)) {
                System.out.println("[LIB][Lang] Parsing commented line (removing comment) ...");
                s = utilz.Utils.toList(s, comment).get(0).trim();
            }
            if (s.startsWith("---") && name == null) {
                System.out.println("[LIB][Lang] Assigning name...");
                name = s.replace("---", "").trim();
            } else if (s.contains(sentenceSeparator)) {
                System.out.println("[LIB][Lang] Reading sentence...");
                sentences.add(new Sentence(s.trim()));
            }
        }
        return true;
    }

    /**
     * Ritorna l'istanza di una Sentence ricercandola tramite il suo nome
     *
     * @param name il nome da cercare
     * @return la Sentence corrispondente se esiste, altrimenti null
     */
    public Sentence get(String name) {
        for (Sentence a : sentences) {
            if (a.getName().equals(name)) {
                return a;
            }
        }
        return null;
    }

    /**
     * Ritorna info sulla lingua, scritte in inglese, leggibili da essere umano.
     *
     * @return una stringa con un resoconto di questa lang.
     */
    public String getLangInfo(boolean verbose) {
        String s = "Lang: " + id + " Name: " + name + "\nSentences: " + sentences.size();
        if (!isLoaded()) {
            return s+"\n[!] LANG NOT LOADED!!!";
        }
        if (!verbose) {
            return s;
        }
        if (sentences.size() > 0) {
            s += "\n";
            for (Sentence se : sentences) {
                s += se.getSentenceInfo() + "\n";
            }
        }
        return s;
    }

    /**
     * Funzione che comunica se la lingua è caricata correttamente
     *
     * @return true se la lang è utilizzabile
     */
    public boolean isLoaded() {
        return !sentences.isEmpty() && name != null && id != null;
    }
}
