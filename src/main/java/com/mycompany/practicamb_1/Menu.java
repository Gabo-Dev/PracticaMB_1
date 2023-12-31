/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.practicamb_1;

import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CoreAdminParams;

/**
 *
 * @author jhony
 */
public class Menu extends javax.swing.JFrame {

    SolrDocumentList docs = new SolrDocumentList(), trecDoc = new SolrDocumentList();
    int noConsultas = 0;

    /**
     * Creates new form Menu
     */
    public Menu() {
        initComponents();
        this.setLocationRelativeTo(null);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        indexaBtn = new javax.swing.JButton();
        consultaBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        textInfo = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        indexaBtn.setText("Indexar Documento");
        indexaBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indexaBtnActionPerformed(evt);
            }
        });

        consultaBtn.setText("Consulta");
        consultaBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                consultaBtnActionPerformed(evt);
            }
        });

        textInfo.setEditable(false);
        textInfo.setColumns(20);
        textInfo.setRows(5);
        jScrollPane1.setViewportView(textInfo);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(384, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(indexaBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
                    .addComponent(consultaBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(358, 358, 358))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(111, Short.MAX_VALUE)
                .addComponent(indexaBtn)
                .addGap(18, 18, 18)
                .addComponent(consultaBtn)
                .addGap(71, 71, 71)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(43, 43, 43))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void indexaBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexaBtnActionPerformed
        // TODO add your handling code here:
        try {
            Scanner sc = null;
            String state = "b", index = null, title = null, author = null, text = null,
                    line, parts[];
            String url = "http://localhost:8983/solr";
            if (hasCollection(url)) {
                int opc = JOptionPane.showConfirmDialog(null, "Solr tiene un core, deseas hacer un reset?");
                if (opc == 0) {
                    // Reset Colecciones
                    DeletingAllDocuments();
                }
            }
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(null);
            File file = null;
            if (result == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
            }

            sc = new Scanner(file);
            //  leemos
            while (sc.hasNextLine()) {
                state = "p";
                line = sc.nextLine();
                //System.out.println(line);
                //  indice
                if (line.startsWith(".I", 0)) {
                    indexaDocumento(index, title, author, text);
                    parts = line.split(" ");
                    index = parts[1];
                    //  System.out.println(index);
                }
                if (line.startsWith(".T", 0)) {
                    title = sc.nextLine();
                }
                if (line.startsWith(".A", 0)) {
                    author = sc.nextLine();
                }
                if (line.startsWith(".W", 0) || "READING".equals(state)) {  //texto 
                    text = sc.nextLine();
                    state = "READING";
                }
                if ("READING".equals(state)) {  //texto del documento 
                    String part = sc.nextLine();
                    if (part.startsWith(".X", 0)) {
                        state = "IGNORING";
                    } else {
                        text = text + part;
                    }
                    
                }
            }
            indexaDocumento(index, title, author, text);
            JOptionPane.showMessageDialog(null, "Document Indexed");
        } catch (SolrServerException | IOException | NullPointerException ex) {

            JOptionPane.showMessageDialog(null, "Error: " + ex);
        }


    }//GEN-LAST:event_indexaBtnActionPerformed

    private void consultaBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_consultaBtnActionPerformed
        // TODO add your handling code here:
        try {
            Scanner sc = null;
            String text = "", line,state = "false";
            JFileChooser fileChooser = new JFileChooser();
            //fileChooser.setFileFilter(new FileNameExtensionFilter("All Files", "*.*"));
            int result = fileChooser.showOpenDialog(null);
            File file = null;
            if (result == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
            }

            sc = new Scanner(file);
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                if (line.startsWith(".I")) {
                     state="false";
                     if (!text.equals("")) {
                         searchDocuments(text);
                         text="";
                    }
                }
                if ("true".equals(state)) {
                    text =text + line+" ";
                    text = text.replaceAll("[\\[\\](){}:]", "");
                }
                if (line.startsWith(".W", 0)) {
                    state="true";
                   
                }
            }
            JOptionPane.showMessageDialog(null, "Consulta Realizada");
            JOptionPane.showMessageDialog(null, "El programa procederá a crear el fichero trec correspondiente");
              try {
            if (trecDoc.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No se ha realizado ninguna consulta");
            } else {
                String path="";
                JOptionPane.showMessageDialog(null, "Indica el directorio donde se almacenarán los datos:");
                JFileChooser t = new JFileChooser();
                t.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int selection = t.showSaveDialog(null);
                if (selection == JFileChooser.APPROVE_OPTION) {
                    path = t.getSelectedFile().getAbsolutePath();
                }
                if (createQRYTrec(path)) {

                    JOptionPane.showMessageDialog(null, "Momento de evaluar.");
                } else {
                    JOptionPane.showMessageDialog(null, "Faltan documentos para poder usar el Trec Eva");
                }
            }
        } catch (HeadlessException e) {
            System.out.println(e);
        }
        } catch (HeadlessException | IOException | NullPointerException | SolrServerException e) {
            JOptionPane.showMessageDialog(null, "Error al realizar consulta.");
        } 
    }//GEN-LAST:event_consultaBtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        Menu m = new Menu();
        m.setDefaultCloseOperation(EXIT_ON_CLOSE);
        m.setVisible(true);

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton consultaBtn;
    private javax.swing.JButton indexaBtn;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea textInfo;
    // End of variables declaration//GEN-END:variables
 public void DeletingAllDocuments() {
        String url = "http://localhost:8983/solr/micoleccion";
        // Preparing Solr Client
        SolrClient Solr = new HttpSolrClient.Builder(url).build();
        try {
            // Delete all
            Solr.deleteByQuery("*");
            Solr.commit();
        } catch (IOException | SolrServerException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        //  Saving the document
        JOptionPane.showMessageDialog(null, "Documents deleted");
    }

    public void indexaDocumento(String index, String title, String author, String text) throws SolrServerException, IOException {
        HttpSolrClient solr = new HttpSolrClient.Builder("http://localhost:8983/solr/micoleccion").build();
        //  Create SolrDocument 
        SolrInputDocument dc = new SolrInputDocument();
        dc.addField("index", index);
        dc.addField("title", title);
        dc.addField("author", author);
        dc.addField("text", text);
        solr.add(dc);
        solr.commit();

    }

    public boolean hasCollection(String url) {
        boolean has = false;
        try ( SolrClient solrClient = new HttpSolrClient.Builder(url).build()) {
            // Use CoreAdminRequest to get the list of collections
            CoreAdminRequest request = new CoreAdminRequest();
            request.setAction(CoreAdminParams.CoreAdminAction.STATUS);

            // Send the request and get the response
            CoreAdminResponse response = request.process(solrClient);

            // Get the list of collections from the response
            int numberOfCollections = response.getCoreStatus().size();
            if (numberOfCollections > 0) {
                has = true;
            }
        } catch (SolrServerException | java.io.IOException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e);
        }
        return has;
    }

    public void searchDocuments(String desc) throws SolrServerException, IOException {
        noConsultas++;
        HttpSolrClient solr = new HttpSolrClient.Builder("http://localhost:8983/solr/micoleccion").build();
        SolrQuery query = new SolrQuery();
        query.setQuery("*");
        query.addFilterQuery("text: " + desc);
        query.setFields("index", "id", "score", "title");
        QueryResponse rsp = solr.query(query);
        docs = rsp.getResults();
        int tm = docs.size();
        //System.out.println("Tamaño de la consulta : " + tm);
        for (int i = 0; i < tm; i++) {
            textInfo.append(docs.get(i).values().toString() + "\n");
            //  Añadimos el valor de cada consulta al doc global
            docs.get(i).setField("Consulta", noConsultas);
            trecDoc.add(docs.get(i));
        }
    }

    private boolean createQRYTrec(String path) {
        boolean isCreated = false;
        try {
            String team = "ETSI", line, document, fichero = "";
            fichero = JOptionPane.showInputDialog(null, "Introduzca el nombre del fichero trec Solr: ") + ".TREC";
            path = path + "\\" + fichero;
            BufferedWriter w = new BufferedWriter(new FileWriter(path));
            int tm = trecDoc.size();
            for (int i = 0; i < tm; i++) {
                document = trecDoc.get(i).getFieldValue("index").toString().replaceAll("[\\[\\](){}]", "");
                line = trecDoc.get(i).getFieldValue("Consulta") + " " + "Q0" + " " + document + " " + i + " " + trecDoc.get(i).getFieldValue("score") + " " + team + "\n";
                //  System.out.println(line);
                w.write(line);
            }
            w.close();
            isCreated = true;
            JOptionPane.showMessageDialog(null, "Fichero creado");
        } catch (HeadlessException | IOException e) {
            System.out.println(e);
            JOptionPane.showMessageDialog(null, "Error al crear el QRY.TREC ");
        }
        return isCreated;
    }

   
    
}
