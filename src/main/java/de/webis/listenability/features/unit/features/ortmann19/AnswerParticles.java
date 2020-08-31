package de.webis.listenability.features.unit.features.ortmann19;

import java.util.List;
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
 * This class implements the feature called "PTC" in Ortmann19.
 * <p>
 * It computes the ratio of answer particles ("yes", "no", "please", "thanks")
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
public class AnswerParticles extends AbstractFeature {
  
  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------
  
  /**
   * The name of this feature.
   */
  private static final String FEATURE_NAME = "PTC";
  
  /**
   * The {@link Unit}s for which this feature can be computed.
   */
  private static final Set<UnitLevel> CONSTRAINTS =
      Set.of(
          UnitLevel.DOCUMENT,
          UnitLevel.PARAGRAPH,
          UnitLevel.SENTENCE);
  
  /**
   * The list of answer particles.
   */
  private static final List<String> ANSWER_PARTICLES =
      List.of("yes", "no", "please", "thanks");

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
    double tokenCount = 0.0;
    double particleCount = 0.0;
    for (final Token token : JCasUtil.selectCovered(Token.class, span)) {
      final String text = token.getCoveredText().toLowerCase();
      final String pos = token.getPos();
      if (pos.equals(".")) // exclude punctuation
        continue;
      if (ANSWER_PARTICLES.contains(text))
        particleCount++;
      tokenCount++;
    }
    return tokenCount == 0.0 ? 0.0 : particleCount / tokenCount;
  }

}
