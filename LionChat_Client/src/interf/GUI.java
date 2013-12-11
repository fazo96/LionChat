package interf;

import utilz.Filez;
import java.util.ArrayList;
import javax.swing.text.DefaultCaret;
import net.Connection;
import utilz.Utils;

/**
 *
 * @author fazo
 */
public class GUI extends javax.swing.JFrame {

    private String ip = "after-end.net";
    private int port = 7777;
    private static GUI gui;
    private static SettingsUI settingsUI;

    /**
     * Creates new form GUI
     */
    public GUI() {
        initComponents(); //inizializzo i componenti grafici
        setTitle("Client"); //imposto titolo finestra
        System.out.println("GUI Avviata"); //output su console
        //Imposto alla textArea di andare a capo automaticamente
        //quando si arriva al limite di caratteri orizzontali
        textArea.setLineWrap(true);
        //Imposto autoscroll
        autoScroll();
        gui = this; //creo un puntatore all'istanza che sto creando
        //Cerco di dare il focus al textfield, così non bisogna cliccarci
        //col mouse
        textField.requestFocusInWindow();
        start(); //mi connetto.
    }

    private void autoScroll() {
        ((DefaultCaret) textArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

    private void start() {
        getSettings(); //provo a leggere ip e porta da file
        append("Provo a connettermi a " + ip + ":" + port + "\n");
        Connection.connect(ip, port); //connessione effettiva
    }

    public static GUI get() { //ritorno l'istanza in esecuzione.
        return gui;
    }

    public void append(String text) {
        //scrivo un messaggio alla gui e in console senza andare a capo
        textArea.append(text);
        autoScroll();
        System.out.print(text);
    }

    public void clear() { //svuoto la textArea
        textArea.setText("");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        textField = new javax.swing.JTextField();
        sendButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jPanel1KeyPressed(evt);
            }
        });

        textField.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        textField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                textFieldKeyPressed(evt);
            }
        });

        sendButton.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        sendButton.setText("Invia");
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });

        textArea.setEditable(false);
        textArea.setColumns(20);
        textArea.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        textArea.setRows(5);
        textArea.setWrapStyleWord(true);
        jScrollPane1.setViewportView(textArea);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(textField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sendButton)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sendButton)
                    .addComponent(textField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jMenu1.setText("File");

        jMenuItem1.setText("Settings");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("Exit");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        //Questa funzione viene chiamata quando viene premuto il tasto Invia
        if (Connection.isConnected()) {
            if (textField.getText() != "" && Utils.isValid(textField.getText())) {
                //sono connesso e il testo è valido
                Connection.send(textField.getText());
            } else {
                //Sono connesso ma il testo non è valido
                System.out.println("Stringa non valida!\n");
            }
        } else {
            //Non sono connesso ma è stato premuto send
            append("Nuovo tentativo di connessione:\n");
            Connection.connect(ip, port); //provo a riccnnettermi
        }
        textField.setText(""); //dopo tutto, resetto il contenuto del textfield
        //restituisco il focus al textfield, così se l'utente ha premuto send col
        //mouse non deve cliccare anche sul textfield
        textField.requestFocusInWindow();

    }//GEN-LAST:event_sendButtonActionPerformed

    private void jPanel1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPanel1KeyPressed
        //Inutile
    }//GEN-LAST:event_jPanel1KeyPressed

    private void textFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFieldKeyPressed
        // Viene chiamato quando viene premuto un tasto e il focus è sul textfield
        if (evt.getKeyCode() == evt.VK_ENTER) { //se il tasto è invio
            sendButtonActionPerformed(null); //simula la pressione di send
        }
    }//GEN-LAST:event_textFieldKeyPressed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        settingsUI.setVisible(true);
        settingsUI.updateFields();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
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
        } catch (Exception ex) {
            System.out.println("[FATAL] Can't load look and feel");
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                gui = new GUI();
                gui.setVisible(true);
                settingsUI = new SettingsUI();
                settingsUI.setVisible(false);
            }
        });
        //gui.start();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton sendButton;
    private javax.swing.JTextArea textArea;
    private javax.swing.JTextField textField;
    // End of variables declaration//GEN-END:variables

    //legge da file ip e porta. Se non riesce, rimangono i default
    private void getSettings() {
        ArrayList<String> cnt = null;
        //leggo il file e creo una lista di stringhe contenute
        append("Inizio lettura file settings.txt\n5 tentativi.\n");
        for (int i = 0; i < 5; i++) {
            append("Tentativo di lettura numero " + (i + 1) + "\n");
            cnt = Utils.toList(Filez.getFileContent("settings.txt"), " ");
            if (cnt == null) { //se la lista è nulla, c'è stato un errore di lettura
                append("File impostazioni non trovato!\nCreazione automatica.\n");
                Filez.writeFile("settings.txt", ip + " " + port);
                continue; //non c'è nulla da fare, quindi ritorno
            }
            append("Lettura riuscita\n");
            break;
        }
        if (cnt == null) {
            append("[ERRORE] Lettura fallita! Utilizzo impostazioni di default\n");
        } else if (cnt.size() != 2) { //se gli elementi non sono due, il file non è valido
            append("Errore: file di configurazione con numero parametri errati (" + cnt.size() + ")\n"
                    + "Per crearlo tu, inserisci SOLO l'IP e la PORTA del server separati da uno spazio\n");
            return; //non c'è nulla da fare, ritorno
        }
        ip = cnt.get(0);
        port = Integer.parseInt(cnt.get(1));
    }
}
