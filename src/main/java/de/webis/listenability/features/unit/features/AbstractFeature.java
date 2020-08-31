package de.webis.listenability.features.unit.features;

import java.util.Set;

import de.aitools.commons.uima.supertype.Unit;
import de.webis.listenability.features.unit.UnitLevel;
import de.webis.writing.Scores;

/**
 * TODO documentation
 * 
 * @author lukas.peter.trautner@uni-weimar.de
 *
 */
public abstract class AbstractFeature implements Feature {
  
  /**
   * 
   * @return
   */
  protected abstract String getName();
  
  /**
   * 
   * @return
   */
  protected abstract Set<UnitLevel> getConstraints();
  
  /**
   * 
   * @param span
   * @return
   */
  protected abstract double computeValue(final Unit span);
  
  /**
   * 
   */
  public final void compute(final Unit span) {
    Scores.add(span, this.getName(), this.computeValue(span));
  }
  
  /**
   * 
   */
  public final boolean check(final Set<UnitLevel> levels) {
    return this.getConstraints().containsAll(levels);
  }
}