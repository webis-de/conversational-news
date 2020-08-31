package de.webis.writing;

import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;

import org.apache.uima.jcas.JCas;

import de.aitools.commons.uima.pipeline.AnalysisEngineComponent;
import de.webis.writing.types.Explanation;
import de.webis.writing.types.Score;
import de.webis.writing.types.ScoredUnit;
import de.webis.writing.types.Suggestion;

/**
 * Pipeline for analyzing text and making suggestions on how to improve it.
 * <p>
 * This class can be used as an UIMA analysis engine, and makes use of two such
 * engines itself:
 * </p>
 * <ul>
 * <li>
 * The <b>scoring engine</b> assigns scores to units of text. It first detects
 * such units and then assesses the appropriate units, adding
 * {@link ScoredUnit}s and {@link Score}s with {@link Explanation}s.
 * </li>
 * <li>
 * The <b>suggestion engine</b> then suggests alternative words or formulations
 * for the scored units, adding {@link Suggestion}s if available.
 * </li>
 * </ul>
 * <p>
 * After the suggestion engine is run, the scoring engine is used again on the
 * suggestions to score each of those. This allows the client (not included) to
 * rank the suggestions and provide explanations to the user. <b>This last step
 * is not implemented yet!</b>
 * </p>
 *
 * @author johannes.kiesel@uni-weimar.de
 */
public class WritingAssistanceEngine
extends AnalysisEngineComponent {
  
  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------

  /**
   * The parameter to specify the analysis engine that is used to score text.
   * <p>
   * The value can be the path to an <code>.xml</code>-file, in which case it is
   * assumed to be the descriptor of the analysis engine to use. Alternatively,
   * it could be the class name of an {@link AnalysisEngineComponent} which is
   * instantiated and configured.
   * </p>
   */
  public static final String PROPERTY_SCORING_ENGINE = "ScoringEngine";

  /**
   * The parameter to specify the analysis engine that is used to make
   * suggestions for a scored text.
   * <p>
   * The value can be the path to an <code>.xml</code>-file, in which case it is
   * assumed to be the descriptor of the analysis engine to use. Alternatively,
   * it could be the class name of an {@link AnalysisEngineComponent} which is
   * instantiated and configured.
   * </p>
   */
  public static final String PROPERTY_SUGGESTION_ENGINE = "SuggestionEngine";
  
  // -------------------------------------------------------------------------
  // MEMBERS
  // -------------------------------------------------------------------------

  private Consumer<JCas> scoringEngine;

  private Consumer<JCas> suggestionEngine;

  // -------------------------------------------------------------------------
  // CONSTRUCTOR
  // -------------------------------------------------------------------------

  /**
   * Creates a new pipeline.
   * <p>
   * The pipeline has to be {@link #configure(Properties)}d before it can be
   * used.
   * </p>
   */
  public WritingAssistanceEngine() {
    this.scoringEngine = null;
    this.suggestionEngine = null;
  }

  // -------------------------------------------------------------------------
  // GETTERS
  // -------------------------------------------------------------------------

  /**
   * Gets the scoring engine.
   * <p>
   * This engine assigns scores to units of text. It first detects such units
   * and then assesses the appropriate units, adding {@link ScoredUnit}s and
   * {@link Score}s with {@link Explanation}s.
   * </p><p>
   * The scoring engine is used once before the suggestion engine and then after
   * the suggestion engine for each suggestion.
   * </p>
   * @return The scoring engine
   */
  public Consumer<JCas> getScoringEngine() {
    return this.scoringEngine;
  }

  /**
   * Gets the suggestion engine.
   * <p>
   * This engine suggests alternative words or formulations for
   * {@link ScoredUnit}s, adding {@link Suggestion}s if available.
   * </p><p>
   * The suggestion engine is used after the scoring engine.
   * </p>
   * @return
   */
  public Consumer<JCas> getSuggestionEngine() {
    return this.suggestionEngine;
  }

  // -------------------------------------------------------------------------
  // CONFIGURATION
  // -------------------------------------------------------------------------

  @Override
  public void configure(final Properties properties) {
    this.setScoringEngine(AnalysisEngineComponent.create(
        PROPERTY_SCORING_ENGINE, properties));
    this.setSuggestionEngine(AnalysisEngineComponent.create(
        PROPERTY_SUGGESTION_ENGINE, properties));
  }

  /**
   * Sets the scoring engine.
   * <p>
   * See {@link #getScoringEngine()} for an explanation.
   * </p>
   * @param scoringEngine The scoring engine
   */
  public void setScoringEngine(final Consumer<JCas> scoringEngine) {
    this.scoringEngine = Objects.requireNonNull(scoringEngine);
  }

  /**
   * Sets the suggestion engine.
   * <p>
   * See {@link #getSuggestionEngine()} for an explanation.
   * </p>
   * @param suggestionEngine The suggestion engine
   */
  public void setSuggestionEngine(final Consumer<JCas> suggestionEngine) {
    this.suggestionEngine = Objects.requireNonNull(suggestionEngine);
  }

  // -------------------------------------------------------------------------
  // FUNCTIONALITY
  // -------------------------------------------------------------------------

  @Override
  public void accept(final JCas jCas) {
    final Consumer<JCas> scoringEngine =
        Objects.requireNonNull(this.getScoringEngine());
    final Consumer<JCas> suggestionEngine =
        Objects.requireNonNull(this.getSuggestionEngine());

    scoringEngine.accept(jCas);
    suggestionEngine.accept(jCas);
    
    // TODO: score each suggestion using the scoring engine
  }

}
