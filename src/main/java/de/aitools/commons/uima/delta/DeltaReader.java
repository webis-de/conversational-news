package de.aitools.commons.uima.delta;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Supplier;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.FsIndexDescription;
import org.apache.uima.util.CasCreationUtils;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import de.aitools.commons.io.Bindings;
import de.aitools.commons.io.deserializer.Deserializer;
import de.aitools.commons.io.deserializer.Deserializer.Factory;
import de.aitools.commons.io.deserializer.EnumerationNameSupplier;
import de.aitools.commons.io.deserializer.SingleElementDeserializer;
import de.aitools.commons.uima.delta.types.Delta;
import de.aitools.commons.uima.standard.DeserializerReader;
import de.aitools.commons.uima.standard.PlainTextReader;

/**
 * A collection reader that reads existing CAS plus text modifications. 
 * 
 * @author johannes.kiesel@uni-weimar.de
 *
 */
public class DeltaReader extends DeserializerReader<DeltaReader.Source> {
  
  // -------------------------------------------------------------------------
  // CONSTRUCTOR
  // -------------------------------------------------------------------------

  /**
   * Creates a new reader.
   */
  public DeltaReader() { }
  
  // -------------------------------------------------------------------------
  // HELPERS
  // -------------------------------------------------------------------------

  @Override
  protected Factory<Source> getFactory(final Properties properties) {
    return new JsonDeserializerFactory();
  }

  @Override
  protected void setCas(
      final CAS cas, final Source source, final String name) {
    try {
      final JCas original = this.createOriginalCas(cas.getTypeSystem(), source);
      final String newDocumentText = this.getNewDocumentText(
          source, original.getDocumentText());
      PlainTextReader.setText(cas, newDocumentText, name);

      final JCas jCas = cas.getJCas();
      this.annotate(jCas, original, source.getDeltas());
    } catch (final CASException e) {
      throw new RuntimeException(e); // no longer thrown according to javadoc
    }
  }

  /**
   * Creates the CAS that contains the original data.
   * @param typeSystem The type system to employ
   * @param source The source for the data
   * @return The CAS, set up with the original data
   */
  protected JCas createOriginalCas(
      final TypeSystem typeSystem, final Source source) {
    try {
      final CAS cas = CasCreationUtils.createCas(
          typeSystem, null, new FsIndexDescription[0], null);
      source.getCas(cas);
      return cas.getJCas();
    } catch (final ResourceInitializationException | CASException e) {
      throw new RuntimeException(e); // should never happen
    } catch (final SAXException e) {
      throw new IllegalArgumentException("Invalid XMI", e);
    }
  }

  /**
   * Gets the document text after modifications.
   * @param source The source for the deltas
   * @param originalDocumentText The document text before modifications
   * @return The document text after modifications
   */
  protected String getNewDocumentText(
      final Source source, final String originalDocumentText) {
    final StringBuilder newDocumentText = new StringBuilder();

    int end = 0;
    for (final InputDelta inputDelta : source.getDeltas()) {
      final int begin = inputDelta.getBegin();
      if (begin > end) {
        newDocumentText.append(originalDocumentText.substring(end, begin));
      }

      end = inputDelta.getEnd();
      newDocumentText.append(inputDelta.getNewText());
    }
    if (end < originalDocumentText.length()) {
      newDocumentText.append(originalDocumentText.substring(end));
    }

    return newDocumentText.toString();
  }

  /**
   * Adds delta annotations for the modifications and moves annotations from
   * the original where there were no modifications.
   * @param jCas The target
   * @param original The original CAS
   * @param inputDeltas The deltas on the original CAS
   */
  protected void annotate(
      final JCas jCas, final JCas original,
      final List<InputDelta> inputDeltas) {
    final String originalDocumentText = original.getDocumentText();
    int originalEnd = 0;
    int shift = 0;
    for (final InputDelta inputDelta : inputDeltas) {
      final int originalBegin = inputDelta.getBegin();
      if (originalBegin > originalEnd) {
        this.moveAnnotations(jCas, original, originalEnd, originalBegin, shift);
      }

      originalEnd = inputDelta.getEnd();
      this.addDeltaAnnotations(
          jCas, inputDelta, shift, originalDocumentText);
      shift += (originalEnd - originalBegin) - inputDelta.getNewText().length();
    }
    if (originalEnd < originalDocumentText.length()) {
      this.moveAnnotations(
          jCas, original, originalEnd, originalDocumentText.length(), shift);
    }
  }

  /**
   * Moves annotations from the original to the target within the specified
   * bound.
   * @param jCas The target
   * @param original The original CAS
   * @param originalBegin Begin index of the region for which the covered
   * annotations are moved to the target
   * @param originalEnd End index (exclusive) of the region for which the
   * covered annotations are moved to the target
   * @param shift Value added to the annotations begin and end when moving them
   * to the target
   */
  protected void moveAnnotations(
      final JCas jCas, final JCas original,
      final int originalBegin, final int originalEnd, final int shift) {
    final Iterator<Annotation> coveredAnnotations =
        original.getAnnotationIndex().select()
        .coveredBy(originalBegin, originalEnd).iterator();
    while (coveredAnnotations.hasNext()) {
      final Annotation annotation = coveredAnnotations.next();
      annotation.removeFromIndexes(original);
      annotation.setBegin(annotation.getBegin() + shift);
      annotation.setEnd(annotation.getEnd() + shift);
      annotation.addToIndexes(jCas);
    }
  }

  /**
   * Creates delta annotations to indicate the regions of modification.
   * @param jCas The target
   * @param inputDelta The input delta
   * @param shift Value added to the annotations begin and end when moving them
   * to the target
   * @param originalDocumentText The document text before modifications
   */
  protected void addDeltaAnnotations(
      final JCas jCas, final InputDelta inputDelta,
      final int shift, final String originalDocumentText) {
    final int originalBegin = inputDelta.getBegin();
    final int originalEnd = inputDelta.getEnd();

    final Delta delta =
        new Delta(jCas, originalBegin + shift, originalEnd + shift);
    delta.setOld(originalDocumentText.substring(originalBegin, originalEnd));
    delta.addToIndexes();
  }
  
  // -------------------------------------------------------------------------
  // DESERIALIZER
  // -------------------------------------------------------------------------

  /**
   * Deserializer for one {@link Source} object. 
   * 
   * @author johannes.kiesel@uni-weimar.de
   *
   */
  public static class JsonDeserializer
  extends SingleElementDeserializer<Source> {

    /**
     * Creates a new deserializer.
     * @param input The JSON of the source as a stream
     */
    protected JsonDeserializer(final InputStream input) {
      super(input);
    }

    /**
     * Creates a new deserializer.
     * @param name A name for the element
     * @param input The JSON of the source as a stream
     */
    protected JsonDeserializer(final String name, final InputStream input) {
      super(name, input);
    }

    @Override
    protected Source parse(final InputStream input) throws Exception {
      return Bindings.JSON_MAPPER.readValue(input, Source.class);
    }

  }
  
  // -------------------------------------------------------------------------
  // DESERIALIZER FACTORY
  // -------------------------------------------------------------------------

  /**
   * Default factory for the delta reader. 
   * 
   * @author johannes.kiesel@uni-weimar.de
   *
   */
  public static class JsonDeserializerFactory
  extends Deserializer.Factory<Source> {
    
    // -----------------------------------------------------------------------
    // MEMBERS
    // -----------------------------------------------------------------------
    
    private final Supplier<String> nameSupplier;
    
    // -----------------------------------------------------------------------
    // CONSTRUCTION
    // -----------------------------------------------------------------------

    /**
     * Creates a new factory.
     * <p>
     * Uses an {@link EnumerationNameSupplier}.
     * </p>
     */
    public JsonDeserializerFactory() {
      this(new EnumerationNameSupplier());
    }

    /**
     * Creates a new factory.
     * @param nameSupplier The supplier that gives a name to each deserialzed
     * source object
     */
    public JsonDeserializerFactory(final Supplier<String> nameSupplier) {
      this.nameSupplier = Objects.requireNonNull(nameSupplier);
    }
    
    // -----------------------------------------------------------------------
    // GETTERS
    // -----------------------------------------------------------------------

    /**
     * Gets the supplier for names.
     * <p>
     * This supplier is used to get a name for each new deserializer.
     * </p>
     * @return The supplier
     * @see #getName()
     */
    protected Supplier<String> getNameSupplier() {
      return this.nameSupplier;
    }

    /**
     * Gets the next name for the next deserializer.
     * @return The next name
     * @see #getNameSupplier()
     */
    protected String getName() {
      final Supplier<String> nameSupplier = this.getNameSupplier();
      synchronized (nameSupplier) {
        return nameSupplier.get();
      }
    }
    
    // -----------------------------------------------------------------------
    // FUNCTIONALITY
    // -----------------------------------------------------------------------

    @Override
    protected boolean isValidUncompressedFile(final String filename) {
      return filename.toLowerCase().endsWith(".json");
    }

    @Override
    public Deserializer<Source> build(final InputStream inputStream)
    throws IOException {
      return new JsonDeserializer(this.getName(), inputStream);
    }
    
  }
 
  // -------------------------------------------------------------------------
  // SOURCE
  // -------------------------------------------------------------------------

  /**
   * The source object for the delta reader. 
   * 
   * @author johannes.kiesel@uni-weimar.de
   *
   */
  @JsonAutoDetect(
      getterVisibility = Visibility.NONE,
      setterVisibility = Visibility.NONE)
  public static class Source {
    
    // -------------------------------------------------------------------------
    // CONSTANTS
    // -------------------------------------------------------------------------
    
    /**
     * JSON key for the original CAS object to be modified in XMI format.
     */
    public static final String JSON_XMI = "xmi";
    
    /**
     * JSON key for the modifications on the original sofa text.
     */
    public static final String JSON_DELTAS = "deltas";
    
    /**
     * JSON key for an identifier or representation of the state of the document
     * text after the modifications.
     */
    public static final String JSON_STATE = "state";
    
    // -------------------------------------------------------------------------
    // MEMBERS
    // -------------------------------------------------------------------------

    private final String xmi;

    private final List<InputDelta> deltas;

    private final String state;

    // -------------------------------------------------------------------------
    // CONSTRUCTORS
    // -------------------------------------------------------------------------

    /**
     * Creates a new source object.
     * @param xmi The original CAS object to be modified in XMI format
     * @param deltas The modifications on the original sofa text
     * @param state An identifier or representation of the state of the document
     * text after the modifications
     */
    public Source(
        final String xmi,
        final Collection<? extends InputDelta> deltas,
        final String state) {
      this.xmi = Objects.requireNonNull(xmi);
      final List<InputDelta> deltasCopy = new ArrayList<>(deltas);
      deltasCopy.sort(null);
      this.deltas = Collections.unmodifiableList(deltasCopy);
      this.state = state;
    }

    /**
     * Creates a new source object.
     * @param xmi The original CAS object to be modified in XMI format
     * @param deltas The modifications on the original sofa text
     * @param state An identifier or representation of the state of the document
     * text after the modifications
     */
    @JsonCreator
    public Source(
        @JsonProperty(JSON_XMI) final String xmi,
        @JsonProperty(JSON_DELTAS) final Collection<? extends InputDelta> deltas,
        @JsonProperty(JSON_STATE) final JsonNode state) {
      this(xmi, deltas, Bindings.toJson(state));
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    /**
     * Gets the original CAS object to be modified in XMI format.
     * @return The XMI string
     */
    @JsonProperty(JSON_XMI)
    public String getXmi() {
      return this.xmi;
    }

    /**
     * Gets the modifications on the original sofa text.
     * @return The modifications as input deltas
     */
    @JsonProperty(JSON_DELTAS)
    public List<InputDelta> getDeltas() {
      return this.deltas;
    }

    /**
     * Gets the identifier or representation of the state of the document
     * text after the modifications.
     * @return The identifier or representation as string
     * @see #getStateNode()
     */
    public String getState() {
      return this.state;
    }

    // -------------------------------------------------------------------------
    // FUNCTIONALITY
    // -------------------------------------------------------------------------

    /**
     * Gets the identifier or representation of the state of the document
     * text after the modifications.
     * @return The identifier or representation as JSON node
     * @see #getState()
     */
    @JsonProperty(JSON_STATE)
    public JsonNode getStateNode() {
      try {
        return Bindings.fromJson(this.getState(), JsonNode.class);
      } catch (final IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    /**
     * Gets the original CAS object to be modified.
     * @param cas The CAS object that should take on the original text
     * @throws SAXException If the string returned by {@link #getXmi()} is not
     * valid
     */
    public void getCas(final CAS cas)
    throws SAXException {
      final ByteArrayInputStream stream =
          new ByteArrayInputStream(this.getXmi().getBytes());
      try {
        XmiCasDeserializer.deserialize(stream, cas);
      } catch (final IOException e) {
        // should not be possible for reading from an byte array input stream
        throw new RuntimeException(e);
      }
    }

  }
  
  // -------------------------------------------------------------------------
  // INPUT DELTA
  // -------------------------------------------------------------------------

  /**
   * Input representation of a text modification. 
   * 
   * @author johannes.kiesel@uni-weimar.de
   *
   */
  @JsonAutoDetect(
      getterVisibility = Visibility.NONE,
      setterVisibility = Visibility.NONE)
  public static class InputDelta implements Comparable<InputDelta> {
    
    // -------------------------------------------------------------------------
    // CONSTANTS
    // -------------------------------------------------------------------------
    
    /**
     * JSON key for the character offset in the original text at which the
     * replacements start (inclusive).
     */
    public static final String JSON_BEGIN = "begin";
    
    /**
     * JSON key for the character offset in the original text at which the
     * replacements ends (exclusive).
     */
    public static final String JSON_END = "end";
    
    /**
     * JSON key for the text that replaces the part of the original text that is
     * defined by {@link #JSON_BEGIN} and {@link #JSON_END}.
     */
    public static final String JSON_NEW_TEXT = "new";
    
    // -------------------------------------------------------------------------
    // MEMBERS
    // -------------------------------------------------------------------------

    private final int begin;

    private final int end;

    private final String newText;

    // -------------------------------------------------------------------------
    // CONSTRUCTORS
    // -------------------------------------------------------------------------

    /**
     * Creates a new delta.
     * @param begin The character offset in the original text at which the
     * replacements start (inclusive)
     * @param end The character offset in the original text at which the
     * replacements ends (exclusive)
     * @param newText The text that replaces the part of the original text that
     * is defined by begin and end
     */
    @JsonCreator
    public InputDelta(
        @JsonProperty(JSON_BEGIN) final int begin,
        @JsonProperty(JSON_END) final int end,
        @JsonProperty(JSON_NEW_TEXT) final String newText) {
      this.begin = begin;
      this.end = end;
      this.newText = Objects.requireNonNull(newText);
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    /**
     * Gets the character offset in the original text at which the
     * replacements start (inclusive).
     * @return The character offset
     */
    @JsonProperty(JSON_BEGIN)
    public int getBegin() {
      return this.begin;
    }

    /**
     * Gets the character offset in the original text at which the
     * replacements ends (exclusive).
     * @return The character offset
     */
    @JsonProperty(JSON_END)
    public int getEnd() {
      return this.end;
    }

    /**
     * Gets the text that replaces the part of the original text that is
     * defined by {@link #getBegin()} and {@link #getEnd()}.
     * @return The replacement text
     */
    @JsonProperty(JSON_NEW_TEXT)
    public String getNewText() {
      return this.newText;
    }

    @Override
    public int compareTo(final InputDelta other) {
      return Integer.compare(this.getBegin(), other.getBegin());
    }
    
  }

}
