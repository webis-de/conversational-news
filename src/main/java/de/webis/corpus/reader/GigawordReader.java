package de.webis.corpus.reader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringList;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.aitools.commons.uima.core.ArticleMetadata;
import de.aitools.commons.uima.core.Paragraph;
import de.aitools.commons.uima.core.SourceDocumentInformation;
import de.aitools.commons.uima.pipeline.CollectionReaderComponent;

/**
 * A collection reader for the Gigaword corpus.
 * 
 * @author lukas.peter.trautner@uni-weimar.de
 *
 */
public class GigawordReader extends CollectionReaderComponent {
  
  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------
  
  /**
   * The name of the corpus.
   */
  private static final String CORPUS_NAME = "gigaword";
  
  // -------------------------------------------------------------------------
  // MEMBERS
  // -------------------------------------------------------------------------
  
  /**
   * The xml-parser.
   */
  private SAXParser parser;
  
  /**
   * Iterates the files in the input directory.
   */
  private Iterator<File> fileIter;
  
  /**
   * Iterates the documents in a file.
   */
  private Iterator<Document> docIter;
  
  /**
   * The number processed documents.
   */
  private int processed;
  
  // -------------------------------------------------------------------------
  // CONSTRUCTORS
  // -------------------------------------------------------------------------
  
  /**
   * Creates a new GigawordCollectionReader.
   */
  public GigawordReader() {
    this.parser = null;
    this.fileIter = null;
    this.docIter = null;
    this.processed = 0;
  }
  
  // -------------------------------------------------------------------------
  // GETTERS
  // -------------------------------------------------------------------------
  
  /**
   * Gets the current parser.
   * 
   * @return the parser
   * 
   * @see {@link #parser}
   * @see {@link #setParser(SAXParser)}
   */
  public SAXParser getParser() {
    return this.parser;
  }
  
  /**
   * Gets the current file-iterator.
   * 
   * @return the iterator
   * 
   * @see {@link #fileIter}
   * @see {@link #setFileIter(Iterator)}
   * @see {@link #makeFileIter(File)}
   */
  public Iterator<File> getFileIter() {
    return this.fileIter;
  }
  
  /**
   * Gets the current document-iterator.
   * 
   * @return the iterator
   * 
   * @see {@link #docIter}
   * @see {@link #setDocIter(Iterator)}
   * @see {@link #makeDocIter(File)}
   */
  public Iterator<Document> getDocIter() {
    return this.docIter;
  }
  
  /**
   * Gets the number of processed documents.
   * 
   * @return the number of processed documents
   * 
   * @see {@link #processed}
   * @see {@link #setProcessed(int)}
   */
  public int getProcessed() {
    return this.processed;
  }
  
  // -------------------------------------------------------------------------
  // SETTERS
  // -------------------------------------------------------------------------
  
  /**
   * Sets the xml-parser.
   * 
   * @param parser
   * 
   * @see {@link #parser}
   * @see {@link #getParser()} 
   */
  public void setParser(final SAXParser parser) {
    this.parser = parser;
  }
  
  /**
   * Sets the file-iterator.
   * 
   * @param fileIter the iterator
   * 
   * @see {@link #fileIter}
   * @see {@link #getFileIter()}
   * @see {@link #makeFileIter(File)}
   */
  public void setFileIter(final Iterator<File> fileIter) {
    this.fileIter = fileIter;
  }
  
  /**
   * Sets the document-iterator.
   * 
   * @param docIter the iterator
   * 
   * @see {@link #docIter}
   * @see {@link #getDocIter()}
   * @see {@link #makeDocIter(File)}
   */
  public void setDocIter(final Iterator<Document> docIter) {
    this.docIter = docIter;
  }
  
  /**
   * Sets the number of processed documents.
   * 
   * @param processed the number of processed documents
   * 
   * @see {@link #processed}
   * @see {@link #getProcessed()}
   */
  public void setProcessed(final int processed) {
    this.processed = processed;
  }

  // -------------------------------------------------------------------------
  // CONFIGURATION
  // -------------------------------------------------------------------------

  @Override
  public void configure(Properties properties) {
    try {
      final SAXParserFactory factory = SAXParserFactory.newInstance();
      final File inputDirectory =
          new File(properties.getProperty("input", null));
      this.setParser(factory.newSAXParser());
      this.setFileIter(this.makeFileIter(inputDirectory));
      this.setDocIter(this.makeDocIter(this.getFileIter().next()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  // -------------------------------------------------------------------------
  // FUNCTIONALITY
  // -------------------------------------------------------------------------

  @Override
  public void getNext(CAS cas) throws IOException, CollectionException {
    try {
      cas.reset();
      final JCas jCas = cas.getJCas();
      final Document document = this.getDocIter().next();
      this.addDocumentText(jCas, document);
      this.addSourceDocumentInformation(jCas, document);
      this.addMetadata(jCas, document);
    } catch (CASException e) {
      e.printStackTrace();
    }
    this.setProcessed(this.getProcessed() + 1);;
  }

  @Override
  public boolean hasNext() throws IOException, CollectionException {
    if (this.getDocIter().hasNext()) {
      return true;
    } else if (this.getFileIter().hasNext()) {
      this.setDocIter(this.makeDocIter(this.getFileIter().next()));
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Progress[] getProgress() {
    return new Progress[] {
        new ProgressImpl(this.getProcessed(), -1, Progress.ENTITIES)
    };
  }

  @Override
  public void close() throws IOException {}
  
  // -------------------------------------------------------------------------
  // HELPERS
  // -------------------------------------------------------------------------
  
  /**
   * Creates an iterator over the files in a directory.
   * 
   * @param directory the directory containing the files
   * @return the iterator
   * 
   * @see {@link #fileIter}
   * @see {@link #getFileIter()}
   * @see {@link #setFileIter(Iterator)}
   */
  private Iterator<File> makeFileIter(final File directory) {
    return FileUtils.listFiles(directory, null, true).iterator();
  }
  
  /**
   * Creates an iterator over the documents in a file.
   * 
   * @param file the file containing the documents
   * @return the iterator
   * @throws IOException in case the file could not be read
   * 
   * @see {@link #docIter}
   * @see {@link #getDocIter()}
   * @see {@link #setDocIter(Iterator)}
   */
  private Iterator<Document> makeDocIter(final File file) throws IOException {
    final String content =
        FileUtils.readFileToString(file, Charset.defaultCharset());
    final List<Document> documents = new ArrayList<Document>();
    for (final String xml : Arrays.asList(content.split("</DOC>\n"))) {
      try {
        final Document doc = this.parse(xml.replaceAll("&", "&amp;") + "</DOC>");
        documents.add(doc);
      } catch (SAXException e) {
        System.err.println("Failed to parse document in file: "
            + file.getAbsolutePath());
      }
    }
    return documents.iterator();
  }
  
  /**
   * Parses an xml-string into a {@link GigawordReader.Document}
   * with the help of a {@link GigawordReader.GigawordHandler}.
   * 
   * @param xml the xml-document
   * @return the document
   * @throws UnsupportedEncodingException
   *            in case the encoding is not supported
   * @throws SAXException in case the document could not be parsed
   * @throws IOException
   * 
   * @see {@link GigawordReader.Document}
   * @see {@link GigawordReader.GigawordHandler}
   */
  private Document parse(final String xml)
      throws UnsupportedEncodingException, SAXException, IOException {
    final Document document = new Document();
    this.getParser().parse(
        new ByteArrayInputStream(xml.getBytes("UTF-8")),
          new GigawordHandler(document));
    return document;
  }
  
  /**
   * Adds the text of a document to a jcas, by either adding the text
   * with {@link Paragraph}-annotations or just adding the text,
   * if no paragraphs are present.
   * 
   * @param jCas the jcas
   * @param document the document
   * 
   * @see {@link GigawordReader.Document}
   * @see {@link #addParagraphs(JCas, List)}}
   * @see {@link #addSourceDocumentInformation(JCas, Document)}
   * @see {@link #addMetadata(JCas, Document)}
   */
  private void addDocumentText(
      final JCas jCas, final Document document) {
    final List<String> paragraphs = document.getParagraphs();
    if (paragraphs.size() == 0) {
      jCas.setDocumentText(document.getText());
    } else {
      this.addParagraphs(jCas, paragraphs);
    }
  }
  
  /**
   * Adds the text of document with corresponding {@link Paragraph}
   * -annotations to a jcas.
   * 
   * @param jCas the jcas
   * @param paragraphs the paragraphs
   * 
   * @see {@link #addDocumentText(JCas, Document)}
   * @see {@link #addSourceDocumentInformation(JCas, Document)}
   * @see {@link #addMetadata(JCas, Document)}
   */
  private void addParagraphs(
      final JCas jCas, final List<String> paragraphs) {
    final StringBuilder builder = new StringBuilder();
    int offset = 0;
    for (final String paragraph : paragraphs) {
      builder.append(paragraph + " ");
      Paragraph p = new Paragraph(jCas);
      p.setBegin(offset);
      p.setEnd(offset + paragraph.length() + 1);
      p.addToIndexes();
      offset += paragraph.length() + 1;
    }
    jCas.setDocumentText(builder.toString());
  }
  
  /**
   * Adds the {@link SourceDocumentInformation} to a jcas.
   * 
   * @param jCas the jcas
   * @param document the document
   * 
   * @see {@link #addDocumentText(JCas, Document)}
   * @see {@link #addParagraphs(JCas, List)}
   * @see {@link #addMetadata(JCas, Document)}
   */
  private void addSourceDocumentInformation(
      final JCas jCas, final Document document) {
    final SourceDocumentInformation sdi =
        new SourceDocumentInformation(jCas);
    sdi.setOffsetInSource(0);
    sdi.setDocumentSize(jCas.getDocumentText().length());
    sdi.setName(CORPUS_NAME + "-" + document.getId());
    sdi.addToIndexes();
  }
  
  /**
   * Adds the metadata as an annotation to the jcas.
   * 
   * @param jCas the jcas
   * @param document the document
   * 
   * @see {@link #addSourceDocumentInformation(JCas, Document)}
   * @see {@link #addParagraphs(JCas, List)}
   * @see {@link #addMetadata(JCas, Document)}
   */
  private void addMetadata(
      final JCas jCas, final Document document) {
    final ArticleMetadata metadata = new ArticleMetadata(jCas);
    metadata.setBegin(0);
    metadata.setEnd(jCas.getDocumentText().length());
    metadata.setCredit(CORPUS_NAME);
    metadata.setHeadline(document.getHeadline());
    metadata.setDateline(document.getDateline());
    metadata.setPublicationDate(this.getDate(document));
    metadata.setTypesOfMaterial(
        StringList.create(jCas, new String[] {document.getType()}));
    metadata.addToIndexes();
  }
  
  /**
   * Gets the publication date from a documents id.
   * 
   * @param document the document
   * @return the date
   */
  private String getDate(final Document document) {
    String date = document.getId();
    int i = date.indexOf('_', 6);
    int j = date.indexOf('.');
    date = date.substring(i + 1, j);
    return date + "T000000";
  }
  
  /**
   * Handler for the xml-parser to parse the xml-document into a
   * {@link GigawordReader.Document}.
   * 
   * @author lukas.peter.trautner@uni-weimar.de
   *
   * @see {@link GigawordReader.Document}
   */
  private class GigawordHandler extends DefaultHandler {
    
    // -----------------------------------------------------------------------
    // MEMBERS
    // -----------------------------------------------------------------------
    
    /**
     * The {@link GigawordReader.Document} into which the data is
     * parsed.
     */
    private Document document;
    
    /**
     * Indicates whether the current element is a headline.
     */
    private boolean isHeadline;
    
    /**
     * Indicates whether the current element is a dateline.
     */
    private boolean isDateLine;
    
    /**
     * Indicates whether the current element is a text element.
     */
    private boolean isText;
   
    /**
     * Indicates whether the current element is a paragraph.
     */
    private boolean isParagraph;
   
    /**
     * Builds the headline.
     */
    private StringBuilder headlineBuilder;
    
    /**
     * Builds the dateline.
     */
    private StringBuilder datelineBuilder;
    
    /**
     * Builds the document text.
     */
    private StringBuilder textBuilder;
    
    /**
     * Builds a paragraph.
     */
    private StringBuilder paragraphBuilder;
    
    // -----------------------------------------------------------------------
    // CONSTRUCTORS
    // -----------------------------------------------------------------------
    
    /**
     * Create a new {@link GigawordHandler}.
     * 
     * @param document the document 
     */
    private GigawordHandler(final Document document) {
      this.document = document;
      this.isHeadline = false;
      this.isDateLine = false;
      this.isText = false;
      this.isParagraph = true;
      this.headlineBuilder = new StringBuilder();
      this.datelineBuilder = new StringBuilder();
      this.textBuilder = new StringBuilder();
      this.paragraphBuilder = new StringBuilder();
    }
    
    // -----------------------------------------------------------------------
    // FUNCTIONALITY
    // -----------------------------------------------------------------------
    
    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        if (name.equals("DOC")) {
          this.document.setId(attributes.getValue(0));
          this.document.setType(attributes.getValue(1));
        } else if (name.equals("HEADLINE")) {
          this.isHeadline = true;
        } else if (name.equals("DATELINE")) {
          this.isDateLine = true;
        } else if (name.equals("P")) {
          this.isParagraph = true;
        } else if (name.equals("TEXT")) {
          this.isText = true;
        }
    }
      
    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
      if (name.equals("HEADLINE")) {
        this.isHeadline = false;
        this.document.setHeadline(
            this.headlineBuilder.toString().replaceAll("\\s+", " ").trim());
      } else if (name.equals("DATELINE")) {
        this.isDateLine = false;
        this.document.setDateline(
            this.datelineBuilder.toString().replaceAll("\\s+", " ").trim());
      } else if (name.equals("P")) {
        this.isParagraph = false;
        this.document.getParagraphs().add(
            this.paragraphBuilder.toString().replaceAll("\\s+", " ").trim());
        this.paragraphBuilder = new StringBuilder();
      } else if (name.equals("TEXT")) {
        this.isText = false;
        this.document.setText(
            this.textBuilder.toString().replaceAll("\\s+", " ").trim());
      } 
    }
    
    @Override
    public void characters(char[] buffer, int start, int length) throws SAXException {
      if (this.isHeadline)
        this.headlineBuilder.append(buffer, start, length);
      else if (this.isDateLine)
        this.datelineBuilder.append(buffer, start, length);
      else if (this.isParagraph)
        this.paragraphBuilder.append(buffer, start, length);
      if (this.isText)
        this.textBuilder.append(buffer, start, length);
    }
  }
  
  /**
   * Class that represents Gigaword document.
   * 
   * @author lukas.peter.trautner@uni-weimar.de
   * 
   * @see {@link GigawordReader.GigawordHandler}
   */
  private class Document {
    
    // -----------------------------------------------------------------------
    // MEMBERS
    // -----------------------------------------------------------------------
    
    /**
     * The id of a document.
     */
    private String id;
    
    /**
     * The type of a document.
     */
    private String type;
    
    /**
     * The headline of a document.
     */
    private String headline;
   
    /**
     * The dateline of a document.
     */
    private String dateline;
    
    /**
     * The text of a document.
     */
    private String text;
    
    /**
     * The paragraphs of a document.
     */
    private final List<String> paragraphs;
    
    // -----------------------------------------------------------------------
    // CONSTRUCTORS
    // -----------------------------------------------------------------------
    
    /**
     * Creates a new document.
     */
    private Document() {
      this.id = null;
      this.type = null;
      this.headline = null;
      this.dateline = null;
      this.text = null;
      this.paragraphs = new ArrayList<String>();
    }
    
    // -----------------------------------------------------------------------
    // GETTERS
    // -----------------------------------------------------------------------
    
    /**
     * Get the id of a document.
     * 
     * @return the id
     */
    private String getId() {
      return this.id;
    }
    
    /**
     * Gets the type of a document.
     * 
     * @return the type
     */
    private String getType() {
      return this.type;
    }
    
    /**
     * Gets the headline of a document.
     * 
     * @return the headline
     */
    private String getHeadline() {
      return this.headline;
    }
    
    /**
     * Gets the dateline of a document.
     * 
     * @return the dateline
     */
    private String getDateline() {
      return this.dateline;
    }
    
    /**
     * Gets the text of a document.
     * 
     * @return the text
     */
    private String getText() {
      return this.text;
    }
    
    /**
     * Gets the paragraphs of a document.
     * 
     * @return the paragraphs
     */
    private List<String> getParagraphs() {
      return this.paragraphs;
    }
    
    // -----------------------------------------------------------------------
    // SETTERS
    // -----------------------------------------------------------------------
    
    /**
     * Sets the id of a document.
     * 
     * @param id the id
     */
    private void setId(final String id) {
      this.id = id;
    }
    
    /**
     * Sets the type of a document.
     * 
     * @param type the type.
     */
    private void setType(final String type) {
      this.type = type;
    }
    
    /**
     * Sets the Headline of a document
     * 
     * @param headline the headline
     */
    private void setHeadline(final String headline) {
      this.headline = headline;
    }
    
    /**
     * Sets the dateline of a document.
     * 
     * @param dateline the dateline
     */
    private void setDateline(final String dateline) {
      this.dateline = dateline;
    }
    
    /**
     * Sets the text of a document.
     * 
     * @param text the text
     */
    private void setText(final String text) {
      this.text = text;
    }
  }

}
