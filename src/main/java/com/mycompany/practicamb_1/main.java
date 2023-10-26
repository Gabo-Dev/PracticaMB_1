package com.mycompany.practicamb_1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JOptionPane;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

public class main {

    public static void main(String[] args) throws FileNotFoundException, SolrServerException, IOException {
        Scanner sc;
        String filename, dir;
        String state = "b", index = null, title = null, author = null, text = null,
                line, parts[];
         //ArrayList<String> desc = null;
        
        
        int opc = JOptionPane.showConfirmDialog(null, "¿Insertar documento?");
        if (opc == 0) {
            // Reset Colecciones
            DeletingAllDocuments();
            dir = System.getProperty("user.dir");
            filename = dir + "\\src\\corpus\\CISI.ALL";
            sc = new Scanner(new File(filename));

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
                    if ((line.startsWith(".X", 0)) || "IGNORING".equals(state)) {  //referencias cruzadas del documento     
                        //no hago nada
                    } else {
                        //aquí tampoco
                    }
                }
            }
            indexaDocumento(index, title, author, text);
        } else {
            opc = JOptionPane.showConfirmDialog(null, "¿Consultar documento?");
            if (opc == 0) {
                //  Consulta */
               
                dir = System.getProperty("user.dir");
                filename = dir + "\\src\\corpus\\CISI.QRY";
                sc = new Scanner(new File(filename));
                while (sc.hasNextLine()) {
                    line = sc.nextLine();
                    //System.out.println(line);
                    //  indice
                    if (line.startsWith(".W", 0)) {
                        //desc.add(line);
                        text = sc.nextLine();
                        searchDocuments(text);
                    }
                }
               
                //searchDocuments(title);
            }
        }

    }

    private static void DeletingAllDocuments() {
        // Preparing Solr Client
        String url = "http://localhost:8983/solr/micoleccion";
        SolrClient Solr = new HttpSolrClient.Builder(url).build();

        try {
            // Delete all
            Solr.deleteByQuery("*");
            Solr.commit();
        } catch (IOException | SolrServerException e) {
            System.out.println("Error: " + e.getMessage());
        }

        //  Saving the document
        System.out.println("Documents deleted");
    }

    private static void indexaDocumento(String index, String title, String author, String text) throws SolrServerException, IOException {
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

    private static void searchDocuments(String desc) throws SolrServerException, IOException {
        HttpSolrClient solr = new HttpSolrClient.Builder("http://localhost:8983/solr/micoleccion").build();
        SolrQuery query = new SolrQuery();
        query.setQuery("text");
        QueryResponse rsp = solr.query(query);
        SolrDocumentList docs = rsp.getResults();
         int tm = docs.size();
        System.out.println("Tamaño de la consulta: " + tm);
        /*for (int i = 0; i < tm; i++) {
            System.out.println(docs.get(i).get("title"));
        }*/
    }
}
