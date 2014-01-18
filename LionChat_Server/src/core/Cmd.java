package core;

import net.ClientHandler;
import net.Server;
import utilz.Utils;

/**
 * Questa classe si occupa di analizzare ed eseguire le stringe inviate da clients e dal server, non è necessario istanziarla.
 * @author fazo
 */
public class Cmd {
    
    /**
     * Analizza una stringa e invia il messaggio o esegue il comando che la stringa contiene, effettuando prima tutti i controlli necessari.
     * @param s il comando/messaggio da analizzare ed eseguire.
     * @param c il client che ha invocato il comando, null se è stato invocato dal server.
     */
    public static void cmd(String s, ClientHandler c) {
        s=s.trim(); //rimuovo spazi bianchi all'inizio e alla fine della stringa
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
                Server.out("[ Server ]: " + s);
                Settings.globalChannel.send("[ Server ]: " + s + "\n");
                return;
            }
            if (!c.getGroup().can("chat")) {
                c.send("[!] Non hai il permesso di utilizzare la chat.");
            } else {
                Server.out("["+c.getWritingChannel().getName()+"][ " + c.getScreenName(true) + " ]: " + s);
                c.getWritingChannel().send("[ " + c.getScreenName(false) + " ]: " + s+"\n");
                /*ClientHandler.send("[ " + c.getScreenName(false) + " ]: " + s + "\n", Settings.groupGuest, Settings.groupUser);
                 ClientHandler.send("[ " + c.getScreenName(true) + " ]: " + s + "\n", Settings.groupAdmin);*/
            }
            return;
        }
        String cmd[] = s.split(" "); //divido il messaggio ricevuto a ogni spazio.
        int l = cmd.length;
        if (l <= 0) {
            c.send("Errore: Comando vuoto\n");
            return; //comando vuoto, inutile continuare
        }

        //INIZIO COMANDI

        if (c != null && c.getGroup().can("login") && cmd[0].equalsIgnoreCase("/login")) { //comando per il login
            if (l != 3) { //errore nei parametri.
                c.send("Utilizzo: /login nome password\n");
                return;
            }
            c.login(cmd[1], cmd[2]);
            return;
        }
        if (cmd[0].equalsIgnoreCase("/account")) {
            if (c == null) {
                Server.out("Stai usando la console del server!\n");
            } else {
                c.send("Sei riconosciuto come " + c.getScreenName(true) + "\nFai parte del gruppo " + c.getGroup().getName() + "\nIl tuo IP è " + c.getIP() + "\n");
            }
            return;
        }
        if (c != null && c.getGroup().can("help") && cmd[0].equalsIgnoreCase("/help")) { //mostra il messaggio di help
            c.send(Settings.getHelpMsg());
            return;
        }
        if (c != null && c.getGroup().can("logout") && cmd[0].equalsIgnoreCase("/logout")) { //comando per il logout
            c.logout();
            return;
        }
        if (c != null && c.getGroup().can("logout") && cmd[0].equalsIgnoreCase("/password")) { //cambia la password
            if (l != 3) {
                c.send("Utilizzo: /password vecchiapass nuovapass\n");
                return;
            }
            if (!cmd[1].equals(c.getPassword())) {
                c.send("Password errata.\n");
                return;
            }
            c.setPassword(cmd[2]);
            c.send("Password cambiata con successo.\n");
            c.save();
            return;
        }
        //if (c == null || c.getGroup() == Settings.groupUser || c.getGroup() == Settings.groupAdmin) {
        //Comandi per server o per utenti loggati
        if ((c == null || c.getGroup().can("who")) && cmd[0].equalsIgnoreCase("/chi")) { //Stampa lista utenti
            if (ClientHandler.getClients().isEmpty()) {
                if (c == null) {
                    Server.out("Nessuno connesso");
                } else {
                    c.send("Nessuno connesso\n");
                }
                return;
            }
            String list;
            if (c == null || c.getGroup() == Settings.groupAdmin) {
                list = ClientHandler.getClientList(true);
            } else {
                list = ClientHandler.getClientList(false);
            }
            if (c == null) {
                Server.out(list);
            } else {
                c.send(list);
            }
            return;
        }
        if (c != null && c.getGroup().can("c") && cmd[0].equals("/c")) { //comandi canale.
            if (cmd.length == 1) {
                c.send("Uso:\n/c list");
                return;
            }
            if (cmd[1].equalsIgnoreCase("list")) {
                String tosend = "Sei nei seguenti canali:";
                for (Channel ch : c.getJoinedChannels()) {
                    tosend += "\n[ " + ch.getName() + " ]";
                }
                c.send(tosend);
                return;
            }
            Channel chan;
            if ((cmd.length == 3 || cmd.length == 4) && cmd[1].equalsIgnoreCase("join")) {
                chan = Channel.get(cmd[2]);

            }
            if (cmd[1].equalsIgnoreCase("leave")) {
                if (cmd.length == 2) {
                    if (c.getWritingChannel() == Settings.globalChannel) {
                        c.send("Non puoi uscire dal canale global");
                        return;
                    }
                    c.getWritingChannel().remove(c);
                    c.setWritingChannel(Settings.globalChannel);
                    return;
                }
            }

            chan = Channel.get(cmd[1]);
            if (chan == null) {
                c.send("L'argomento del comando è errato o il canale non esiste.\n");
                return;
            }
            if (chan.getPassword() != null && (cmd.length == 2 || cmd[2].equals(chan.getPassword()))) {
                c.send("Password del canale errata!\n");
                return;
            }
            c.setWritingChannel(chan);
            c.send("Ora stai scrivendo sul canale " + chan.getName() + "\n");
        }
        //if (c == null || c.getGroup() == Settings.groupAdmin) {
        //questi sono i comandi esclusivi da amministratori
        if ((c == null || c.getGroup().can("hash")) && cmd[0].equalsIgnoreCase("/hash")) { //hash di una stringa
            if (cmd.length != 2) {
                if (c == null) {
                    Server.out("Hash de che?");
                } else {
                    c.send("hash de che?\n");
                }
                return;
            }
            if (c == null) {
                Server.out("hash di " + cmd[1] + ": " + Utils.getSecureHash(cmd[1]));
            } else {
                c.send("hash di " + cmd[1] + ": " + Utils.getSecureHash(cmd[1]) + "\n");
            }
            return;
        }
        if ((c == null || c.getGroup().can("ahelp")) && cmd[0].equalsIgnoreCase("/adminhelp")) {
            if (c == null) {
                System.out.print(Settings.getAdminHelpMsg());
            } else {
                c.send(Settings.getAdminHelpMsg());
            }
            return;
        }
        if ((c == null || c.getGroup().can("gc")) && cmd[0].equalsIgnoreCase("/gc")) {
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
        if ((c == null || c.getGroup().can("setgroup")) && cmd[0].equalsIgnoreCase("/setGroup")) {
            if (l != 3 && l != 2) {
                if (c == null) {
                    Server.out("Uso: /setGroup nome gruppo\nOppure: /setGroup nome");
                } else {
                    c.send("Uso: /setGroup nome gruppo\nOppure /setGroup nome\n");
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
            Group g = Group.get(cmd[2]);
            if (g == null) { //gruppo non esiste
                if (c == null) {
                    Server.out("Gruppo non esiste!");
                } else {
                    c.send("Gruppo non esiste!\n");
                }
                return;
            } else if (g == ch.getGroup()) { //utente fa gia parte del gruppo
                if (c == null) {
                    Server.out(ch.getScreenName(true) + " fa già parte del gruppo " + g.getName());
                } else {
                    c.send(ch.getScreenName(true) + " fa già parte del gruppo " + g.getName() + "\n");
                }
                return;
            } else if (g == Settings.groupGuest) { //utente va disconnesso
                if (c == null) {
                    Server.out("Logout forzato per " + ch.getScreenName(true));
                }
                ClientHandler.send("Logout forzato per " + ch.getScreenName(false) + "\n", Settings.groupAdmin);
                ch.save();
                ch.logout();
                return;
            } else { //tutto ok, imposto gruppo
                if (c == null) {
                    Server.out(ch.getScreenName(true) + " fa ora parte del gruppo " + g.getName());
                }
                ClientHandler.send(ch.getScreenName(true) + " fa ora parte del gruppo " + g.getName() + "\n", Settings.groupAdmin);
                ch.setGroup(g);
                ch.save();
                return;
            }
        }
        if ((c == null || c.getGroup().can("status")) && cmd[0].equalsIgnoreCase("/status")) { //STATO DEL SERVER
            //Date d= Server.getTimePassed();
            if (c == null) {
                Server.out(Server.getStatus());
            } else {
                c.send(Server.getStatus() + "\n");
            }
            return;
        }
        if ((c == null || c.getGroup().can("stop")) && cmd[0].equalsIgnoreCase("/stop")) {
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
        if ((c == null || c.getGroup().can("kick")) && cmd[0].equalsIgnoreCase("/kick")) {
            if (l == 1) {
                if (c != null) {
                    c.send(c.getScreenName(true) + " Disconnette tutti!\n", Settings.groupAdmin);
                    if (c.getGroup() == Settings.groupUser) {
                        Settings.globalChannel.send(c.getScreenName(false) + " Disconnette tutti!\n");
                    }
                } else {
                    Server.out("Disconnetto tutti!");
                }
                ClientHandler.disconnectAll();
                return;
            } else if (l == 2) {
                ClientHandler ch;
                if ((ch = ClientHandler.get(cmd[1])) != null) {
                    if (c != null) {
                        //c.send("Disconnetto tutti!\n");
                        c.send(c.getScreenName(true) + " disconnette " + ch.getScreenName(true) + "\n", Settings.groupAdmin);
                    } else {
                        Server.out("Disconnessione di " + ch.getScreenName(true));
                    }
                    ch.disconnect();
                    return;
                } else if (c == null) {
                    Server.out("Utente non esiste.");
                } else {
                    c.send("Utente non esiste\n");
                }
            } else if (c == null) {
                Server.out("Uso: /kick\nOppure /kick Utente");
            } else {
                c.send("Uso: /kick\nOppure /kick Utente\n");
            }
            return;
        }
        if ((c == null || c.getGroup().can("save")) && cmd[0].equalsIgnoreCase("/save")) {
            if (c != null) {
                c.send("Salvo dati server...\n");
            }
            Server.save();
            if (c != null) {
                c.send("Dati server salvati!\n");
            }
            return;
        }
        //fine comandi
        if (c == null) {
            Server.out("Comando sconosciuto o comando non utilizzabile dal server.");
        } else {
            c.send("Comando sconosciuto o non eseguibile dal gruppo \"" + c.getGroup().getName() + "\" al quale sei assegnato.\n");
        }
    }
    /**
     * @throws UnsupportedOperationException la classe non dovrebbe essere istanziata.
     */
    private Cmd(){
        throw new UnsupportedOperationException("La classe Cmd non dovrebbe essere istanziata");
    }
}
