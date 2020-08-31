package de.webis.writing.suggestions;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.aitools.commons.components.Components;
import de.aitools.commons.uima.pipeline.AnalysisEngineComponent;
import de.aitools.commons.uima.supertype.Unit;
import de.webis.writing.suggestions.predicates.UnitInContextPredicate;

/**
 * An analysis engine that processes units in their context.
 *
 * @param CONTEXT The surrounding context contains all tokens that are taken
 * into consideration for the processing of the unit 
 * @param UNIT These are the units that are processed
 * @author johannes.kiesel@uni-weimar.de
 */
public abstract class AbstractUnitInContextAnalysisEngine<
  CONTEXT extends Annotation, UNIT extends Unit>
extends AnalysisEngineComponent{
  
  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------
  
  /**
   * Property to specify the predicate to determine the units (in their
   * context) for which to generate suggestions.
   */
  public static final String PROPERTY_UNIT_IN_CONTEXT_PREDICATE =
      "unitInContextPredicate";
  
  /**
   * Default value for {@link #PROPERTY_UNIT_IN_CONTEXT_PREDICATE}.
   */
  protected static final String DEFAULT_UNIT_IN_CONTEXT_PREDICATE = null;
  
  // -------------------------------------------------------------------------
  // MEMBERS
  // -------------------------------------------------------------------------

  private final Class<CONTEXT> contextClass;

  private final Class<UNIT> unitClass;

  private UnitInContextPredicate<? super CONTEXT, ? super UNIT> unitInContextPredicate;
  
  // -------------------------------------------------------------------------
  // CONSTRUCTORS
  // -------------------------------------------------------------------------

  /**
   * Creates a new engine without unit in context predicate.
   * @param contextClass Class of the context annotation: the surrounding
   * context contains all tokens that are taken into consideration for the
   * processing of the unit
   * @param unitClass Class of the unit annotation: these are the units that are
   * processed
   * @see #setUnitInContextPredicate(UnitInContextPredicate)
   */
  public AbstractUnitInContextAnalysisEngine(
      final Class<CONTEXT> contextClass, final Class<UNIT> unitClass) {
    this.contextClass = Objects.requireNonNull(contextClass);
    this.unitClass = Objects.requireNonNull(unitClass);
    this.setUnitInContextPredicate(null);
  }
  
  // -------------------------------------------------------------------------
  // GETTERS
  // -------------------------------------------------------------------------

  /**
   * Gets the class of the context annotation.
   * <p>
   * The surrounding context contains all tokens that are taken into
   * consideration for the processing of the unit 
   * </p>
   * @return The class object
   */
  public Class<CONTEXT> getContextClass() {
    return this.contextClass;
  }

  /**
   * Gets the class of the unit annotation.
   * <p>
   * These are the units that are processed.
   * </p>
   * @return
   */
  public Class<UNIT> getUnitClass() {
    return this.unitClass;
  }

  /**
   * Gets the predicate to determine the units (in their context) which to
   * process.
   * @return The predicate or <code>null</code> if all units should be processed
   */
  public UnitInContextPredicate<? super CONTEXT, ? super UNIT> getUnitInContextPredicate() {
    return this.unitInContextPredicate;
  }
  
  // -------------------------------------------------------------------------
  // SETTERS
  // -------------------------------------------------------------------------

  /**
   * Sets the predicate to determine the units (in their context) which to
   * process.
   * @param unitInContextPredicate The predicate or <code>null</code> if all
   * units should be processed
   */
  public void setUnitInContextPredicate(
      final UnitInContextPredicate<? super CONTEXT, ? super UNIT> unitInContextPredicate) {
    this.unitInContextPredicate = unitInContextPredicate;
  }
  
  // -------------------------------------------------------------------------
  // CONFIGURATION
  // -------------------------------------------------------------------------

  @Override
  public void configure(final Properties properties) {
    final String unitInContextPredicateProperty =
        properties.getProperty(
            PROPERTY_UNIT_IN_CONTEXT_PREDICATE,
            DEFAULT_UNIT_IN_CONTEXT_PREDICATE);
    if (unitInContextPredicateProperty == null
        || unitInContextPredicateProperty.isEmpty()) {
      this.setUnitInContextPredicate(null);
    } else {
      @SuppressWarnings("unchecked")
      final UnitInContextPredicate<CONTEXT, UNIT> unitInContextPredicate =
          Components.build(UnitInContextPredicate.class,
              PROPERTY_UNIT_IN_CONTEXT_PREDICATE, properties);
      this.setUnitInContextPredicate(unitInContextPredicate);
    }
  }
  
  // -------------------------------------------------------------------------
  // FUNCTIONALITY
  // -------------------------------------------------------------------------

  @Override
  public void accept(final JCas jCas) {
    final AnnotationIndex<UNIT> unitIndex =
        jCas.getAnnotationIndex(this.getUnitClass());

    final FSIterator<CONTEXT> contexts = this.getContexts(jCas);
    while (contexts.hasNext()) {
      final CONTEXT context = contexts.next();
      final List<UNIT> contextUnits = this.getContextUnits(unitIndex, context);
      this.processUnitsInContext(context, contextUnits);
    }
  }
  
  // -------------------------------------------------------------------------
  // HELPERS
  // -------------------------------------------------------------------------

  /**
   * Gets an iterator over all contexts of the JCas.
   * @param jCas The JCas
   * @return The iterator
   * @see #getContextClass()
   */
  protected FSIterator<CONTEXT> getContexts(final JCas jCas) {
    return jCas.getAnnotationIndex(this.getContextClass()).iterator();
  }

  /**
   * Gets all units of the context as a list.
   * @param unitIndex The index of units
   * @param context the context
   * @return All units of the context
   * @see #getUnitClass()
   */
  protected List<UNIT> getContextUnits(
      final AnnotationIndex<UNIT> unitIndex, final CONTEXT context) {
    return Collections.unmodifiableList(
        unitIndex.select()
          .coveredBy(context.getBegin(), context.getEnd())
          .asList());
  }

  /**
   * Processes all units in one context.
   * @param context The context
   * @param contextUnits All units in the context
   * @see #getContextClass()
   * @see #getUnitClass()
   */
  protected void processUnitsInContext(
      final CONTEXT context, final List<UNIT> contextUnits) {
    final UnitInContextPredicate<? super CONTEXT, ? super UNIT> unitInContextPredicate =
        this.getUnitInContextPredicate();

    for (int u = 0; u < contextUnits.size(); ++u) {
      if (unitInContextPredicate == null
          || unitInContextPredicate.test(context, contextUnits, u)) {
        this.processUnitInContext(context, contextUnits, u);
      }
    }
  }

  /**
   * Processes a unit in its context.
   * @param context The context of the unit
   * @param contextUnits All units in the context
   * @param contextUnitsIndex The index of the unit to be processed
   * @see #getContextClass()
   * @see #getUnitClass()
   */
  protected abstract void processUnitInContext(
      final CONTEXT context, final List<UNIT> contextUnits,
      final int contextUnitsIndex);

}
