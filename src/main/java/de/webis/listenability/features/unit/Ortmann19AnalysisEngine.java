package de.webis.listenability.features.unit;

import java.util.Properties;

import de.aitools.ie.stanford.StanfordDependencyParser;
import de.aitools.ie.stanford.Tokenizer;
import de.webis.listenability.features.unit.features.AbstractFeature;
import de.webis.listenability.features.unit.features.Feature;
import de.webis.listenability.features.unit.features.ortmann19.AnswerParticles;
import de.webis.listenability.features.unit.features.ortmann19.CoordInit;
import de.webis.listenability.features.unit.features.ortmann19.DemonstrativePronouns;
import de.webis.listenability.features.unit.features.ortmann19.DemonstrativePronounsShort;
import de.webis.listenability.features.unit.features.ortmann19.Exclam;
import de.webis.listenability.features.unit.features.ortmann19.Interjections;
import de.webis.listenability.features.unit.features.ortmann19.LexDens;
import de.webis.listenability.features.unit.features.ortmann19.MeanSentenceLength;
import de.webis.listenability.features.unit.features.ortmann19.MeanWordLength;
import de.webis.listenability.features.unit.features.ortmann19.MedianSentenceLength;
import de.webis.listenability.features.unit.features.ortmann19.MedianWordLength;
import de.webis.listenability.features.unit.features.ortmann19.NomCmplx;
import de.webis.listenability.features.unit.features.ortmann19.PronounFirst;
import de.webis.listenability.features.unit.features.ortmann19.PronounSubject;
import de.webis.listenability.features.unit.features.ortmann19.Question;
import de.webis.listenability.features.unit.features.ortmann19.Subord;
import de.webis.listenability.features.unit.features.ortmann19.VerbsToNouns;

/**
 * Analysis engine that adds the features described in
 * <a href="https://git.webis.de/code-research/conversational-search/
listenability-tools/-/blob/master/material/ortmann19-variation-between
-different-discourse-types-literate-vs-oral.pdf">Ortmann19</a> as
 * {@link Score}s.
 * <p>
 * This analysis engine depends on {@link Tokenizer} and
 * {@link StanfordDependencyParser}.
 * </p><p>
 * For the configuration options of this analysis engine, see
 * {@link AbstractUnitFeatureAnalysisEngine}.
 * </p><p>
 * For information regarding the individual features check the documentation
 * of the features.  
 * </p>
 * 
 * @author lukas.peter.trautner@uni-weimar.de
 * 
 * @see AbstractUnitFeatureAnalysisEngine
 * @see Feature
 * @see AbstractFeature
 * 
 */
public class Ortmann19AnalysisEngine
  extends AbstractUnitFeatureAnalysisEngine {
  
  // -------------------------------------------------------------------------
  // CONFIGURATION
  // -------------------------------------------------------------------------

  @Override
  public void configure(final Properties properties) {
    this.addFeature(properties, MeanWordLength.class);
    this.addFeature(properties, MedianWordLength.class);
    this.addFeature(properties, MeanSentenceLength.class);
    this.addFeature(properties, MedianSentenceLength.class);
    this.addFeature(properties, Subord.class);
    this.addFeature(properties, CoordInit.class);
    this.addFeature(properties, Question.class);
    this.addFeature(properties, Exclam.class);
    this.addFeature(properties, NomCmplx.class);
    this.addFeature(properties, VerbsToNouns.class);
    this.addFeature(properties, LexDens.class);
    this.addFeature(properties, PronounSubject.class);
    this.addFeature(properties, PronounFirst.class);
    this.addFeature(properties, DemonstrativePronouns.class);
    this.addFeature(properties, DemonstrativePronounsShort.class);
    this.addFeature(properties, AnswerParticles.class);
    this.addFeature(properties, Interjections.class);
  }
  
}
