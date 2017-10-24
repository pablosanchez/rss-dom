package rssdom;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;

public class RSSDom {

    private static final String RSS_URL = "http://www.europapress.es/rss/rss.aspx";

    private RSSDom() {}

    public static void main(String[] args) {
        new RSSDom().onInitialize();
    }

    private void onInitialize() {
        try {
            Document doc = getDocumentFromUrl(RSS_URL);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Document getDocumentFromUrl(String url) throws ParserConfigurationException, IOException, SAXException {
        if (url.isEmpty()) {
            throw new IllegalArgumentException("URL must not be empty");
        }

        if (!url.startsWith("http")) {
            throw new IllegalArgumentException("URL must start by 'http'");
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new URL(url).openStream());
    }
}
