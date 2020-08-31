package de.webis.listenability.features.unit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.aitools.commons.uima.core.Token;
import de.aitools.commons.uima.pipeline.AnalysisEngineComponent;
import de.webis.writing.Scores;
import de.webis.writing.types.Score;
import de.webis.writing.types.ScoredUnit;

/**
 * TODO finalize documentation
 * 
 * @author lukas.peter.trautner@uni-weimar.de
 *
 */
public class Kuperman12AnalysisEngine extends AnalysisEngineComponent {
  
  // -------------------------------------------------------------------------
  // LOGGING
  // -------------------------------------------------------------------------
  
  private static final Logger LOG =
      Logger.getLogger(Kuperman12AnalysisEngine.class.getName());
  
  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------

  /**
   * Configuration option specifying the path to the lexicon.
   * <p>
   * The lexicon must be a csv file, where each row represents a word and each
   * column a different feature of that word.
   * </p>
   */
  private static final String PROPERTY_LEXICON_PATH = "lexicon";
  
  /**
   * Default value {@link #PROPERTY_LEXICON_PATH}.
   */
  private static final String DEFAULT_LEXICON_PATH =
      "src/main/resources/de/webis/lexicon/english-lexicon-project.csv";
  
  /**
   * Configuration option specifying the name of the column in the csv,
   * specified by {@link #PROPERTY_LEXICON_PATH}, that contains the
   * words.
   */
  private static final String PROPERTY_WORD_COLUMN_NAME = "wordColumn";
  
  /**
   * Default value for {@link #PROPERTY_WORD_COLUMN_NAME}.
   */
  private static final String DEFAULT_WORD_COLUMN_NAME = "Word";
  
  /**
   * Configuration option specifying mappings from feature (column) names,
   * as they appear in the csv file, to the name of the feature as it
   * should appear in the CAS.
   * <p>
   * This option also allows to exclude certain features from the csv,
   * by excluding them from the mapping.
   * </p><p>
   * The value of this option must be parseable by
   * {@link #parseFeatureMappings(String)}, therefore use the format as
   * in {@link #DEFAULT_FEATURE_MAPPING}. 
   * </p>
   */
  private static final String PROPERTY_FEATURE_MAPPING = "mapping";
  
  /**
   * Default value for {@link #PROPERTY_FEATURE_MAPPING}.
   * <p>
   * The format is 'nameInCsv1:nameInCas1,nameInCsv2:nameInCas2,...'.
   * </p>
   */
  private static final String DEFAULT_FEATURE_MAPPING =
      "Length"      + ":" +   "OrthographicLength,"               +
      "Freq_HAL"    + ":" +   "WordFrequency,"                    +
      "Ortho_N"     + ":" +   "OrthographicNeighbors,"            +
      "Phono_N"     + ":" +   "PhonologicalNeighbors,"            +
      "Phono_N_H"   + ":" +   "PhonologicalNeighborsNH,"          +
      "OG_N"        + ":" +   "PhonographicNeighbors,"            +
      "OG_N_H"      + ":" +   "PhonographicNeighborsNH,"          +
      "Freq_N"      + ":" +   "FreqOrthographicNeighbors,"        +
      "Freq_N_P"    + ":" +   "FreqPhonologicalNeighbors,"        +
      "Freq_N_PH"   + ":" +   "FreqPhonologicalNeighborsNH,"      +
      "Freq_N_OG"   + ":" +   "FreqPhonographicNeighbors,"        +
      "Freq_N_OGH"  + ":" +   "FreqPhonographicNeighborsNH,"      +
      "OLD"         + ":" +   "OLD20,"                            +
      "PLD"         + ":" +   "PLD20,"                            +
      "BG_Mean"     + ":" +   "MeanBigramFrequencies,"            +
      "BG_Sum"      + ":" +   "SumBigramFrequencies,"             +
      "NSyll"       + ":" +   "SyllableCount,"                    +
      "NMorph"      + ":" +   "MorphemeCount,"                    +
      "NPhon"       + ":" +   "PhonemesCount";
  
  // -------------------------------------------------------------------------
  // MEMBERS
  // -------------------------------------------------------------------------
  
  /**
   * 
   */
   private final Map<String, Map<String, Double>> lexicon;
  
  // -------------------------------------------------------------------------
  // CONSTRUCTORS
  // -------------------------------------------------------------------------
   
   /**
    * Creates a new analysis engine.
    */
   public Kuperman12AnalysisEngine() {
     this.lexicon = new HashMap<String, Map<String, Double>>();
   }
  
  // -------------------------------------------------------------------------
  // GETTERS
  // -------------------------------------------------------------------------
   
   /**
    * Gets the current lexicon.
    * 
    * @return the lexicon
    * 
    * @see #lexicon
    */
   public Map<String, Map<String, Double>> getLexicon() {
     return this.lexicon;
   }
  
  // -------------------------------------------------------------------------
  // CONFIGURATION
  // -------------------------------------------------------------------------

  @Override
  public void configure(final Properties properties) {
    try {
      final String lexiconPath =
          properties.getProperty(
              PROPERTY_LEXICON_PATH, DEFAULT_LEXICON_PATH);
      final String featureMappingsString =
          properties.getProperty(
              PROPERTY_FEATURE_MAPPING, DEFAULT_FEATURE_MAPPING);
      final String wordColumnName =
          properties.getProperty(
              PROPERTY_WORD_COLUMN_NAME, DEFAULT_WORD_COLUMN_NAME);
      final Map<String, String> featureMappings =
          this.parseFeatureMappings(featureMappingsString);
      this.buildLexiconFromFile(
          lexiconPath, wordColumnName, featureMappings);
    } catch (IOException e) {
      LOG.severe(e.toString());
      throw new UncheckedIOException(e);
    }
  }
  
  // -------------------------------------------------------------------------
  // FUNCTIONALITY
  // -------------------------------------------------------------------------

  @Override
  public void accept(final JCas jCas) {
    final FSIterator<Annotation> iterator = 
        jCas.getAnnotationIndex(Token.type).iterator();
    while (iterator.hasNext()) {
      final Token token = (Token) iterator.next();
      final String text = token.getCoveredText().toLowerCase();
      final Map<String, Double> features =
          this.getLexicon().getOrDefault(text, null);
      if (features == null)
        continue;
      this.addFeatures(token, features);
    }
  }
  
  /**
   * Adds all features for a {@link Token}, present in the lexicon, as
   * a {@link Score} to the JCas, by calling
   * {@link Scores#add(Unit, String, double)}.
   * 
   * @param token     The token for which the features are added  
   * @param features  The features to add
   * 
   * @see Score
   * @see ScoredUnit
   * @see Scores
   */
  private void addFeatures(
      final Token token,
      final Map<String, Double> features) {
    for (final String feature : features.keySet()) {
      Scores.add(token, feature, features.get(feature));
    }
  }

  // -------------------------------------------------------------------------
  // HELPERS
  // -------------------------------------------------------------------------
  
  /**
   * 
   * 
   * @param mappings
   * @return
   * 
   * @see #PROPERTY_FEATURE_MAPPING
   * @see #DEFAULT_FEATURE_MAPPING
   */
  private Map<String, String> parseFeatureMappings(final String mappings) {
    Map<String, String> parsed = new HashMap<String, String>();
    for (final String mapping : mappings.split(",")) {
      if (mapping.contains(":") && mapping.split(":").length == 2) {
        parsed.put(mapping.split(":")[0], mapping.split(":")[1]);
      } else {
        LOG.warning("Found malformed mapping at '" + mapping + "'");
      }
    }
    return parsed;
  }
  
  /**
   * Reads the lexicon containing the feature values from a csv file, specified
   * by <code>lexiconPath</code>.
   * 
   * @param lexiconPath The path to the csv containing the lexicon
   * @param featureMappings The mapping from feature names in the csv file
   *                        to the feature names as they will appear in CAS
   * @throws IOException If the lexicon could not be read
   * 
   * @see #addEntryToLexicon(CSVRecord, String, Map)
   * @see #addFeatureToEntry(CSVRecord, Map, String, String)
   */
  private void buildLexiconFromFile(
      final String lexiconPath,
      final String wordColumnName,
      final Map<String, String> featureMappings) throws IOException {
    final BufferedReader reader =
        new BufferedReader(new FileReader(lexiconPath));
    final Iterable<CSVRecord> records =
        CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
    for (final CSVRecord record : records) {
      this.addEntryToLexicon(record, wordColumnName, featureMappings);
    }
  }
  
  /**
   * 
   * @param record
   * @param featureMappings
   */
  private void addEntryToLexicon(
      final CSVRecord record,
      final String wordColumnName,
      final Map<String, String> featureMappings) {
    final String word = record.get(wordColumnName).toLowerCase();
    final Map<String, Double> lexiconEntry = new HashMap<String, Double>();
    for (final String columnName : featureMappings.keySet()) {
      final String featureName = featureMappings.get(columnName);
      this.addFeatureToEntry(record, lexiconEntry, columnName, featureName);
    }
    this.getLexicon().put(word, lexiconEntry);
  }
  
  /**
   * 
   * @param record
   * @param lexiconEntry
   * @param columnName
   * @param featureName
   */
  private void addFeatureToEntry(
      final CSVRecord record,
      final Map<String, Double> lexiconEntry,
      final String columnName,
      final String featureName) {
    final String value = record.get(columnName);
    if (value.equals("") || value == null)
      return;
    lexiconEntry.put(featureName, Double.parseDouble(value));
  }
  
}
