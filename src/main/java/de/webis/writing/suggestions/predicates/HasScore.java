package de.webis.writing.suggestions.predicates;

import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.apache.uima.jcas.tcas.Annotation;

import de.aitools.commons.uima.supertype.Unit;
import de.aitools.commons.uima.util.FSArrays;
import de.webis.writing.Scores;
import de.webis.writing.types.Score;
import de.webis.writing.types.ScoredUnit;

/**
 * A predicate that checks whether a unit has a certain score (optionally below
 * some threshold).
 * <p>
 * {@link #test(Annotation, List, int)} returns <code>true</code> if the unit
 * has at least one score with the score name below the threshold.
 * </p>
 *
 * @author johannes.kiesel@uni-weimar.de
 */
public class HasScore
implements UnitInContextPredicate<Annotation, Unit> {
  
  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------
  
  /**
   * Property that specifies the name of the score to check for.
   */
  public static final String PROPERTY_SCORE_NAME = "scoreName";
  
  /**
   * Property that specifies the threshold of the score to check for.
   */
  public static final String PROPERTY_SCORE_THRESHOLD = "scoreThreshold";
  
  // -------------------------------------------------------------------------
  // MEMBERS
  // -------------------------------------------------------------------------

  private String scoreName;

  private double scoreThreshold;
  
  // -------------------------------------------------------------------------
  // CONSTRUCTORS
  // -------------------------------------------------------------------------

  /**
   * Creates a new predicate, but without score name set.
   */
  public HasScore() {
    this.scoreName = null;
    this.scoreThreshold = Double.POSITIVE_INFINITY;
  }
  
  // -------------------------------------------------------------------------
  // GETTERS
  // -------------------------------------------------------------------------

  /**
   * Gets the name of the score to check for. 
   * @return The name of the score
   * @see Score#getName()
   */
  public String getScoreName() {
    return this.scoreName;
  }

  /**
   * Gets the threshold to check for.
   * <p>
   * Only scored units with the score below the threshold fulfill this
   * predicate. 
   * </p>
   * @return The thresholds
   * @see Score#getValue()
   */
  public double getScoreThreshold() {
    return this.scoreThreshold;
  }
  
  // -------------------------------------------------------------------------
  // SETTERS
  // -------------------------------------------------------------------------

  /**
   * Sets the name of the score to check for. 
   * @param scoreName The name of the score
   * @see Score#getName()
   */
  public void setScoreName(final String scoreName) {
    this.scoreName = Objects.requireNonNull(scoreName);
  }

  /**
   * Sets the threshold to check for.
   * @param scoreThreshold The threshold
   * @see #getScoreThreshold()
   * @see Score#getValue()
   */
  public void setScoreThreshold(final double scoreThreshold) {
    this.scoreThreshold = scoreThreshold;
  }
  
  // -------------------------------------------------------------------------
  // CONFIGURATION
  // -------------------------------------------------------------------------

  @Override
  public void configure(final Properties properties) {
    this.setScoreName(properties.getProperty(PROPERTY_SCORE_NAME));

    if (properties.containsKey(PROPERTY_SCORE_THRESHOLD)) {
      this.setScoreThreshold(
          Double.parseDouble(properties.getProperty(PROPERTY_SCORE_THRESHOLD)));
    }
  }
  
  // -------------------------------------------------------------------------
  // FUNCTIONALITY
  // -------------------------------------------------------------------------

  @Override
  public boolean test(
      final Annotation context,
      final List<? extends Unit> contextUnits, final int contextUnitsIndex) {
    final Unit unit = contextUnits.get(contextUnitsIndex);
    final ScoredUnit scoredUnit = Scores.getUnitFor(unit);
    final String scoreName = Objects.requireNonNull(this.getScoreName());
    final double scoreThreshold = this.getScoreThreshold();
    for (final Score score : FSArrays.getNonNull(scoredUnit.getScores())) {
      if (score.getName().equals(scoreName)
          && score.getValue() < scoreThreshold) {
        return true;
      }
    }
    return false;
  }

}
