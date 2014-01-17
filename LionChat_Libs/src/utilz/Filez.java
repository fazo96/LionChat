package utilz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Questa classe gestisce la lettura e scrittura su file.
 *
 * @author fazo
 */
public class Filez {

    /**
     * Crea un file al percorso dato.
     *
     * @param s il percorso del file da creare (nome compreso).
     * @return false se l'operazione è fallita, true altrimenti.
     */
    public static boolean makeFile(String s) {
        File file = new File(s);
        try {
            File parent = file.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                System.out.println("[LIB] impossibile creare file " + s + "\n");
                return false;
                //throw new IllegalStateException("Couldn't create: " + parent);
            }
        } catch (Exception ex) {
            System.out.println("[LIB] impossibile creare file " + s + "\n");
            return false;
        }
        try {
            file.createNewFile();
        } catch (IOException ex) {
            System.out.println("[LIB] impossibile creare file " + s + "\n");
            Logger.getLogger(Filez.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    /**
     * Scrive su un file sostituendone il contenuto con quello dato, Se il file
     * non esiste lo crea usando makeFile
     *
     * @param filepath il percorso del file (nome compreso) su cui scrivere.
     * @param content il nuovo contenuto del file in formato stringa.
     * @return false se l'operazione è fallita, true altrimenti.
     */
    public static boolean writeFile(String filepath, String content) {
        System.out.println("[LIB] starting write process...");
        try {
            System.out.println("[LIB] write file " + filepath);
            File file = new File(filepath);
            if (!file.exists()) {
                makeFile(filepath);
            }
            FileWriter fw = null;
            try {
                System.out.println("[LIB] Init filewriter " + filepath);
                fw = new FileWriter(file);
            } catch (IOException ex) {
                Logger.getLogger(Filez.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            try {
                System.out.println("[LIB] Write content " + filepath);
                fw.write(content);
            } catch (IOException ex) {
                Logger.getLogger(Filez.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            try {
                System.out.println("[LIB] Close file " + filepath);
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(Filez.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("[LIB] Finish");
        } catch (Throwable t) {
            Logger.getLogger(Filez.class.getName()).log(Level.SEVERE, null, t);
        }
        return true;
    }

    /**
     * Ritorna il contenuto di un file in formato stringa.
     *
     * @param s il percorso (nome compreso) del file da leggere.
     * @return il contenuto del file letto, null se la lettura è fallita.
     */
    public static String getFileContent(String s) { //ritorna il contenuto di un file sottoforma di stringa
        System.out.println("[LIB] Inizio lettura...");
        FileReader fr = null;
        try {
            fr = new FileReader(s);
        } catch (FileNotFoundException ex) {
            System.out.println("[LIB] File non trovato!");
            /*content.add("notfound");
             return content;*/
            return null;
        }
        BufferedReader br = new BufferedReader(fr);
        String z = "", a = "";
        int i = 1;
        while (true) {
            //System.out.println("[LIB] Leggo riga " + i + "...");
            try {
                z = br.readLine();
                if (z != null) {
                    if (i != 1) {
                        a += "\n";
                    }
                    a += z;
                    i++;
                } else {
                    System.out.println("[LIB] Vuoto! Rompo lettura");
                    break;
                } //se viene letta una riga nulla smette di leggere
            } catch (IOException ex) {
                //System.out.println("[LIB] Eccezione! Rompo lettura...");
                break;
                //Logger.getLogger(Filez.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            //System.out.println("[LIB] Chiudo!");
            fr.close();
        } catch (IOException ex) {
            System.out.println("[LIB] Errore in chiusura!");
            //Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*System.out.println("[LIB] Stampo lettura!");
        System.out.println(a);
        System.out.println("[LIB] Fine stampa!");*/
        return a;
    }
}
