/*
 LionChat
 Copyright (C) 2014 Enrico Fasoli ( fazius2009 at gmail dot com )

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package UI;

import java.util.ArrayList;
import net.Connection;
import parsing.Language;
import utilz.Filez;
import utilz.Out;
import utilz.Utils;

/**
 *
 * @author fazo
 */
public class Client {

    private Out out;
    private boolean useGUI = true;
    private GUI gui;
    private static Client instance;
    private Connection connection;
    private String ip = "localhost", languageID = "en";
    private int port = 7777;
    private Language language = null;

    public static void main(String args[]) {
        // Start the Client
        instance = new Client(true);
        // Load language
        instance.loadLanguage(instance.languageID);
        // Load settings
        instance.loadSettings();
        // Start the connection
        instance.startConnection();
    }

    public Client(boolean useGUI) {
        this.useGUI = useGUI;
        out = new Out();
        out.info("Started Client with " + (useGUI ? "GUI enabled" : "GUI disabled") + ".");
        if (useGUI) {
            // Set the Nimbus look and feel
            //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
             * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
             */
            try {
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (ClassNotFoundException ex) {
                java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            } catch (javax.swing.UnsupportedLookAndFeelException ex) {
                java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            //</editor-fold>
            gui = new GUI();
            // Tell the gui to read the output messages from our Out object
            gui.setOutputHandler(out);
        }
    }

    /**
     * Read settings from the file, autoconfiguring it if it's missing.
     */
    public void loadSettings() {
        ArrayList<String> cnt = null;
        //read the file and read the content
        out.info(language.getSentence("tryReadSettings").print());
        for (int i = 0; i < 2; i++) {
            out.info(language.getSentence("tryNumber").print("" + (i + 1)));
            cnt = Utils.toList(Filez.getFileContent("settings.txt"), " ");
            if (cnt == null) { // If the list is null, it means the read failed
                out.info(language.getSentence("settingsNotFound").print());
                Filez.writeFile("settings.txt", ip + " " + port + " " + languageID);
                continue; //Nothing to do anymore
            }
            out.info(language.getSentence("readSuccessfull").print());
            break;
        }
        if (cnt == null) {
            out.info(language.getSentence("settingsReadFailed").print());
        } else if (cnt.size() != 3) { //There must be 3 elements for the file to be valid
            out.info(language.getSentence("settingsWrongParamNumber").print(cnt.size() + ""));
            // Rewrite the settings
            Filez.writeFile("settings.txt", ip + " " + port + " " + languageID);
            return; //nothing to do anymore.
        }
        // Finally assign the parameters
        ip = cnt.get(0);
        try {
            port = Integer.parseInt(cnt.get(1));
        } catch (Exception ex) { // Port number is not valid!
            port = 7777;
        }
        String oldLang = languageID;
        languageID = cnt.get(2);
        if (!oldLang.equals(languageID)) {
            loadLanguage(languageID);
        }
    }

    /**
     * Loads up a language file and applies it automatically
     *
     * @param lang the string identifying the name of the file
     * @return if the loading of given language fails, the method outputs an
     * error message and loads english and returns true. If loading english also
     * fails, it returns false
     */
    private boolean loadLanguage(String lang) {
        out.info("Loading language \"" + lang + "\"\n");
        language = new Language(lang);
        if (!language.isLoaded()) {
            if (lang.equals("en")) {
                return false;
            }
            out.info("[!] Could not load language \"" + lang + "\", trying english instead\n");
            // If the language given can't be loaded, just load english instead
            return loadLanguage("en");
        }
        //out.info(language.getLangInfo(true));
        if(useGUI){
            gui.applyLanguage(language);
        }
        return true;
    }

    public void startConnection() {
        connection = new Connection(ip, port);
    }

    public String getIP() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public Out getOutputHandler() {
        return out;
    }

    public boolean isUseGUI() {
        return useGUI;
    }

    public GUI getGUI() {
        return gui;
    }

    public Connection getConnection() {
        return connection;
    }

    public static Client get() {
        return instance;
    }

    public Out out() {
        return out;
    }

    public Language getLanguage() {
        return language;
    }

}
