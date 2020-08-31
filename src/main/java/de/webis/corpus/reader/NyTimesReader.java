package de.webis.corpus.reader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringList;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import de.aitools.commons.uima.core.ArticleMetadata;
import de.aitools.commons.uima.core.Paragraph;
import de.aitools.commons.uima.core.SourceDocumentInformation;
import de.aitools.commons.uima.pipeline.CollectionReaderComponent;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Nodes;

/**
 * A collection reader for the New York Times corpus.
 * 
 * @author lukas.peter.trautner@uni-weimar.de
 *
 */
public class NyTimesReader extends CollectionReaderComponent {
  
  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------
  
  /**
   * The name of the corpus.
   */
  private static final String CORPUS_NAME = "nytimes";
  
  // -------------------------------------------------------------------------
  // MEMBERS
  // -------------------------------------------------------------------------
  
  /**
   * The xml-parser.
   */
  private final Builder parser;
  
  /**
   * Iterarates the files in the input directory.
   */
  private Iterator<File> fileIter;

  /**
   * Iterates the documents in on tgz-archive.
   */
  private Iterator<Document> docIter; 

  /**
   * The current document.
   */
  private Document current;
  
  /**
   * The number of processed documents.
   */
  private int processed;
  
  // -------------------------------------------------------------------------
  // CONSTRUCTORS
  // -------------------------------------------------------------------------
  
  /**
   * Creates a new {@link NyTimesReader}.
   * 
   * @throws ParserConfigurationException
   *            in case the parser could not be instantiated
   */
  public NyTimesReader() throws ParserConfigurationException {
    this.parser = new Builder();
    this.fileIter = null;
    this.docIter = null;
    this.current = null;
    this.processed = 0;
  }
  
  // -------------------------------------------------------------------------
  // GETTERS
  // -------------------------------------------------------------------------
  
  /**
   * Gets the xml-parser.
   * 
   * @return the parser
   * 
   * @see {@link #parser}
   */
  public Builder getParser() {
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
   * @see {@link #docIter}
   * @see {@link #setDocIter(Iterator)}
   * @see {@link #makeDocIter(File)}
   */
  public Iterator<Document> getDocIter() {
    return this.docIter;
  }
  
  /**
   * Gets the current document.
   * 
   * @return the document
   * 
   * @see {@link #current}
   * @see {@link #setCurrentDocument(Document)}
   */
  public Document getCurrentDocument() {
    return this.current;
  }
  
  /**
   * Gets the number of processed documents
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
   * Sets the file iterator.
   * 
   * @param fileIter the iterator
   * 
   * @see {@link #fileIter}
   * @see {@link #getFileIter()}
   * @see {@link #makeFileIter(File)}
   * @see {@link #configure(Properties)}
   */
  public void setFileIter(final Iterator<File> fileIter) {
    this.fileIter = fileIter;
  }
  
  /**
   * Sets the document iterator.
   * 
   * @param docIter the iterator
   * 
   * @see {@link #docIter}
   * @see {@link #getDocIter()}
   * @see {@link #makeDocIter(File)}
   * @see {@link #configure(Properties)}
   */
  public void setDocIter(final Iterator<Document> docIter) {
    this.docIter = docIter;
  }
  
  /**
   * Sets the current document.
   * 
   * @param current the document
   * 
   * @see {@link #current}
   * @see {@link #getCurrentDocument()}
   */
  public void setCurrentDocument(final Document current) {
    this.current = current;
  }
  
  /**
   * Sets the number of processed documents.
   * 
   * @param processed the number of processed documents.
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
      final File inputDirectory =
          new File(properties.getProperty("input", null));
      this.setFileIter(this.makeFileIter(inputDirectory));
      this.setDocIter(this.makeDocIter(this.fileIter.next()));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  // -------------------------------------------------------------------------
  // FUNCTIONALITY
  // -------------------------------------------------------------------------

  @Override
  public void getNext(CAS cas) throws IOException, CollectionException {
    cas.reset();
    try {
      final Document document = this.getCurrentDocument();
      final JCas jCas = cas.getJCas();
      this.addDocumentText(jCas, document);
      this.addSourceDocumentInformation(jCas, document);
      this.addMetadata(jCas, document);
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    
    this.setProcessed(this.getProcessed() + 1);
  }

  @Override
  public boolean hasNext() throws IOException, CollectionException {
    if (this.getDocIter().hasNext()) {
      this.setCurrentDocument(this.getDocIter().next());
      return true;
    } else if (this.fileIter.hasNext()) {
      this.setDocIter(this.makeDocIter(this.getFileIter().next()));
      this.setCurrentDocument(this.getDocIter().next());
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Progress[] getProgress() {
    return new Progress[] {
        new ProgressImpl(this.processed, -1, Progress.ENTITIES)
    };
  }

  @Override
  public void close() throws IOException {
    
    
    
  }
  
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
   * Creates an iterator over the documents in a tgz-archive.
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
    final List<Document> documents = new ArrayList<Document>();
    TarArchiveInputStream stream = new TarArchiveInputStream(
        new GzipCompressorInputStream(
            new FileInputStream(file)));
    TarArchiveEntry entry = null;
    while ((entry = stream.getNextTarEntry()) != null) {
      if (entry.isFile()) {
        final byte[] content = new byte[(int) entry.getSize()];
        stream.read(content, 0, content.length);
        String xml = new String(content, Charset.forName("UTF-8"));
        xml = xml.replace("<!DOCTYPE nitf "
            + "SYSTEM \"http://www.nitf.org/"
            + "IPTC/NITF/3.3/specification/dtd/nitf-3-3.dtd\">", "");
        try {
          final Document doc =
              this.getParser().build(new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));
          documents.add(doc);
        } catch (Exception e) {
          continue;
        }
      }
    }
    stream.close();
    return documents.iterator();
  }
  
 /**
  * Adds the text of a document with {@link Paragraph}-annotations to a JCas.
  * 
  * @param jCas the jCas
  * @param document the document currently processed
  * 
  * @see {@link #addSourceDocumentInformation(JCas, Document)}
  * @see {@link #addMetadata(JCas, Document)}
  */
  private void addDocumentText(final JCas jCas, final Document document) {
    final List<String> paragraphs = NyTimes.getParagraphs(document);
    final StringBuilder builder = new StringBuilder();
    int offset = 0;
    for (final String paragraph : paragraphs) {
      final Paragraph p = new Paragraph(jCas);
      p.setBegin(offset);
      p.setEnd(offset + paragraph.length() + 1);
      p.addToIndexes();
      builder.append(paragraph + " ");
      offset += paragraph.length() + 1;
    }
    jCas.setDocumentText(builder.toString());
  }
  
  /**
   * Adds the {@link SourceDocumentInformation} to a jcas.
   * 
   * @param jCas the jcas
   * @param document the document currently processed
   * 
   * {@link #addDocumentText(JCas, Document)}
   * {@link #addMetadata(JCas, Document)}
   */
  private void addSourceDocumentInformation(
      final JCas jCas, final Document document) {
    final SourceDocumentInformation sdi =
        new SourceDocumentInformation(jCas);
    sdi.setOffsetInSource(0);
    sdi.setDocumentSize(jCas.getDocumentText().length());
    sdi.setName(CORPUS_NAME + "-" + NyTimes.getGUID(document));
    sdi.addToIndexes();
  }
  
  /**
   * Adds a {@link ArticleMetadata}-annotation to a JCas.
   * 
   * @param jCas the JCas
   * @param document the document currently processed
   * 
   * {@link #addDocumentText(JCas, Document)}
   * {@link #addSourceDocumentInformation(JCas, Document)}
   */
  private void addMetadata(
      final JCas jCas, final Document document) {
    final ArticleMetadata meta =  new ArticleMetadata(jCas);
    meta.setBegin(0);
    meta.setEnd(jCas.getDocumentText().length());
    NyTimes.setAlternateUrl(document, meta);
    NyTimes.setArticleAbstract(document, meta);
    NyTimes.setAuthorBiography(document, meta);
    NyTimes.setBanner(document, meta);
    NyTimes.setBiographicalCategories(document, meta, jCas);
    NyTimes.setByline(document, meta);
    NyTimes.setColumnName(document, meta);
    NyTimes.setColumnNumber(document, meta);
    NyTimes.setCorrectionDate(document, meta);
    NyTimes.setCorrectionText(document, meta);
    NyTimes.setCredit(document, meta);
    NyTimes.setDateline(document, meta);
    NyTimes.setDayOfWeek(document, meta);
    NyTimes.setDescriptors(document, meta, jCas);
    NyTimes.setFeaturePage(document, meta);
    NyTimes.setGeneralOnlineDescriptors(document, meta, jCas);
    NyTimes.setGUID(document, meta);
    NyTimes.setHeadline(document, meta);
    NyTimes.setKicker(document, meta);
    NyTimes.setLocations(document, meta, jCas);
    NyTimes.setNames(document, meta, jCas);
    NyTimes.setNewsDesk(document, meta);
    NyTimes.setNormalizedByline(document, meta);
    NyTimes.setOnlineDescriptors(document, meta, jCas);
    NyTimes.setOnlineHeadline(document, meta);
    NyTimes.setOnlineLocations(document, meta, jCas);
    NyTimes.setOnlineOrganizations(document, meta, jCas);
    NyTimes.setOnlinePeople(document, meta, jCas);
    NyTimes.setOnlineSection(document, meta);
    NyTimes.setOnlineTitles(document, meta, jCas);
    NyTimes.setOrganizations(document, meta);
    NyTimes.setPage(document, meta);
    NyTimes.setPeople(document, meta, jCas);
    NyTimes.setPublicationDate(document, meta);
    NyTimes.setSection(document, meta);
    NyTimes.setSeriesName(document, meta);
    NyTimes.setSlug(document, meta);
    NyTimes.setTaxonomicClassifiers(document, meta, jCas);
    NyTimes.setTitles(document, meta, jCas);
    NyTimes.setTypesOfMaterial(document, meta, jCas);
    NyTimes.setUrl(document, meta);
    meta.addToIndexes();
  }
  
  /**
   * Helper class for extracting metadata from a New York Times document.
   * <p>
   * For the documentation of all the elements have a look at the
   * <a href="https://catalog.ldc.upenn.edu/docs/LDC2008T19/new_york_times_
annotated_corpus.pdf">corpus documentation</a>.
   * </p>
   * 
   * @author lukas.peter.trautner@uni-weimar.de
   *
   */
  private static class NyTimes {
    
    // -----------------------------------------------------------------------
    // CONSTANTS
    // -----------------------------------------------------------------------
    
    /**
     * Xpath path expression for the full text.
     */
    private static final String FULL_TEXT =
        "/nitf/body/body.content/block[@class=\"full_text\"]/p";

    /**
     * Xpath path expression for the alternate url.
     */
    private static final String ALTERNATE_URL =
        "/nitf/head/meta[@name=\"alternate_url\"]/@content";
    
    /**
     * Xpath path expression for the article abstract.
     */
    private static final String ARTICLE_ABSTRACT =
        "/nitf/body/body.head/abstract";
    
    /**
     * Xpath path expression for the author biography.
     */
    private static final String AUTHOR_BIOGRAPHY =
        "/nitf/body/body.content/block[@class=\"author_info\"]";
    
    /**
     * Xpath path expression for the banner.
     */
    private static final String BANNER =
        "/nitf/head/meta[@name=\"banner\"]/@content";

    /**
     * Xpath path expression for the biographical categories.
     */
    private static final String BIOGRAPHICAL_CATEGORIES =
        "/nitf/head/docdata/identified-content/classifier[@class="
        + "\"indexing_service\" and @type=\"biographical_categories\"]";

    /**
     * Xpath path expression for the byline.
     */
    private static final String BYLINE =
        "/nitf/body/body.head/byline[@class=\"print_byline\"]";
    
    /**
     * Xpath path expression for the column name.
     */
    private static final String COLUMN_NAME =
        "/nitf/head/meta[@name=\"column_name\"]/@content";
    
    /**
     * Xpath path expression for the column number.
     */
    private static final String COLUMN_NUMBER =
        "/nitf/head/meta[@name=\"print_column\"]/@content";
    
    /**
     * Xpath path expression for the correction date.
     */
    private static final String CORRECTION_DATE =
        "/nitf/head/meta[@name=\"correction_date\"]/@content";
    
    /**
     * Xpath path expression for the correction text.
     */
    private static final String CORRECTION_TEXT =
        "/nitf/body/body.content/block[@class=\"correction_text\"]";
    
    /**
     * Xpath path expression for the credit.
     */
    private static final String CREDIT =
        "/nitf/head/docdata/doc.copyright/@holder";

    /**
     * Xpath path expression for the dateline.
     */
    private static final String DATELINE =
        "/nitf/body/body.head/dateline";
    
    /**
     * Xpath path expression for the day of week.
     */
    private static final String DAY_OF_WEEK =
        "/nitf/head/meta[@name=\"publication_day_of_month\"]/@content";
    
    /**
     * Xpath path expression for the descriptors.
     */
    private static final String DESCRIPTORS =
        "/nitf/head/docdata/identified-content/classifier[@class=\""
        + "indexing_service\" and @type=\"descriptor\"]";
    
    /**
     * Xpath path expression for the feature page.
     */
    private static final String FEATURE_PAGE =
        "/nitf/head/meta[@name=\"feature_page\"]/@content";
    
    /**
     * Xpath path expression for the general online descriptors.
     */
    private static final String GENERAL_ONLINE_DESCRIPTORS =
        "/nitf/head/docdata/identified-content/classifier[@class=\""
        + "online_producer\" and @type=\"general_descriptor\"]";
    
    /**
     * Xpath path expression for the GUID.
     */
    private static final String GUID =
        "/nitf/head/docdata/doc-id/@id-string";
    
    /**
     * Xpath path expression for the headline.
     */
    private static final String HEADLINE =
        "/nitf/body[1]/body.head/hedline/hl1";
    
    /**
     * Xpath path expression for the kicker.
     */
    private static final String KICKER =
        "/nitf/head/docdata/series/@series.name";
    
    /**
     * Xpath path expression for the locations.
     */
    private static final String LOCATIONS =
        "/nitf/head/docdata/identified-content/location[@class="
        + "\"indexing_service\"]";
    
    /**
     * Xpath path expression for the names.
     */
    private static final String NAMES =
        "/nitf/head/docdata/identified-content/classifier[@class="
        + "\"indexing_service\" and @type=\"names\"]";
    
    /**
     * Xpath path expression for the news desk.
     */
    private static final String NEWS_DESK =
        "/nitf/head/meta[@name=\"dsk\"]/@content";
    
    /**
     * Xpath path expression for the normalized byline.
     */
    private static final String NORMALIZED_BYLINE =
        "/nitf/body/body.head/byline[@class=\"normalized_byline\"]";
    
    /**
     * Xpath path expression for the online descriptors.
     */
    private static final String ONLINE_DESCRIPTORS =
        "/nitf/head/docdata/identified-content/classifier[@class="
        + "\"online_producer\" and @type=\"descriptor\"]";
    
    /**
     * Xpath path expression for the online headline.
     */
    private static final String ONLINE_HEADLINE =
        "/nitf/body[1]/body.head/hedline/hl2";
    
    /**
     * Xpath path expression for the online locations.
     */
    private static final String ONLINE_LOCATIONS =
        "/nitf/head/docdata/identified-content/location[@class"
        + "=\"online_producer\"]";
    
    /**
     * Xpath path expression for the online organizations.
     */
    private static final String ONLINE_ORGANIZATIONS =
        "/nitf/head/docdata/identified-content/org[@class="
        + "\"online_producer\"]";
    
    /**
     * Xpath path expression for the online people.
     */
    private static final String ONLINE_PEOPLE =
        "/nitf/head/docdata/identified-content/person[@class="
        + "\"online_producer\"]";
    
    /**
     * Xpath path expression for the online section.
     */
    private static final String ONLINE_SECTION =
        "/nitf/head/meta[@name=\"online_sections\"]/@content";
    
    /**
     * Xpath path expression for the online titles.
     */
    private static final String ONLINE_TITLES =
        "/nitf/head/docdata/identified-content/object.title[@class="
        + "\"online_producer\"]";
    
    /**
     * Xpath path expression for the organizations.
     */
    private static final String ORGANIZATIONS =
        "/nitf/head/docdata/identified-content/org[@class="
        + "\"indexing_service\"]";
    
    /**
     * Xpath path expression for the page.
     */
    private static final String PAGE =
        "/nitf/head/meta[@name=\"print_page_number\"]/@content";
    
    /**
     * Xpath path expression for the people.
     */
    private static final String PEOPLE =
        "/nitf/head/docdata/identified-content/person[@class="
        + "\"indexing_service\"]";
    
    /**
     * Xpath path expression for the publication date.
     */
    private static final String PUBLICATION_DATE =
        "/nitf/head/pubdata/@date.publication";
    
    /**
     * Xpath path expression for the section.
     */
    private static final String SECTION =
        "/nitf/head/meta[@name=\"print_section\"]/@content";
    
    /**
     * Xpath path expression for the series name.
     */
    private static final String SERIES_NAME =
        "/nitf/head/meta[@name=\"series_name\"]/@content";
    
    /**
     * Xpath path expression for the slug.
     */
    private static final String SLUG =
        "/nitf/head/meta[@name=\"slug\"]/@content";
    
    /**
     * Xpath path expression for the taxonomic classifiers.
     */
    private static final String TAXONOMIC_CLASSIFIERS =
        "/nitf/head/docdata/identified-content/classifier[@class="
        + "\"online_producer\" and @type=\"taxonomic_classifier\"]";
    
    /**
     * Xpath path expression for the titles.
     */
    private static final String TITLES =
        "/nitf/head/docdata/identified-content/object.title[@class="
        + "\"indexing_service\"]";
    
    /**
     * Xpath path expression for the types of material.
     */
    private static final String TYPES_OF_MATERIAL =
        "/nitf/head/docdata/identified-content/classifier[@class="
        + "\"online_producer\" and @type=\"types_of_material\"]";
    
    /**
     * Xpath path expression for the url.
     */
    private static final String URL =
        "/nitf/head/pubdata/@ex-ref";
    
    // -----------------------------------------------------------------------
    // FUNCTIONALITY
    // -----------------------------------------------------------------------
    
    /**
     * Sets the value of the <code>alternateUrl</code> feature of an
     * {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setAlternateUrl(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(ALTERNATE_URL);
      if (nodes.size() != 0) 
        meta.setAlternateUrl(nodes.get(0).getValue());
    }
    
    /**
     * Sets the value of the <code>articleAbstract</code> feature of an
     * {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setArticleAbstract(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(ARTICLE_ABSTRACT);
      if (nodes.size() != 0) 
        meta.setArticleAbstract(
            nodes.get(0).getValue().replace("\n", "").trim());
    }
    
    /**
     * Sets the value of the <code>authorBiography</code> feature of an
     * {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setAuthorBiography(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(AUTHOR_BIOGRAPHY);
      if (nodes.size() != 0) 
        meta.setAuthorBiography(nodes.get(0).getValue());
    }
    
    /**
     * Sets the value of the <code>banner</code> feature of an
     * {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setBanner(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(BANNER);
      if (nodes.size() != 0) 
        meta.setBanner(nodes.get(0).getValue());
    }
    
    /**
     * Sets the value of the <code>biographicalCategories</code> feature of an
     * {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setBiographicalCategories(
        final Document document,
        final ArticleMetadata meta,
        final JCas jCas) {
      final Nodes nodes = document.query(BIOGRAPHICAL_CATEGORIES);
      if (nodes.size() != 0) {
        final List<String> list = new ArrayList<String>();
        for (int i = 0; i < nodes.size(); ++i) {
          list.add(nodes.get(i).getValue());
        }
        meta.setBiographicalCategories(
            StringList.create(jCas, list.toArray(new String[list.size()])));
      }
    }
    
    /**
     * Sets the value of the <code>byline</code> feature of an
     * {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setByline(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(BYLINE);
      if (nodes.size() != 0) 
        meta.setByline(nodes.get(0).getValue());
    }
    
    /**
     * Sets the value of the <code>columnName</code> feature of an
     * {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setColumnName(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(COLUMN_NAME);
      if (nodes.size() != 0) 
        meta.setColumnName(nodes.get(0).getValue());
    }
    
    /**
     * Sets the value of the <code>columnNumber</code> feature of an
     * {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setColumnNumber(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(COLUMN_NUMBER);
      if (nodes.size() != 0) 
        meta.setColumnNumber(Integer.parseInt(nodes.get(0).getValue()));
    }
    
    /**
     * Sets the value of the <code>correctionDate</code> feature of an
     * {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setCorrectionDate(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(CORRECTION_DATE);
      if (nodes.size() != 0) 
        meta.setCorrectionDate(nodes.get(0).getValue());
    }
    
    /**
     * Sets the value of the <code>correctionText</code> feature of an
     * {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setCorrectionText(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(CORRECTION_TEXT);
      if (nodes.size() != 0) 
        meta.setCorrectionText(nodes.get(0).getValue());
    }
    
    /**
     * Sets the value of the <code>credit</code> feature of an
     * {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setCredit(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(CREDIT);
      if (nodes.size() != 0) 
        meta.setCredit(nodes.get(0).getValue());
    }
    
    /**
     * Sets the value of the <code>dateline</code> feature of an
     * {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setDateline(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(DATELINE);
      if (nodes.size() != 0) 
        meta.setDateline(nodes.get(0).getValue());
    }
    
    /**
     * Sets the value of the <code>dayOfWeek</code> feature of an
     * {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setDayOfWeek(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(DAY_OF_WEEK);
      if (nodes.size() != 0) 
        meta.setDayOfWeek(nodes.get(0).getValue());
    }
    
    /**
     * Sets the value of the <code>descriptors</code> feature of an
     * {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setDescriptors(
        final Document document,
        final ArticleMetadata meta,
        final JCas jCas) {
      final Nodes nodes = document.query(DESCRIPTORS);
      if (nodes.size() != 0) {
        final List<String> list = new ArrayList<String>();
        for (int i = 0; i < nodes.size(); ++i) {
          list.add(nodes.get(i).getValue());
        }
        meta.setDescriptors(
            StringList.create(jCas, list.toArray(new String[list.size()])));
      }
    }
    
    /**
     * Sets the value of the <code>featurePage</code> feature of an
     * {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setFeaturePage(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(FEATURE_PAGE);
      if (nodes.size() != 0) 
        meta.setFeaturePage(nodes.get(0).getValue());
    }
    
    /**
     * Sets the value of the <code>generalOnlineDescriptors</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setGeneralOnlineDescriptors(
        final Document document,
        final ArticleMetadata meta,
        final JCas jCas) {
      final Nodes nodes = document.query(GENERAL_ONLINE_DESCRIPTORS);
      if (nodes.size() != 0) {
        final List<String> list = new ArrayList<String>();
        for (int i = 0; i < nodes.size(); ++i) {
          list.add(nodes.get(i).getValue());
        }
        meta.setGeneralOnlineDescriptors(
            StringList.create(jCas, list.toArray(new String[list.size()])));
      }
    }
    
    /**
     * Sets the value of the <code>guid</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setGUID(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(GUID);
      if (nodes.size() != 0) 
        meta.setGuid(Long.parseLong(nodes.get(0).getValue()));
    }
    
    /**
     * Sets the value of the <code>headline</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setHeadline(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(HEADLINE);
      if (nodes.size() != 0) 
        meta.setHeadline(nodes.get(0).getValue());
    }
    
    /**
     * Sets the value of the <code>kicker</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setKicker(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(KICKER);
      if (nodes.size() != 0) 
        meta.setKicker(nodes.get(0).getValue());
    }
    
    /**
     * Sets the value of the <code>locations</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setLocations(
        final Document document,
        final ArticleMetadata meta,
        final JCas jCas) {
      final Nodes nodes = document.query(LOCATIONS);
      if (nodes.size() != 0) {
        final List<String> list = new ArrayList<String>();
        for (int i = 0; i < nodes.size(); ++i) {
          list.add(nodes.get(i).getValue());
        }
        meta.setLocations(
            StringList.create(jCas, list.toArray(new String[list.size()])));
      }
    }
    
    /**
     * Sets the value of the <code>names</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setNames(
        final Document document,
        final ArticleMetadata meta,
        final JCas jCas) {
      final Nodes nodes = document.query(NAMES);
      if (nodes.size() != 0) {
        final List<String> list = new ArrayList<String>();
        for (int i = 0; i < nodes.size(); ++i) {
          list.add(nodes.get(i).getValue());
        }
        meta.setNames(
            StringList.create(jCas, list.toArray(new String[list.size()])));
      }
    }
    
    /**
     * Sets the value of the <code>newsDesk</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setNewsDesk(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(NEWS_DESK);
      if (nodes.size() != 0) 
        meta.setNewsDesk(nodes.get(0).getValue());
    }
    
    /**
     * Sets the value of the <code>normalizedByline</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setNormalizedByline(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(NORMALIZED_BYLINE);
      if (nodes.size() != 0) 
        meta.setNormalizedByline(nodes.get(0).getValue());
    }
    
    /**
     * Sets the value of the <code>onlineDescriptors</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setOnlineDescriptors(
        final Document document,
        final ArticleMetadata meta,
        final JCas jCas) {
      final Nodes nodes = document.query(ONLINE_DESCRIPTORS);
      if (nodes.size() != 0) {
        final List<String> list = new ArrayList<String>();
        for (int i = 0; i < nodes.size(); ++i) {
          list.add(nodes.get(i).getValue());
        }
        meta.setOnlineDescriptors(
            StringList.create(jCas, list.toArray(new String[list.size()])));
      }
    }
    
    /**
     * Sets the value of the <code>onlineHeadline</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setOnlineHeadline(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(ONLINE_HEADLINE);
      if (nodes.size() != 0) 
        meta.setOnlineHeadline(nodes.get(0).getValue());
    }
    
    /**
     * Sets the value of the <code>onlineLocations</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setOnlineLocations(
        final Document document,
        final ArticleMetadata meta,
        final JCas jCas) {
      final Nodes nodes = document.query(ONLINE_LOCATIONS);
      if (nodes.size() != 0) {
        final List<String> list = new ArrayList<String>();
        for (int i = 0; i < nodes.size(); ++i) {
          list.add(nodes.get(i).getValue());
        }
        meta.setOnlineLocations(
            StringList.create(jCas, list.toArray(new String[list.size()])));
      }
    }
    
    /**
     * Sets the value of the <code>onlineOrganizations</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setOnlineOrganizations(
        final Document document,
        final ArticleMetadata meta,
        final JCas jCas) {
      final Nodes nodes = document.query(ONLINE_ORGANIZATIONS);
      if (nodes.size() != 0) {
        final List<String> list = new ArrayList<String>();
        for (int i = 0; i < nodes.size(); ++i) {
          list.add(nodes.get(i).getValue());
        }
        meta.setOnlineOrganizations(
            StringList.create(jCas, list.toArray(new String[list.size()])));
      }
    }

    /**
     * Sets the value of the <code>onlinePeople</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setOnlinePeople(
        final Document document,
        final ArticleMetadata meta,
        final JCas jCas) {
      final Nodes nodes = document.query(ONLINE_PEOPLE);
      if (nodes.size() != 0) {
        final List<String> list = new ArrayList<String>();
        for (int i = 0; i < nodes.size(); ++i) {
          list.add(nodes.get(i).getValue());
        }
        meta.setOnlinePeople(
            StringList.create(jCas, list.toArray(new String[list.size()])));
      }
    }   
    
    /**
     * Sets the value of the <code>onlineSection</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setOnlineSection(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(ONLINE_SECTION);
      if (nodes.size() != 0) 
        meta.setOnlineSection(nodes.get(0).getValue());
    } 
    
    /**
     * Sets the value of the <code>onlineTitles</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setOnlineTitles(
        final Document document,
        final ArticleMetadata meta,
        final JCas jCas) {
      final Nodes nodes = document.query(ONLINE_TITLES);
      if (nodes.size() != 0) {
        final List<String> list = new ArrayList<String>();
        for (int i = 0; i < nodes.size(); ++i) {
          list.add(nodes.get(i).getValue());
        }
        meta.setOnlineTitles(
            StringList.create(jCas, list.toArray(new String[list.size()])));
      }
    }   
    
    /**
     * Sets the value of the <code>organizations</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setOrganizations(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(ORGANIZATIONS);
      if (nodes.size() != 0) 
        meta.setOrganizations(nodes.get(0).getValue());
    } 

    /**
     * Sets the value of the <code>page</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setPage(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(PAGE);
      if (nodes.size() != 0) 
        meta.setPage(Integer.parseInt(nodes.get(0).getValue()));
    } 
    
    /**
     * Sets the value of the <code>people</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setPeople(
        final Document document,
        final ArticleMetadata meta,
        final JCas jCas) {
      final Nodes nodes = document.query(PEOPLE);
      if (nodes.size() != 0) {
        final List<String> list = new ArrayList<String>();
        for (int i = 0; i < nodes.size(); ++i) {
          list.add(nodes.get(i).getValue());
        }
        meta.setPeople(
            StringList.create(jCas, list.toArray(new String[list.size()])));
      }
    }   
    
    /**
     * Sets the value of the <code>publicationDate</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setPublicationDate(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(PUBLICATION_DATE);
      if (nodes.size() != 0) 
        meta.setPublicationDate(nodes.get(0).getValue());
    } 
    
    /**
     * Sets the value of the <code>section</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setSection(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(SECTION);
      if (nodes.size() != 0) 
        meta.setSection(nodes.get(0).getValue());
    } 
    
    /**
     * Sets the value of the <code>seriesName</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setSeriesName(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(SERIES_NAME);
      if (nodes.size() != 0) 
        meta.setSeriesName(nodes.get(0).getValue());
    } 
    
    /**
     * Sets the value of the <code>slug</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setSlug(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(SLUG);
      if (nodes.size() != 0) 
        meta.setSlug(nodes.get(0).getValue());
    } 

    /**
     * Sets the value of the <code>taxonomicClassifiers</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setTaxonomicClassifiers(
        final Document document,
        final ArticleMetadata meta,
        final JCas jCas) {
      final Nodes nodes = document.query(TAXONOMIC_CLASSIFIERS);
      if (nodes.size() != 0) {
        final List<String> list = new ArrayList<String>();
        for (int i = 0; i < nodes.size(); ++i) {
          list.add(nodes.get(i).getValue());
        }
        meta.setTaxonomicClassifiers(
            StringList.create(jCas, list.toArray(new String[list.size()])));
      }
    }   
   
    /**
     * Sets the value of the <code>titles</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setTitles(
        final Document document,
        final ArticleMetadata meta,
        final JCas jCas) {
      final Nodes nodes = document.query(TITLES);
      if (nodes.size() != 0) {
        final List<String> list = new ArrayList<String>();
        for (int i = 0; i < nodes.size(); ++i) {
          list.add(nodes.get(i).getValue());
        }
        meta.setTitles(
            StringList.create(jCas, list.toArray(new String[list.size()])));
      }
    }  
    
    /**
     * Sets the value of the <code>typesOfMaterial</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setTypesOfMaterial(
        final Document document,
        final ArticleMetadata meta,
        final JCas jCas) {
      final Nodes nodes = document.query(TYPES_OF_MATERIAL);
      if (nodes.size() != 0) {
        final List<String> list = new ArrayList<String>();
        for (int i = 0; i < nodes.size(); ++i) {
          list.add(nodes.get(i).getValue());
        }
        meta.setTypesOfMaterial(
            StringList.create(jCas, list.toArray(new String[list.size()])));
      }
    }  
    
    /**
     * Sets the value of the <code>url</code> feature
     * of an {@link ArticleMetadata} annotation for one document.
     * 
     * @param document The document
     * @param meta The ArticleMetadata annotation
     */
    private static void setUrl(
        final Document document, final ArticleMetadata meta) {
      final Nodes nodes = document.query(URL);
      if (nodes.size() != 0) 
        meta.setUrl(nodes.get(0).getValue());
    } 
    
    /**
     * Return all the paragraphs in one document as a list.
     * <p>
     * This method is used by
     * {@link NyTimesReader#addDocumentText(JCas, Document)}
     * to add the document text of a JCas with {@link Paragraph}
     * annotations.
     * </p>
     * 
     * @param document The document
     * @return The list of paragraphs
     */
    private static List<String> getParagraphs(final Document document) {
      final Nodes nodes = document.query(FULL_TEXT);
      final List<String> paragraphs = new ArrayList<String>();
      for (int i = 0; i < nodes.size(); ++i) {
        paragraphs.add(nodes.get(i).getValue());
      }
      return paragraphs;
    }
    
    /**
     * Returns the GUID of a document.
     * <p>
     * The return value of this method is used by
     * {@link NyTimesReader#addSourceDocumentInformation(JCas, Document)}
     * together with {@link NyTimesReader#CORPUS_NAME} to set the name
     * of a document in the {@link SourceDocumentInformation}.
     * </p>
     * 
     * @param document The document
     * @return The GUID of a document or null, if the document contains no
     *         GUID, which is never the case for a document of the nytimes-
     *         corpus
     */
    private static String getGUID(final Document document) {
      final Nodes nodes = document.query(GUID);
      if (nodes.size() != 0) 
        return nodes.get(0).getValue();
      return null;
    } 
  }
  
}
