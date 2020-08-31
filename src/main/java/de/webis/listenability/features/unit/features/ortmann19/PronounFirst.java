package de.webis.listenability.features.unit.features.ortmann19;

import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;

import de.aitools.commons.uima.core.Document;
import de.aitools.commons.uima.core.Paragraph;
import de.aitools.commons.uima.core.Sentence;
import de.aitools.commons.uima.core.Token;
import de.aitools.commons.uima.supertype.Unit;
import de.webis.listenability.features.unit.AbstractUnitFeatureAnalysisEngine;
import de.webis.listenability.features.unit.UnitLevel;
import de.webis.listenability.features.unit.features.AbstractFeature;
import de.webis.listenability.features.unit.features.Feature;

/**
 * This class implements the feature called "PRON1st" in Ortmann19.
 * <p>
 * It computes the ratio of first person singular and plural personal
 * pronouns, i.e. tokens tagged as "PRP", with one of the lemmas "I" or "we",
 * to all words, excluding punctuation, in a span of text captured by a
 * {@link Unit}-annotation.
 * <p>
 * This feature can be computed for following {@link Unit}s:
 * <ul>
 *  <li>{@link Document}</li>
 *  <li>{@link Paragraph}</li>
 *  <li>{@link Sentence}</li>
 * </ul>
 * 
 * @author lukas.peter.trautner@uni-weimar.de
 * 
 * @see {@link Feature}
 * @see {@link AbstractFeature}
 * @see {@link AbstractUnitFeatureAnalysisEngine}
 *
 */
public class PronounFirst extends AbstractFeature {

  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------
  
  /**
   * The name of this feature.
   */
  private static final String FEATURE_NAME = "PRON1st";
  
  /**
   * The {@link Unit}s for which this feature can be computed.
   */
  private static final Set<UnitLevel> CONSTRAINTS =
      Set.of(
          UnitLevel.DOCUMENT,
          UnitLevel.PARAGRAPH,
          UnitLevel.SENTENCE);

  // -------------------------------------------------------------------------
  // GETTERS
  // -------------------------------------------------------------------------
  
  @Override
  protected String getName() {
    return FEATURE_NAME;
  }

  @Override
  protected Set<UnitLevel> getConstraints() {
    return CONSTRAINTS;
  }
  
  // -------------------------------------------------------------------------
  // FUNCTIONALITY
  // -------------------------------------------------------------------------

  @Override
  protected double computeValue(Unit span) {
    double pronounCount = 0.0;
    double tokenCount = 0.0;
    for (final Token token : JCasUtil.selectCovered(Token.class, span)) {
      final String pos = token.getPos();
      final String lemma = token.getLemma();
      if (pos.equals(".")) // exclude puncuation
        continue;
      if (pos.equals("PRP") 
          && (lemma.toLowerCase().equals("we")
              || lemma.toLowerCase().equals("i")))
        pronounCount++;
      tokenCount++;
    }
    return tokenCount == 0.0 ? 0.0 : pronounCount / tokenCount;
  }
  
}
