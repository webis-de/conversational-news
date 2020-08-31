package de.aitools.commons.uima.delta;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class DomTest {

  public static void main(final String[] args) throws Exception {
    File xmlFile = new File("analyzed-example-data/wikipedia-articles/catopsbaatar-first-sentence.txt/000000005.xmi");
    
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = factory.newDocumentBuilder();
    Document doc = dBuilder.parse(xmlFile);
    
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transf = transformerFactory.newTransformer();
    DOMSource source = new DOMSource(doc);
    File outputFile = new File(
        "analyzed-example-data/wikipedia-articles/catopsbaatar-first-sentence.txt/output.xmi");
    transf.transform(source, new StreamResult(outputFile));
  }

}
