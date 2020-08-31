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
 * This class implements the feature called "V:N" in Ortmann19.
 * <p>
 * It computes the ratio of verbs to nouns in a span of text captured
 * by a {@link Unit}-annotation.
 * <p>
 * This feature can be computed for following {@link Unit}s:
 * <ul>
 *  <li>{@link Document}</li>
 *  <li>{@link Paragraph}</li>
 *  <li>{@link Sentence}</li>
 * </ul>
 * <p>
 * <b>Note:</b>
 * <p>
 * The original feature of Ortmann19 only considered full verbs, instead of
 * all verbs in the computation of the feature value. In the Penn Treebank
 * tagset there is no pos-tag indicating full verbs, so this implementation
 * counts all verbs, i.e. tokens with as pos-tag, that starts with "VB".
 * 
 * @author lukas.peter.trautner@uni-weimar.de
 * 
 * @see {@link Feature}
 * @see {@link AbstractFeature}
 * @see {@link AbstractUnitFeatureAnalysisEngine}
 *
 */
public class VerbsToNouns extends AbstractFeature {

  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------
  
  /**
   *The name of this feature.
   */
  private static final String FEATURE_NAME = "V:N";
  
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
    double nounCount = 0.0;
    double verbCount = 0.0;
    for (final Token token : JCasUtil.selectCovered(Token.class, span)) {
      final String pos = token.getPos();
      if (pos.startsWith("NN"))
        nounCount++;
      if (pos.startsWith("VB"))
        verbCount++;
    }
    return nounCount == 0.0 ? 0.0 : verbCount / nounCount;
  }
  
}
