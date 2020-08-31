package de.webis.listenability.features.unit.features.ortmann19;

import java.util.ArrayList;
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
 * This class implements the feature called "nomCmplx" in Ortmann19.
 * <p>
 * It computes the mean number of prenomial dependents, including determiners
 * and excluding punctuation marks, prepositions and contractions of
 * prepositions and articles, for each noun in the dependency tree, for a span
 *  of text captured by a {@link Unit}-annotation.
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
public class NomCmplx extends AbstractFeature {
  
  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------
  
  /**
   * The name of this feature.
   */
  private static final String FEATURE_NAME = "nomCmplx";
  
  /**
   * The {@link Unit}s for which this feature can be computed.
   */
  private static final Set<UnitLevel> CONSTRAINTS =
      Set.of(
          UnitLevel.DOCUMENT,
          UnitLevel.PARAGRAPH,
          UnitLevel.SENTENCE);
  
  /**
   * The tags indicating nouns.
   */
  private static final List<String> NOUNS =
      List.of("NN", "NNS", "NNP", "NNPS");
  
  /**
   * The tags indicating prenomials.
   */
  private static final List<String> PRENOMIALS =
      List.of("DT", "PDT");
  
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
    final List<Token> nouns = new ArrayList<Token>();
    for (final Token token : JCasUtil.selectCovered(Token.class, span)) {
     if (NOUNS.contains(token.getPos()))
       nouns.add(token);
    }
    double nounCount = nouns.size();
    double prenominalDepCount = 0.0;
    for (final Token token : JCasUtil.selectCovered(Token.class, span)) {
      if (PRENOMIALS.contains(token.getPos())) {
        final Token parent = token.getParent();
        if (parent != null && NOUNS.contains(parent.getPos())) {
          prenominalDepCount++;
        }
      }
    }
    return nounCount == 0.0 ? 0.0 : prenominalDepCount / nounCount;
  }
  
}
