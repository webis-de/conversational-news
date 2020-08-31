package de.webis.listenability.features.unit.features.ortmann19;

import java.util.ArrayList;
import java.util.Collections;
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
 * This class implements the feature called "med_sent" in Ortmann19.
 * <p>
 * It computes the median sentence length, based on the number of tokens,
 * excluding tokens representing punctuation, in a span of text captured
 * by a {@link Unit}-annotation.
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
public class MedianSentenceLength extends AbstractFeature {
  
  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------
  
  /**
   * The name of this feature.
   */
  private static final String FEATURE_NAME = "med_sent";
  
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
    final List<Double> sentences = new ArrayList<Double>();
    for (final Sentence sentence : JCasUtil.selectCovered(Sentence.class, span)) {
      double tokenCount = 0.0;
      for (final Token token : JCasUtil.selectCovered(Token.class, sentence)) {
        if (token.getPos().equals(".")) // exclude punctuation
          continue;
        tokenCount++;
      }
      sentences.add(tokenCount);
    }
    Collections.sort(sentences); // compute median
    if (sentences.size() != 0 && sentences.size() % 2 == 0) {
      return (sentences.get(sentences.size() / 2) + sentences.get(sentences.size() / 2 - 1)) / 2.0;
    } else if (sentences.size() != 0 && sentences.size() % 2 == 1) {
      return sentences.get(sentences.size() / 2);
    } else {
      return 0.0;
    }
  }





}
