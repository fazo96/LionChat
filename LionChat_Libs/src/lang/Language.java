package lang;

import java.util.ArrayList;

/**
 * This class represents a language and its Sentences.
 *
 * @author fazo
 */
public class Language {

    // Separators for the parser
    public static final String sentenceSeparator = ":", comment = "#", title = "---";
    // All the sentences of this language
    private ArrayList<Sentence> sentences;
    // Language attributes
    private String id = null, name = null;

    /**
     * Initializes and loads from file the language with given ID.
     *
     * @param id the ID of the language, which also identifies the file.
     */
    public Language(String id) {
        this.id = id;
        sentences = new ArrayList<Sentence>();
        load();
    }

    /**
     * Loads language data from file. The file must be in ./lang/ and must be
     * called like the language ID. A load is attempted automatically when a new
     * language is declared.
     *
     * @return se è riuscita l'operazione o no
     */
    public boolean load() {
        String fileContent = utilz.Filez.getFileContent("./lang/" + id);
        if (fileContent == null) {
            System.out.println("[LIB][Lang] Tried to load lang \"" + id + "\" but null file");
            return false;
        }
        System.out.println("[LIB][Lang] Loading \"" + id + "\" ...");
        int i=0,se=0;
        for (String s : utilz.Utils.toList(fileContent, "\n")) {
            i++;
            if (s == null || s == "") {
                //System.out.println("[LIB][Lang line:"+i+"] Skipping empty line");
                continue;
            }
            s = s.trim();
            //System.out.println("[LIB][Lang line:"+i+" Processing Langfile String: \"" + s + "\"");
            if (s.startsWith(comment)) {
                //System.out.println("[LIB][Lang line:"+i+"] Skipping comment");
                continue;
            } else if (s.contains(comment)) {
                //System.out.println("[LIB][Lang line:"+i+"] Parsing commented line (removing comment) ...");
                s = utilz.Utils.toList(s, comment).get(0).trim();
            }
            if (s.startsWith("---") && name == null) {
                System.out.println("[LIB][Lang line:"+i+"] Assigning language name \""+(name=s.replace("---", "").trim())+"\"...");
            } else if (s.contains(sentenceSeparator)) {
                //System.out.println("[LIB][Lang line:"+i+"] Adding Sentence ...");
                sentences.add(new Sentence(s.trim())); se++;
            }
        }
        if(name==null)name=id;
        System.out.println("[LIB][Lang] Successfully loaded Language \""+id+"\": "+i+" lines, "+se+" sentences added, \""+name+"\" picked as name.");
        return true;
    }

    /**
     * Returns a sentence from this Language by searching the list using the
     * given sentence name. Sentence names are case-sensitive.
     *
     * @param name the sentence to look for
     * @return the sentence if found, else a new sentence containing an error
     * message (this is to avoid very annoying exceptions that cause crashes
     * when looking for nonexistant sentences just because a language file is
     * badly written. This way an error will be displayed in the GUI instead)
     */
    public Sentence getSentence(String name) {
        for (Sentence a : sentences) {
            if (a.getName().equals(name)) {
                return a;
            }
        }
        return new Sentence("name", "[!] ERROR IN LANGUAGE FILE \"" + id + "\": CAN'T FIND " + name + " SENTENCE");
    }

    /**
     * Returns a resume of this Language object, written in english, readable to humans.
     *
     * @return resume as string.
     */
    public String getLangInfo(boolean verbose) {
        String s = "Lang: " + id + " Name: " + name + "\nSentences: " + sentences.size();
        if (!isLoaded()) {
            return s + "\n[!] LANGUAGE IS NOT LOADED!!!";
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
     * Tells wether or not the Language looks loaded in memory without issues.
     *
     * @return true if the Language looks usable
     */
    public boolean isLoaded() {
        return !sentences.isEmpty() && name != null && id != null;
    }
}
