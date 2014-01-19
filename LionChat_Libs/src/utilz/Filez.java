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
 * This class offers file handling functions tuned to work with LionChat. It has
 * been created to avoid code duplication.
 *
 * @author fazo
 */
public class Filez {

    /**
     * Creates a file in the relative path given. It should work with absolute
     * paths, however LionChat never uses them.
     *
     * @param s filepath ending with filename. Example:
     * C:\\somefolder\\somefile.someextension for windows and
     * ./somefolder/somefile.someextension for linux or /somedirectory/somefile
     * if you want to try an absolute path.
     * @return true if the file creation process has been successfull
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
     * Writes a file at the given relative path, replacing its original content
     * with the given string. If the file doesn't exist it is automatically created.
     *
     * @param filepath the file path containing the filename. It hasn't been tested with absolute paths.
     * @param content the content to write into the file.
     * @return true if the operation has been successfull.
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
     * Returns a string representing the content of the file at the given filepath.
     *
     * @param filepath the file's filepath with file name and extension included.
     * @return the content of the file as a string. Returns a null String if the operation failed for some reason.
     */
    public static String getFileContent(String filepath) { //ritorna il contenuto di un file sottoforma di stringa
        System.out.println("[LIB] Accessing file at "+filepath+" ...");
        FileReader fr;
        try {
            fr = new FileReader(filepath);
        } catch (FileNotFoundException ex) {
            System.out.println("[!][LIB] File not found! Reading aborted");
            /*content.add("notfound");
             return content;*/
            return null;
        }
        BufferedReader br = new BufferedReader(fr);
        String z = "", a = "";
        int i = 1;
        while (true) {
            //System.out.println("[LIB] Reading line number " + i + "...");
            try {
                z = br.readLine();
                if (z != null) {
                    if (i != 1) {
                        a += "\n";
                    }
                    a += z;
                    i++;
                } else {
                    System.out.println("[LIB] Read stopped: end of file.");
                    break;
                } //se viene letta una riga nulla smette di leggere
            } catch (IOException ex) {
                System.out.println("[LIB] Read stopped: IO exception.");
                break;
            }
        }
        System.out.println("[LIB] "+i+" lines have been read successfully!");
        try {
            fr.close();
        } catch (IOException ex) {
            System.out.println("[!][LIB] Could not close file!");
        }
        System.out.println("[LIB] File closed successfully.");
        return a;
    }
}
