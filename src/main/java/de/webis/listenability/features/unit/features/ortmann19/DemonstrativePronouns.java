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
 * This class implements the feature called "DEM" in Ortmann19.
 * <p>
 * It computes the ratio of demonstrative pronouns, i.e. tokens with the
 * pos-tag "DT" and one of the following lemmas, "this", "that", "those",
 * "these" and "the", to all words, excluding punctuation, in a span of
 * text captured by a {@link Unit}-annotation.
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
 * The original feature from Ortmann19 identified demonstrative pronouns
 * by the pos-tag "PDS". The Penn Treebank tagset does not include a tag
 * especially for demonstrative pronouns, therefore this feature identifies
 * them with the tag "DT", which is used for determiners (this includes
 * demonstrative pronouns but also other determiners) and then selects only
 * the tokens that also have a demonstrative pronoun ("this", "that", "those",
 * "these") as their lemma. "The" is added to the list of lemmas, due to the
 * fact that some demonstrative pronouns are realized as the short form "the",
 * see {@link DemonstrativePronounsShort}.
 * 
 * @author lukas.peter.trautner@uni-weimar.de
 * 
 * @see {@link Feature}
 * @see {@link AbstractFeature}
 * @see {@link AbstractUnitFeatureAnalysisEngine}
 *
 */
public class DemonstrativePronouns extends AbstractFeature {
  
  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------
  
  /**
   * The name of this feature.
   */
  private static final String FEATURE_NAME = "DEM";
  
  /**
   * The {@link Unit}s for which this feature can be computed.
   */
  private static final Set<UnitLevel> CONSTRAINTS =
      Set.of(
          UnitLevel.DOCUMENT,
          UnitLevel.PARAGRAPH,
          UnitLevel.SENTENCE);
  
  /**
   * The lemmas of demonstrative pronouns.
   */
  private static final List<String> PRONOUNS =
      List.of("this", "that", "those", "these", "the");
  
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
      if (pos.equals(".")) // exclude punctuation
        continue;
      if (pos.equals("DT") && PRONOUNS.contains(lemma))
        pronounCount++;
      tokenCount++;
    }
    return tokenCount == 0.0 ? 0.0 : pronounCount / tokenCount;
  }

}
