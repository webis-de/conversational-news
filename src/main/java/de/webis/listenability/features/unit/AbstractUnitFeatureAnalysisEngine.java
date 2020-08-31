package de.webis.listenability.features.unit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import de.aitools.commons.uima.core.Document;
import de.aitools.commons.uima.core.Paragraph;
import de.aitools.commons.uima.core.Sentence;
import de.aitools.commons.uima.core.Token;
import de.aitools.commons.uima.pipeline.AnalysisEngineComponent;
import de.aitools.commons.uima.supertype.Unit;
import de.webis.listenability.features.unit.features.Feature;

/**
 * TODO documentation
 * 
 * @author lukas.peter.trautner@uni-weimar.de
 *
 */
public abstract class AbstractUnitFeatureAnalysisEngine
  extends AnalysisEngineComponent {
  
  // -------------------------------------------------------------------------
  // LOGGING
  // -------------------------------------------------------------------------
  
  private static final Logger LOG =
      Logger.getLogger(AbstractUnitFeatureAnalysisEngine.class.getName());
  
  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------
  
  /**
   * 
   */
  private static final String PROPERTY_FEATURE_LEVEL = ".level";
  
  /**
   * 
   */
  private static final String DEFAULT_FEATURE_VALUE = "true";
  
  /**
   * 
   */
  private static final String DEFAULT_FEATURE_LEVEL = "document";
  

  // -------------------------------------------------------------------------
  // MEMBERS
  // -------------------------------------------------------------------------
  
  /**
   * 
   */
  private final Map<Feature, Set<UnitLevel>> features;
  
  // -------------------------------------------------------------------------
  // CONSTRUCTORS
  // -------------------------------------------------------------------------
  
  /**
   * 
   */
  public AbstractUnitFeatureAnalysisEngine() {
    this.features = new HashMap<Feature, Set<UnitLevel>>();
  }
  
  // -------------------------------------------------------------------------
  // FUNCTIONALITY
  // -------------------------------------------------------------------------
  
  
  @Override
  public final void accept(final JCas jCas) {
    for (final Feature feature : this.features.keySet()) {
      for (final UnitLevel level : this.features.get(feature)) {
        if (level == UnitLevel.DOCUMENT) {
          this.computeFeatureValue(
              jCas, feature, Document.type);
        } else if (level == UnitLevel.PARAGRAPH) {
           this.computeFeatureValue(
               jCas, feature, Paragraph.type);
        } else if (level == UnitLevel.SENTENCE){
          this.computeFeatureValue(
              jCas, feature, Sentence.type);
        } else  if (level == UnitLevel.TOKEN){
          this.computeFeatureValue(
              jCas, feature, Token.type);
        }
      }
    }
  }
  
  /**
   * 
   * @param jCas
   * @param feature
   * @param level
   */
  private void computeFeatureValue(
      final JCas jCas,
      final Feature feature,
      final int level) {
    final FSIterator<Annotation> iterator = 
        jCas.getAnnotationIndex(level).iterator();
    while (iterator.hasNext()){
      final Unit unit = (Unit) iterator.next();
      feature.compute(unit);
    }
  }

  // -------------------------------------------------------------------------
  // HELPERS
  // -------------------------------------------------------------------------
  
  /**
   * 
   * @param properties
   * @param featureClass
   */
  protected void addFeature(
      final Properties properties,
      final Class<? extends Feature> featureClass
      ) {
    if (Boolean.parseBoolean(properties.getProperty(
        featureClass.getSimpleName(), DEFAULT_FEATURE_VALUE))) {
      try {
        final Feature feature = (Feature) Class.forName(
            featureClass.getName()).getConstructor().newInstance();
        final Set<UnitLevel> levels =  this.parseLevels(properties.getProperty(
            featureClass.getSimpleName() + PROPERTY_FEATURE_LEVEL,
            DEFAULT_FEATURE_LEVEL));
        if (feature.check(levels)) {
          this.features.put(feature, levels);
        } else {
          LOG.warning(
              "Failed to add feature '" + featureClass.getName()
              + "'\n\tThe specified levels are not applicable to"
              + "this feature.");
        }
      } catch (ReflectiveOperationException e) {
        LOG.warning(
            "Failed to add feature '" + featureClass.getName() + "'\n\t" + e);
      }
      LOG.fine("Successfully added feature " + featureClass.getName());
    }
  }
  
  /**
   * 
   * @param levels
   * @return
   */
  protected final Set<UnitLevel> parseLevels(final String levels) {
    final String[] s = levels.split(",");
    final Set<UnitLevel> parsed = new HashSet<UnitLevel>();
    for (int i = 0; i < s.length; ++i) {
      final String current = s[i];
      if (current.equals("document")) {
        parsed.add(UnitLevel.DOCUMENT);
      } else if (current.equals("paragraph")) {
        parsed.add(UnitLevel.PARAGRAPH);
      } else if (current.equals("sentence")) {
        parsed.add(UnitLevel.SENTENCE);
      } else if (current.equals("token")) {
        parsed.add(UnitLevel.TOKEN);
      } else {
        LOG.warning(
            "Found level '" + current + "'. Only 'document', 'paragraph', "
            + "'sentence' and 'token' are allowed.");
        continue;
      }
    }
    return parsed;
  }
  
}
