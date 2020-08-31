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
 * This class implements the feature called "lexDens" in Ortmann19.
 * <p>
 * It computes the ratio of lexical items, i.e. tokens tagged with one of
 * the tags in {@link #LEXICAL_ITEMS}, to all words, excluding punctuation,
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
 * The original feature from Ortmann19 defined lexical items as a tokens with
 * one of the following pos-tags "ADJ.", "ADV", "N.", "VV.*" from the
 * STTS-tagset. This feature uses all nouns, verbs, adjective and adverbs as
 * lexical items, due to the absence of corresponding tags in the Penn
 * Treebank-tagset.
 * 
 * @author lukas.peter.trautner@uni-weimar.de
 * 
 * @see {@link Feature}
 * @see {@link AbstractFeature}
 * @see {@link AbstractUnitFeatureAnalysisEngine}
 *
 */
public class LexDens extends AbstractFeature {

  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------
  
  /**
   * The name of this feature.
   */
  private static final String FEATURE_NAME = "lexDens";
  
  /**
   * The {@link Unit}s for which this feature can be computed.
   */
  private static final Set<UnitLevel> CONSTRAINTS =
      Set.of(
          UnitLevel.DOCUMENT,
          UnitLevel.PARAGRAPH,
          UnitLevel.SENTENCE);
  
  /**
   * The pos-tags indicating lexical items. 
   */
  private static final List<String> LEXICAL_ITEMS =
      List.of("NN", "NNS", "NNP", "NNPS",
              "JJ", "JJR", "JJS",
              "RB", "RBR", "RBS", "WRB",
              "VB", "VBD", "VBG", "VBN", "VBP", "VBZ");

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
    double lexicalCount = 0.0;
    double tokenCount = 0.0;
    for (final Token token : JCasUtil.selectCovered(Token.class, span)) {
      final String pos = token.getPos();
      if (pos.equals(".")) // exclude punctuation
        continue;
      if (LEXICAL_ITEMS.contains(pos))
        lexicalCount++;
      tokenCount++;
    }
    return tokenCount == 0.0 ? 0.0 : lexicalCount / tokenCount;
  }
  
}
