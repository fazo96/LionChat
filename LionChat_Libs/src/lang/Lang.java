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

    public static final String separator = ":::"; //Separatore universale
    private ArrayList<Sentence> sentences;
    private String id, name;

    /**
     * Inizializza e carica in memoria la lingua con l'id dato.
     *
     * @param id l'identificativo della lingua e nome del file che la contiene.
     */
    public Lang(String id) {
        this.id = id;
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
            return false;
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
    public String getLangInfo() {
        if (!isLoaded()) {
            return "[!] LANG NOT LOADED!!!";
        }
        return "Lang: " + id + " Name: " + id + "\nSentences: " + sentences.size();
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
