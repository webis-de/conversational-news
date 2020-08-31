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
 * This class implements the feature called "question" in Ortmann19.
 * <p>
 * It computes the ratio of interrogative sentences, i.e. sentences, where the
 * last token has the pos-tag "." and the captured text of that token
 * contains "?", to all sentences in a span of text captured by a
 * {@link Unit}-annotation.
 * <p>
 * This feature can be computed for following {@link Unit}s:
 * <ul>
 *  <li>{@link Document}</li>
 *  <li>{@link Paragraph}</li>
 * </ul>
 * 
 * @author lukas.peter.trautner@uni-weimar.de
 * 
 * @see {@link Feature}
 * @see {@link AbstractFeature}
 * @see {@link AbstractUnitFeatureAnalysisEngine}
 *
 */
public class Question extends AbstractFeature {
  
  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------
  
  /**
   * The name of this feature.
   */
  private static final String FEATURE_NAME = "question";
  
  /**
   * The {@link Unit}s for which this feature can be computed.
   */
  private static final Set<UnitLevel> CONSTRAINTS =
      Set.of(
          UnitLevel.DOCUMENT,
          UnitLevel.PARAGRAPH);
  
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
    double sentenceCount = 0.0;
    double questionCount = 0.0;
    for (final Sentence sentence : JCasUtil.selectCovered(Sentence.class, span)) {
      final List<Token> tokens = 
          JCasUtil.selectCovered(Token.class, sentence);
      if (tokens.size() != 0) {
        final Token token = tokens.get(tokens.size() - 1);
        if (token.getPos().equals(".")
            && token.getCoveredText().contains("?"))
          questionCount++;
      }
      sentenceCount++;
    }
      return sentenceCount == 0.0 ? 0.0 : questionCount / sentenceCount;
  }

}
