package de.webis.writing.suggestions.predicates;

import java.util.List;

import org.apache.uima.jcas.tcas.Annotation;

import de.aitools.commons.components.Component;
import de.aitools.commons.uima.supertype.Unit;

/**
 * A predicate for units in some context.
 *
 * @param CONTEXT The surrounding context contains all tokens that are taken
 * into consideration for evaluating a unit
 * @param UNIT The units to be evaluated
 * @author johannes.kiesel@uni-weimar.de
 */
public interface UnitInContextPredicate<
  CONTEXT extends Annotation, UNIT extends Unit>
extends Component {

  /**
   * Tests one unit in its context.
   * @param context The context of the unit
   * @param contextUnits All units in the context
   * @param contextUnitsIndex The index of the unit to be tested
   * @return The result of the test
   */
  public boolean test(
      final CONTEXT context, final List<? extends UNIT> contextUnits,
      final int contextUnitsIndex);

}
