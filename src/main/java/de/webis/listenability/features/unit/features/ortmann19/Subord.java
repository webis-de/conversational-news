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
 * This class implements the feature called "subord" in Ortmann19.
 * <p>
 * It computes the ratio of subordinating conjuctions to all verbs, i.e. the
 * ratio of token with the pos-tag "IN" to token with a tag starting with "VB"
 * in a span of text captured by a {@link Unit}-annotation.
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
 * The original feature from Ortmann19 identifies coordinating conjuctions
 * , based on the "KOUS"/"KOUI"-tags from the STTS-tagset. This implementation
 * uses the Penn Treebank-tagset, which does not include a tag specifically
 * for subordinating conjuctions. It only includes the "IN"-tag which captures
 * prepositions and subordinating conjuctions, which is used here.
 * Also the original paper uses only full verbs in the computation of the
 * feature value, this implementation uses all verbs, due to the absence of
 * pos-tags for full verbs in the Penn Treebank-tagset.
 * 
 * @author lukas.peter.trautner@uni-weimar.de
 * 
 * @see {@link Feature}
 * @see {@link AbstractFeature}
 * @see {@link AbstractUnitFeatureAnalysisEngine}
 *
 */
public class Subord extends AbstractFeature {
  
  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------
  
  /**
   * The name of this feature.
   */
  private static final String FEATURE_NAME = "subord";
  
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
    double verbCount = 0.0;
    double subordCount = 0.0;
    for (final Token token : JCasUtil.selectCovered(Token.class, span)) {
      if (token.getPos().startsWith("VB"))
        verbCount++;
      if (token.getPos().equals("IN"))
        subordCount++;
    }
    return verbCount == 0.0 ? 0.0 : subordCount / verbCount;
  }

}
