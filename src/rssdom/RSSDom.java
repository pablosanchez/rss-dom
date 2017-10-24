package rssdom;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;

/**
 * This program takes a RSS URL feed as input, processes it by using DOM
 * strategy and prints out the news on that RSS channel and generates an XML document
 * @author Pablo Sanchez
 * @version 1.0
 */
public class RSSDom {

    private static final String RSS_URL = "http://www.europapress.es/rss/rss.aspx";

    private RSSDom() {}

    public static void main(String[] args) {
        new RSSDom().onInitialize();
    }

    private void onInitialize() {
        try {
            Document doc = getDocumentFromUrl(RSS_URL);
            printChannelInfo(doc);
            printChannelNews(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method takes an URL and returns a {@link Document} ready to be processed
     * @param url The RSS URL
     * @return The document loaded as a DOM
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public Document getDocumentFromUrl(String url) throws ParserConfigurationException, IOException, SAXException {
        if (url.isEmpty()) {
            throw new IllegalArgumentException("URL must not be empty");
        }

        if (!url.startsWith("http")) {
            throw new IllegalArgumentException("URL must start by \"http\"");
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new URL(url).openStream());
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
            print("\tTitulo: " + getTextFromNode(title));
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
            print("\t\tTitulo: " + getTextFromNode(title));
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
