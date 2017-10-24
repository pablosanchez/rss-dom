package rssdom;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This program takes a RSS URL feed as input, processes it by using DOM
 * strategy and prints out the news on that RSS channel and generates an XML document
 * @author Pablo Sanchez
 * @version 1.0
 */
public class RSSDom {

    private static final String RSS_URL = "http://www.europapress.es/rss/rss.aspx";

    private String channelTitle;
    private List<String> newsTitles;

    private RSSDom() {
        newsTitles = new ArrayList<>();
    }

    public static void main(String[] args) {
        new RSSDom().onInitialize();
    }

    private void onInitialize() {
        try {
            Document docIn = getDocumentFromUrl(RSS_URL);
            printChannelInfo(docIn);
            printChannelNews(docIn);
            Document docOut = generateXMLDoc(getDocumentFromUrl(""));
            writeXMLToFile(docOut);
            writeJSONToFile(generateJSON());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method takes an URL and returns a {@link Document} ready to be processed
     * @param url The RSS URL
     * @return The document loaded as a DOM
     * @throws ParserConfigurationException ParserConfigurationException
     * @throws IOException IOException
     * @throws SAXException SAXException
     */
    private Document getDocumentFromUrl(String url) throws ParserConfigurationException, IOException, SAXException {
        if (!url.isEmpty() && !url.startsWith("http")) {
            throw new IllegalArgumentException("URL must start by \"http\"");
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        if (url.isEmpty()) {
            return db.newDocument();
        } else {
            return db.parse(new URL(url).openStream());
        }
    }

    /**
     * This methods receives a {@link Document} and prints out the title, url and description of the channel
     * @param doc The document to process
     */
    private void printChannelInfo(Document doc) {
        if (doc == null) {
            throw new IllegalArgumentException("Document must not be null");
        }

        print("Información del canal");
        Element channel = (Element) doc.getElementsByTagName("channel").item(0);
        if (channel != null) {
            Element title = (Element) channel.getElementsByTagName("title").item(0);
            channelTitle = getTextFromNode(title);
            print("\tTitulo: " + channelTitle);
            Element url = (Element) channel.getElementsByTagName("link").item(0);
            print("\tUrl: " + getTextFromNode(url));
            Element description = (Element) channel.getElementsByTagName("description").item(0);
            print("\tDescripcion: " + getTextFromNode(description));
        }
    }

    /**
     * This methods receives a {@link Document} and prints out the news inside it
     * @param doc The document to process
     */
    private void printChannelNews(Document doc) {
        if (doc == null) {
            throw new IllegalArgumentException("Document must not be null");
        }

        print("Noticias");
        NodeList news = doc.getElementsByTagName("item");
        for (int i = 0; i < news.getLength(); i++) {
            print("\tNoticia " + (i+1));
            Element currentNew = (Element) news.item(i);
            Element title = (Element) currentNew.getElementsByTagName("title").item(0);
            String titleText = getTextFromNode(title);
            newsTitles.add(titleText);
            print("\t\tTitulo: " + titleText);
            Element url = (Element) currentNew.getElementsByTagName("link").item(0);
            print("\t\tUrl: " + getTextFromNode(url));
            Element description = (Element) currentNew.getElementsByTagName("description").item(0);
            print("\t\tDescripcion: " + getTextFromNode(description));
            Element pubDate = (Element) currentNew.getElementsByTagName("pubDate").item(0);
            print("\t\tFecha de publicacion: " + getTextFromNode(pubDate));
            Element category = (Element) currentNew.getElementsByTagName("category").item(0);
            print("\t\tCategoria: " + getTextFromNode(category));
        }
    }

    /**
     * This method receives an empty document and generates another document
     * with the following format:
     * <noticias canal="titulo canal">
     *     <noticia>titulo noticia</noticia>
     * </noticias>
     * @param doc The document to process
     * @return The XML document ready to be written on a file
     */
    private Document generateXMLDoc(Document doc) {
        if (doc == null) {
            throw new IllegalArgumentException("Document must not be null");
        }

        Element noticias = doc.createElement("noticias");
        noticias.setAttribute("canal", channelTitle);
        doc.appendChild(noticias);

        for (String title : newsTitles) {
            Element noticia = doc.createElement("noticia");
            Text newTitle = doc.createTextNode(title);
            noticia.appendChild(newTitle);
            noticias.appendChild(noticia);
        }

        return doc;
    }

    /**
     * This method receives an XML document and writes it to a file
     * @param doc The document to process
     * @throws TransformerException TransformerException
     */
    private void writeXMLToFile(Document doc) throws TransformerException {
        if (doc == null) {
            throw new IllegalArgumentException("Document must not be null");
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();

        transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
        transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        transformer.transform(new DOMSource(doc),
                new StreamResult(new File("noticias_" + channelTitle + ".xml")));
    }

    /**
     * This methods generates a JSON string with the following format:
     * {"noticias":
     *   [{"noticia": "titulo noticia"},
     *    {"noticia": "titulo noticia"}
     *    ...
     *   ]
     * }
     * @return The JSON
     */
    private String generateJSON() throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray noticias = new JSONArray();

        for (String title : newsTitles) {
            JSONObject noticia = new JSONObject();
            noticia.put("noticia", title);
            noticias.put(noticia);
        }

        json.put("noticias", noticias);

        return json.toString();
    }

    /**
     * This method takes a JSON string and writes it to a file
     * @param json The JSON string to write on a file
     */
    private void writeJSONToFile(String json) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("noticias_" + channelTitle + ".json"));
        bw.write(json);
        bw.close();
    }

    /**
     * This method takes an element of the DOM and returns the string associated with the text node
     * @param node The element to get the text from
     * @return The text associated with the node
     */
    private String getTextFromNode(Element node) {
        if (node != null) {
            return node.getFirstChild().getNodeValue();
        } else {
            return null;
        }
    }

    private void print(String text) {
        System.out.println(text);
    }
}
