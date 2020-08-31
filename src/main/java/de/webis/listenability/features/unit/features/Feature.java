package de.webis.listenability.features.unit.features;

import java.util.Set;

import de.aitools.commons.uima.supertype.Unit;
import de.webis.listenability.features.unit.UnitLevel;

/**
 * TODO documentation
 * 
 * @author lukas.peter.trautner@uni-weimar.de
 *
 */
public interface Feature {
  
  /**
   * 
   * @param levels 
   * @return
   */
  public boolean check(final Set<UnitLevel> levels);

  /**
   * 
   * @param span
   */
  public void compute(final Unit span);
  
}
