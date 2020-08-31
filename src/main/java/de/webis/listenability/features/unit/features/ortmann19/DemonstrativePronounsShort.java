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
 * This class implements the feature called "DEMshort" in Ortmann19.
 * <p>
 * It computes the ratio of demonstrative pronouns realized as the short form
 * "the" to demonstrative pronouns with one of the lemmas "this", "that" or
 * "the", in a span of text captured by a {@link Unit}-annotation.
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
 * The original feature from Ortmann19 identified demonstrative pronouns by
 * the pos-tag "PDS". The Penn Treebank tagset does not include a tag
 * especially for demonstrative pronouns, therefore this feature identifies
 * them with the tag "DT", which is used for determiners (this includes
 * demonstrative pronouns but also other determiners) and then selects only
 * the tokens that also have one of the considered demonstrative pronouns as
 * their lemma.
 * 
 * @author lukas.peter.trautner@uni-weimar.de
 * 
 * @see {@link Feature}
 * @see {@link AbstractFeature}
 * @see {@link AbstractUnitFeatureAnalysisEngine}
 *
 */
public class DemonstrativePronounsShort extends AbstractFeature {

  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------
  
  /**
   * The name of this feature.
   */
  private static final String FEATURE_NAME = "DEMshort";
  
  /**
   * The {@link Unit}s for which this feature can be computed.
   */
  private static final Set<UnitLevel> CONSTRAINTS =
      Set.of(
          UnitLevel.DOCUMENT,
          UnitLevel.PARAGRAPH,
          UnitLevel.SENTENCE);
  
  /**
   * The lemmas of demonstrative pronouns considered by this feature.
   */
  private static final List<String> LEMMAS =
      List.of("the", "this", "that");

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
    double shortFormCount = 0.0;
    for (final Token token : JCasUtil.selectCovered(Token.class, span)) {
      final String pos = token.getPos();
      final String lemma = token.getLemma();
      if (pos.equals("DT")) {
        if (LEMMAS.contains(lemma))
          pronounCount++;
        if (lemma.equals("the"))
          shortFormCount++;
      }
    }
    return pronounCount == 0.0 ? 0.0 : shortFormCount / pronounCount;
  }
  
}
