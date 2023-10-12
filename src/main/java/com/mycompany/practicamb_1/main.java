package com.mycompany.practicamb_1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

public class main {

    public static void main(String[] args) throws FileNotFoundException, SolrServerException, IOException {
        String dir = System.getProperty("user.dir");
        String filename = dir + "\\src\\corpus\\CISI.ALL";
        Scanner sc = new Scanner(new File(filename));
        String state = "b", index = null, title = null, author = null, text = null,
                line, parts[];

        // Reset Colecciones
        DeletingAllDocuments();

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
                System.out.println(index);
            }
            if (line.startsWith(".T", 0)) {
                title = sc.nextLine();
            }
            if (line.startsWith(".A", 0)) {
                author = sc.nextLine();
            }
            if (line.startsWith(".W", 0) || state == "READING") {  //texto 
                text = sc.nextLine();
                state = "READING";
            }
            if (state == "READING") {  //texto del documento 
                String part = sc.nextLine();
                if (part.startsWith(".X", 0)) {
                    state = "IGNORING";
                } else {
                    text = text + part;
                }
                if ((line.startsWith(".X", 0)) || state == "IGNORING") {  //referencias cruzadas del documento     
                    //no hago nada
                }    else {
                    //aquí tampoco
                }
            }
        }
        indexaDocumento(index, title, author, text);
        
        //  Consulta 
        searchDocuments();
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

    private static void searchDocuments() throws SolrServerException, IOException {
        HttpSolrClient solr = new HttpSolrClient.Builder("http://localhost:8983/solr/micoleccion").build();
        SolrQuery query  =  new SolrQuery();
        query.setQuery("*");
        QueryResponse rsp = solr.query(query);
        SolrDocumentList docs  = rsp.getResults();
        int tm = docs.size();
        System.out.println("Tamaño de la consulta: "+ tm);
        for (int i = 0; i < tm; i++) {
            System.out.println(docs.get(i));
        }
    }
}
