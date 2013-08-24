/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.ClientHandler;
import net.Server;
import utilz.Filez;
import utilz.Utils;

/**
 *
 * @author fazo
 */
public class Cmd {

    public static void cmd(String s, ClientHandler c) {
        if (!Settings.isInit()) {
            Settings.init(); //se non è inizializzato, inizializzo adesso
        }
        if (!Utils.isValid(s)) { //controllo validità stringa
            Server.out("Ricevuta stringa non valida, salto esecuzione.");
            if (c != null) {
                c.send("Stringa non valida.\n");
            }
            return;
        }
        if (!s.startsWith("/")) { //se il messaggio ricevuto non inizia con "/" allora non è un comando
            if (c == null) {
                ClientHandler.sendToAll("[ SERVER ]: " + s + "\n");
                return;
            }
            ClientHandler.sendToAll("[ " + c.getScreenName(false) + " ]: " + s + "\n");
            return;
        }
        String cmd[] = s.split(" "); //divido il messaggio ricevuto a ogni spazio.
        int l = cmd.length;
        /*Server.out("INIZIO COMANDO"); //debug che stampa le parti del comando.
         for(int i=0;i<l;i++)
         Server.out(cmd[i]);
         Server.out("FINE COMANDO");*/
        if (l <= 0) {
            c.send("Errore: Comando vuoto\n");
            return; //comando vuoto, inutile continuare
        }

        if (c != null) {  //COMANDI PER UTENTI NON LOGGATI
            if (cmd[0].equalsIgnoreCase("/login")) { //comando per il login
                if (c.getGroup() == Settings.groupUser) {
                    c.send("Già loggato!\n");
                    return;
                } //non fa niente se client è già loggato
                if (l != 3) { //errore nei parametri.
                    c.send("Utilizzo: /login nome password\n");
                    return;
                }
                c.login(cmd[1], cmd[2]);
                return;
            }
        }
        if (c != null && c.getGroup() == Settings.groupUser) {
            //Comandi esclusivi per un utente loggato, non per il server
            if (cmd[0].equalsIgnoreCase("/admin")) {
                if (c.getGroup() == Settings.groupAdmin) {
                    c.send("Sei amministratore.\nUsa /adminhelp per i comandi admin\n");
                } else {
                    c.send("Non sei un amministratore!\n");
                }
                return;
            }
            if (cmd[0].equalsIgnoreCase("/help")) {
                c.send(Settings.getHelpMsg());
                return;
            }
            if (cmd[0].equalsIgnoreCase("/logout")) { //comando per il logout
                c.logout();
                return;
            }
            if (cmd[0].equalsIgnoreCase("/password")) {
                if (l != 3) {
                    c.send("Utilizzo: /password vecchiapass nuovapass\n");
                    return;
                }
                if (cmd[1] != c.getPassword()) {
                    c.send("Password errata.\n");
                    return;
                }
                c.setPassword(cmd[2]);
                return;
            }
        }
        if (c == null || c.getGroup() == Settings.groupUser) {
            //Comandi per server o per utenti loggati
            if (cmd[0].equalsIgnoreCase("/chi")) { //Stampa lista utenti
                String list = "";
                int i = 0;
                if (ClientHandler.getClients().isEmpty()) {
                    if (c == null) {
                        Server.out("Nessuno connesso");
                    } else {
                        c.send("Nessuno connesso\n");
                    }
                    return;
                }
                for (ClientHandler ch : ClientHandler.getClients()) {
                    i++;
                    if (c == null || c.getGroup() == Settings.groupAdmin) {
                        list += i + " - " + ch.getScreenName(true) + "\n";
                    } else {
                        list += i + " - " + ch.getScreenName(false) + "\n";
                    }
                }
                if (c == null) {
                    System.out.print(list);
                } else {
                    c.send(list);
                }
                return;
            }
        }
        if (c == null || c.getGroup() == Settings.groupAdmin) {
            //questi sono i comandi esclusivi da amministratore, possono essere 
            //eseguiti da un client admin o dalla console del server (null)
            if (cmd[0].equalsIgnoreCase("/adminhelp")) {
                if (c == null) {
                    System.out.print(Settings.getAdminHelpMsg());
                } else {
                    c.send(Settings.getAdminHelpMsg());
                }
                return;
            }
            if (cmd[0].equalsIgnoreCase("/gc")) {
                if (c == null) {
                    Server.out("Chiamata al garbage collector...");
                } else {
                    c.send("Chiamata al garbage collector...\n");
                }
                Runtime.getRuntime().gc();
                if (c == null) {
                    Server.out("Chiamata completata.");
                } else {
                    c.send("Chiamata completata.\n");
                }
                return;
            }
            if (cmd[0].equalsIgnoreCase("/setGroup")) {
                if (l != 3 && l != 2) {
                    if (c == null) {
                        Server.out("Uso: /setGroup nome gruppo");
                    } else {
                        c.send("Uso: /setGroup nome gruppo\n");
                    }
                    return;
                }
                ClientHandler ch;
                if ((ch = ClientHandler.get(cmd[1])) == null) {
                    if (c == null) {
                        Server.out("Utente non esiste!");
                    } else {
                        c.send("Utente non esiste!\n");
                    }
                    return;
                }
                if (l == 2) {
                    if (c == null) {
                        Server.out("L'utente fa parte del gruppo " + ch.getGroup().getName());
                    } else {
                        c.send("L'utente fa parte del gruppo " + ch.getGroup().getName() + "\n");
                    }
                    return;
                }
                Group g;
                if ((g = Group.get(cmd[2])) == null) {
                    if (c == null) {
                        Server.out("Gruppo non esiste!");
                    } else {
                        c.send("Gruppo non esiste!\n");
                    }
                } else if (g == Settings.groupGuest) {
                    ch.logout();
                } else {
                    ch.setGroup(g);
                }
                return;
            }
            /*if (cmd[0].equalsIgnoreCase("/setAdmin")) {
             if (l != 2) {
             if (c == null) {
             Server.out("Uso: /setAdmin nome");
             } else {
             c.send("Uso: /setAdmin nome\n");
             }
             return;
             }
             ClientHandler ch;
             if ((ch = ClientHandler.get(cmd[1])) == null) {
             if (c == null) {
             Server.out("Utente non esiste!");
             } else {
             c.send("Utente non esiste!\n");
             }
             return;
             }
             ch.setGroup(Settings.groupAdmin);
             //ch.setAdmin(true);
             return;
             }
             if (cmd[0].equalsIgnoreCase("/unsetAdmin")) {
             if (l != 2) {
             if (c == null) {
             Server.out("Uso: /unsetAdmin nome");
             } else {
             c.send("Uso: /unsetAdmin nome\n");
             }
             return;
             }
             ClientHandler ch;
             if ((ch = ClientHandler.get(cmd[1])) == null) {
             if (c == null) {
             Server.out("Utente non esiste!");
             } else {
             c.send("Utente non esiste!\n");
             }
             return;
             }
             ch.setGroup(Settings.groupUser);
             return;
             }*/
            if (cmd[0].equalsIgnoreCase("/status")) { //STATO DEL SERVER
                Settings.status = "Uptime: " + (Utils.nanoToSec(System.nanoTime()) - Server.getStartTime())
                        + "s\nMemoria Usata: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 100000) + "MB"
                        + " / " + (Runtime.getRuntime().totalMemory() / 100000) + "MB"
                        + "\nMemoria Massima: " + (Runtime.getRuntime().maxMemory() / 100000) + "MB"
                        + "\nThread in esecuzione: " + Thread.activeCount()
                        + "\nCPU Cores disponibili: " + Runtime.getRuntime().availableProcessors();
                if (c == null) {
                    Server.out(Settings.status);
                } else {
                    c.send(Settings.status+"\n");
                }
                return;
            }
            if (cmd[0].equalsIgnoreCase("/stop")) {
                if (c == null) {
                    Server.out("SERVER IN ARRESTO PER COMANDO SERVER");
                } else {
                    c.send("SERVER IN ARRESTO PER COMANDO TUO\n");
                    if (c.getGroup() == Settings.groupUser) {
                        Server.out("SERVER IN ARRESTO PER COMANDO DELL'UTENTE" + c.getScreenName(true));
                        ClientHandler.send("SERVER IN ARRESTO PER COMANDO DELL'UTENTE " + c.getScreenName(true) + "\n", Settings.groupAdmin);
                    } else {
                        Server.out("SERVER IN ARRESTO PER COMANDO DELL'UTENTE" + c.getScreenName(true));
                        ClientHandler.send("SERVER IN ARRESTO PER COMANDO DELL'UTENTE " + c.getScreenName(true) + "\n", Settings.groupAdmin);
                    }
                }
                Server.stop();
                return;
            }

            if (cmd[0].equalsIgnoreCase("/kick")) {
                if (l == 0) {
                    if (c != null) {
                        c.send(c.getScreenName(true) + " Disconnette tutti!\n", Settings.groupAdmin);
                        if (c.getGroup() == Settings.groupUser) {
                            c.sendToAll(c.getScreenName(true) + " Disconnette tutti!\n");
                        } else {
                            c.sendToAll(c.getScreenName(true) + " Disconnette tutti!\n");
                        }
                    } else {
                        c.send("Disconnetto tutti!\n");
                    }
                    ClientHandler.disconnectAll();
                    return;
                } else {
                    ClientHandler ch;
                    if ((ch = ClientHandler.get(cmd[1])) != null) {
                        if (c != null) {
                            //c.send("Disconnetto tutti!\n");
                            c.send(c.getScreenName(true) + " disconnette " + ch.getScreenName(true) + "\n", Settings.groupAdmin);
                            if (c.getGroup() == Settings.groupUser) {
                                Server.out(c.getScreenName(true) + " disconnette tutti!\n");
                            } else {
                                Server.out(c.getScreenName(true) + " disconnette tutti!\n");
                            }
                        }
                        ch.disconnect();
                        return;
                    } else if (c == null) {
                        Server.out("Utente non esiste.");

                    } else {
                        c.send("Utente non esiste\n");
                    }
                }
                return;
            }
            if (cmd[0].equalsIgnoreCase("/save")) {
                if (c != null) {
                    c.send("Salvo dati server...\n");
                }
                Server.save();
                if (c != null) {
                    c.send("Dati server salvati!\n");
                }
                /*if (l == 1) { //questo codice salva tutti gli utenti o un solo utente. Deprecato
                 ClientHandler.saveAll();
                 } else {
                 ClientHandler ch;
                 if ((ch = ClientHandler.get(cmd[1])) != null) {
                 ch.save();
                 } else if (c == null) {
                 Server.out("Utente non esiste.");
                 } else {
                 c.send("Utente non esiste\n");
                 }
                 }*/
                return;
            }
        }

        if (c == null) {
            Server.out("Comando sconosciuto.");
        } else {
            c.send("Comando sconosciuto.\n");
        }
    }

    private String getAlias(String cmd) {
        return cmd;
    }
}
