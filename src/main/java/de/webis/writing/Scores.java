package de.webis.writing;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import de.webis.writing.types.*;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import de.aitools.commons.uima.util.FSArrays;
import de.aitools.commons.uima.supertype.Unit;

/**
 * Utility class for dealing with the writing assistance UIMA types.
 *
 * @author johannes.kiesel@uni-weimar.de
 */
public class Scores {
  
  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------

  /**
   * A factory that can be used to create {@link JsonGenerator}s for the
   * different <code>toJson</code> methods.
   */
  public static final JsonFactory JSON_FACTORY = new JsonFactory();
  
  // -------------------------------------------------------------------------
  // CONSTRUCTORS
  // -------------------------------------------------------------------------
  
  private Scores() {} // prevent instantiation
  
  // -------------------------------------------------------------------------
  // FUNCTIONALITY: SCORED UNITS
  // -------------------------------------------------------------------------

  /**
   * Gets the ScoredUnit for a unit.
   * <p>
   * If a ScoredUnit already exists for the unit, it is returned. Otherwise
   * this function creates a new ScoredUnit, adds it to the indexed, and returns
   * it.
   * </p>
   * @param unit The unit
   * @return The scored unit
   * @throws NullPointerException If the unit is <code>null</code>
   */
  public static ScoredUnit getUnitFor(final Unit unit) {
    final JCas jCas = unit.getJCas();
    final int begin = unit.getBegin();
    final int end = unit.getEnd();

    final AnnotationIndex<ScoredUnit> index =
        jCas.getAnnotationIndex(ScoredUnit.class);
    final Optional<ScoredUnit> maybeScoredUnit = index.select()
        .at(begin, end)
        .filter(scoredUnit -> scoredUnit.getUnit() == unit)
        .findFirst();
    if (maybeScoredUnit.isPresent()) {
      return maybeScoredUnit.get();
    } else {
      final ScoredUnit scoredUnit = new ScoredUnit(jCas, begin, end);
      scoredUnit.setUnit(unit);
      scoredUnit.addToIndexes();
      return scoredUnit;
    }
  }

  // -------------------------------------------------------------------------
  // FUNCTIONALITY: SCORES
  // -------------------------------------------------------------------------

  /**
   * Adds a score to a unit.
   * <p>
   * The value should usually be between 0 (= worst possible) and 1
   * (= everything is fine). The score is used to hint at problems, so values
   * above 1 are usually avoided.
   * </p><p>
   * This function uses {@link #getUnitFor(Unit)} to get the ScoredUnit for the
   * unit and adds the score to that one.
   * </p><p>
   * Explanations can be added to the returned object using
   * {@link #explain(Score, String, String, Annotation)} or another function
   * with the same name (but less parameters).
   * </p>
   * @param unit The unit
   * @param name The name of the measure that calculated the value
   * @param value The calculated value
   * @return The score object
   * @throws NullPointerException If the unit or name is <code>null</code>
   * @throws IllegalArgumentException If the value is negative
   * @see #add(ScoredUnit, String, double)
   */
  public static Score add(
      final Unit unit, final String name, final double value) {
    final ScoredUnit scoredUnit = Scores.getUnitFor(unit);
    return Scores.add(scoredUnit, name, value);
  }

  /**
   * Adds a score to a ScoredUnit.
   * <p>
   * The value should usually be between 0 (= worst possible) and 1
   * (= everything is fine). The score is used to hint at problems, so values
   * above 1 are usually avoided.
   * </p><p>
   * Explanations can be added to the returned object using
   * {@link #explain(Score, String, String, Annotation)} or another function
   * with the same name (but less parameters).
   * </p>
   * @param scoredUnit The unit
   * @param name The name of the measure that calculated the value
   * @param value The calculated value
   * @return The score object
   * @throws NullPointerException If the ScoredUnit or name is <code>null</code>
   * @throws IllegalArgumentException If the value is negative
   * @see #add(Unit, String, double)
   */
  public static Score add(
      final ScoredUnit scoredUnit, final String name, final double value) {
    final Score score = new Score(scoredUnit.getJCas());
    score.setName(Objects.requireNonNull(name));
    if (value < 0.0) {
      throw new IllegalArgumentException(
          "Negative values are not allowed, but got " + value);
    }
    score.setValue(value);
    score.addToIndexes();

    final FSArray<Score> scores = FSArrays.push(
        scoredUnit.getJCas(), scoredUnit.getScores(), score);
    scoredUnit.setScores(scores);
    return score;
  }

  /**
   * Adds a score to a suggestion.
   * <p>
   * The value should usually be between 0 (= worst possible) and 1
   * (= everything is fine). The score is used to hint at problems, so values
   * above 1 are usually avoided.
   * </p><p>
   * Explanations can be added to the returned object using
   * {@link #explain(Score, String, String, Annotation)} or another function
   * with the same name (but less parameters).
   * </p>
   * @param suggestion The suggestion
   * @param name The name of the measure that calculated the value
   * @param value The calculated value
   * @return The score object
   * @throws NullPointerException If the suggestion or name is <code>null</code>
   * @throws IllegalArgumentException If the value is negative
   */
  public static Score add(
      final Suggestion suggestion, final String name, final double value) {
    final Score score = new Score(suggestion.getJCas());
    score.setName(Objects.requireNonNull(name));
    if (value < 0.0) {
      throw new IllegalArgumentException(
          "Negative values are not allowed, but got " + value);
    }
    score.setValue(value);
    score.addToIndexes();

    final FSArray<Score> scores = FSArrays.push(
        suggestion.getJCas(), suggestion.getScores(), score);
    suggestion.setScores(scores);
    return score;
  }
  
  // -------------------------------------------------------------------------
  // FUNCTIONALITY: SUGGESTIONS
  // -------------------------------------------------------------------------

  /**
   * Adds a suggestion to a unit.
   * <p>
   * This function uses {@link #getUnitFor(Unit)} to get the ScoredUnit for the
   * unit and adds the suggestion to that one.
   * </p><p>
   * Scores can be added to the returned object using
   * {@link #add(Suggestion, String, double)}.
   * </p>
   * @param unit The unit
   * @param text The suggestion text, which can (for example) be a synonym or a
   * paraphrase of the unit's covered text
   * @return The suggestion object
   * @throws NullPointerException If the unit or text is <code>null</code>
   * @see #suggest(ScoredUnit, String)
   */
  public static Suggestion suggest(
      final Unit unit, final String text) {
    final ScoredUnit scoredUnit = Scores.getUnitFor(unit);
    return Scores.suggest(scoredUnit, text);
  }

  /**
   * Adds a suggestion to a ScoredUnit.
   * <p>
   * Scores can be added to the returned object using
   * {@link #add(Suggestion, String, double)}.
   * </p>
   * @param scoredUnit The unit
   * @param text The suggestion text, which can (for example) be a synonym or a
   * paraphrase of the unit's covered text
   * @return The suggestion object
   * @throws NullPointerException If the ScoredUnit or text is <code>null</code>
   * @see #suggest(Unit, String)
   */
  public static Suggestion suggest(
      final ScoredUnit scoredUnit, final String text) {
    final Suggestion suggestion = new Suggestion(scoredUnit.getJCas());
    suggestion.setText(Objects.requireNonNull(text));
    suggestion.addToIndexes();

    final FSArray<Suggestion> suggestions = FSArrays.push(
        scoredUnit.getJCas(), scoredUnit.getSuggestions(), suggestion);
    scoredUnit.setSuggestions(suggestions);
    return suggestion;
  }

  /**
   * Adds several suggestion to a unit.
   * <p>
   * This function uses {@link #getUnitFor(Unit)} to get the ScoredUnit for the
   * unit and adds the suggestions to that one.
   * </p><p>
   * Scores can be added to each of the returned object using
   * {@link #add(Suggestion, String, double)}.
   * </p>
   * @param scoredUnit The unit
   * @param texts The suggestion texts, which can (for example) be synonyms or
   * paraphrases of the unit's covered text
   * @return The suggestion objects in the same order as the texts
   * @throws NullPointerException If the unit or texts or some text is
   * <code>null</code>
   * @see #suggest(Unit, String)
   */
  public static List<Suggestion> suggest(
      final Unit unit, final Iterable<String> texts) {
    final ScoredUnit scoredUnit = Scores.getUnitFor(unit);
    return Scores.suggest(scoredUnit, texts);
  }

  /**
   * Adds several suggestion to a ScoredUnit.
   * <p>
   * Scores can be added to each of the returned object using
   * {@link #add(Suggestion, String, double)}.
   * </p>
   * @param scoredUnit The unit
   * @param texts The suggestion texts, which can (for example) be synonyms or
   * paraphrases of the unit's covered text
   * @return The suggestion objects in the same order as the texts
   * @throws NullPointerException If the scored unit or texts or some text is
   * <code>null</code>
   * @see #suggest(Unit, String)
   */
  public static List<Suggestion> suggest(
      final ScoredUnit scoredUnit, final Iterable<String> texts) {
    final List<Suggestion> suggestions = new ArrayList<>();
    for (final String text : texts) {
      suggestions.add(Scores.suggest(scoredUnit, text));
    }
    return suggestions;
  }
  
  // -------------------------------------------------------------------------
  // FUNCTIONALITY: EXPLANATIONS
  // -------------------------------------------------------------------------

  /**
   * Adds an explanation to a score.
   * <p>
   * The key of the explanation will be used to interpret the existence of this
   * explanation object in order to compose a human-readable explanation text
   * for the score. For some measures this text is composed from several
   * explanation objects with different keys.
   * </p>
   * @param score The score
   * @param key The name for this explanation
   * @return The explanation object
   * @throws NullPointerException If the score or key is <code>null</code>
   */
  public static Explanation explain(final Score score, final String key) {
    return Scores.explain(score, key, null, null);
  }

  /**
   * Adds an explanation to a score.
   * <p>
   * The key of the explanation will be used to interpret the value in order to
   * compose a human-readable explanation text for the score. For some measures
   * this text is composed from several explanation objects with different keys.
   * </p>
   * @param score The score
   * @param key The name for this explanation
   * @param value Some value of the explanation (interpretation depends on the
   * key)
   * @return The explanation object
   * @throws NullPointerException If the score or key is <code>null</code>
   */
  public static Explanation explain(final Score score,
      final String key, final String value) {
    return Scores.explain(score, key, value, null);
  }

  /**
   * Adds an explanation to a score.
   * <p>
   * The key of the explanation will be used to interpret the referenced
   * annotation in order to compose a human-readable explanation text
   * for the score. For some measures this text is composed from several
   * explanation objects with different keys.
   * </p>
   * @param score The score
   * @param key The name for this explanation
   * @param reference A referenced annotation for the explanation
   * (interpretation depends on the key)
   * @return The explanation object
   * @throws NullPointerException If the score or key is <code>null</code>
   */
  public static Explanation explain(final Score score,
      final String key, final Annotation reference) {
    return Scores.explain(score, key, null, reference);
  }

  /**
   * Adds an explanation to a score.
   * <p>
   * The key of the explanation will be used to interpret the value and
   * referenced annotation in order to compose a human-readable explanation text
   * for the score. For some measures this text is composed from several
   * explanation objects with different keys.
   * </p>
   * @param score The score
   * @param key The name for this explanation
   * @param value Some value of the explanation (interpretation depends on the
   * key)
   * @param reference A referenced annotation for the explanation
   * (interpretation depends on the key)
   * @return The explanation object
   * @throws NullPointerException If the score or key is <code>null</code>
   */
  public static Explanation explain(final Score score,
      final String key, final String value, final Annotation reference) {
    Objects.requireNonNull(key);
    final Explanation explanation = new Explanation(score.getJCas());
    explanation.setKey(key);
    explanation.setValue(value);
    explanation.setReference(reference);
    explanation.addToIndexes();
    
    final FSArray<Explanation> explanations = FSArrays.push(
        score.getJCas(), score.getExplanations(), explanation);
    score.setExplanations(explanations);
    return explanation;
  }

  
  // -------------------------------------------------------------------------
  // FUNCTIONALITY: JSON
  // -------------------------------------------------------------------------

  /**
   * Creates a JSON representation of the {@link ScoredUnit}s of the JCas.
   * @param jCas The JCas
   * @return The JSON representation
   */
  public static String toJson(final JCas jCas) {
    try {
      final StringWriter stringWriter = new StringWriter();
      final JsonGenerator json = JSON_FACTORY.createGenerator(stringWriter);
      json.writeStartObject();
      Scores.toJson(json, jCas);
      json.writeEndObject();
      json.flush();
      return stringWriter.toString();
    } catch (final IOException e) {
      // should be impossible for StringWriter
      throw new IllegalStateException(e);
    }
  }

  /**
   * Writes all the {@link ScoredUnit}s of the JCas to an array field,
   * 'scoreUnits'.
   * <p>
   * The method requires that the generation is currently within an object.
   * </p>
   * @param json The generator, for example created using {@link #JSON_FACTORY}
   * @param jCas The JCas
   * @throws IOException If the JSON could not be written
   */
  public static void toJson(
      final JsonGenerator json, final JCas jCas)
  throws IOException {
    json.writeArrayFieldStart("scoredUnits");
    for (final ScoredUnit scoredUnit
        : jCas.getAnnotationIndex(ScoredUnit.class)) {
      Scores.toJson(json, scoredUnit);
    }
    json.writeEndArray();
  }

  /**
   * Creates a JSON representation of the ScoredUnit.
   * @param jCas The JCas
   * @return The JSON representation
   */
  public static String toJson(final ScoredUnit scoredUnit) {
    try {
      final StringWriter stringWriter = new StringWriter();
      final JsonGenerator json = JSON_FACTORY.createGenerator(stringWriter);
      Scores.toJson(json, scoredUnit);
      json.flush();
      return stringWriter.toString();
    } catch (final IOException e) {
      // should be impossible for StringWriter
      throw new IllegalStateException(e);
    }
  }

  /**
   * Writes the ScoredUnit as an JSON object.
   * @param json The generator, for example created using {@link #JSON_FACTORY}
   * @param scoredUnit The ScoredUnit
   * @throws IOException If the JSON could not be written
   */
  public static void toJson(
      final JsonGenerator json, final ScoredUnit scoredUnit)
  throws IOException {
    final List<Score> scores =
        FSArrays.getNonNull(scoredUnit.getScores());
    final List<Suggestion> suggestions =
        FSArrays.getNonNull(scoredUnit.getSuggestions());

    json.writeStartObject();
    json.writeNumberField("begin", scoredUnit.getBegin());
    json.writeNumberField("end", scoredUnit.getEnd());
    if (!scores.isEmpty()) {
      json.writeArrayFieldStart("scores");
      for (final Score score : scores) {
        Scores.toJson(json, score);
      }
      json.writeEndArray();
    }
    if (!suggestions.isEmpty()) {
      json.writeArrayFieldStart("suggestions");
      for (final Suggestion suggestion : suggestions) {
        Scores.toJson(json, suggestion);
      }
      json.writeEndArray();
    }
    json.writeEndObject();
  }

  /**
   * Creates a JSON representation of the score.
   * @param jCas The JCas
   * @return The JSON representation
   */
  public static String toJson(final Score score) {
    try {
      final StringWriter stringWriter = new StringWriter();
      final JsonGenerator json = JSON_FACTORY.createGenerator(stringWriter);
      Scores.toJson(json, score);
      json.flush();
      return stringWriter.toString();
    } catch (final IOException e) {
      // should be impossible for StringWriter
      throw new IllegalStateException(e);
    }
  }

  /**
   * Writes the score as an JSON object.
   * @param json The generator, for example created using {@link #JSON_FACTORY}
   * @param score The score
   * @throws IOException If the JSON could not be written
   */
  public static void toJson(
      final JsonGenerator json, final Score score)
  throws IOException {
    final String name = score.getName();
    final double value = score.getValue();
    final List<Explanation> explanations =
        FSArrays.getNonNull(score.getExplanations());

    json.writeStartObject();
    json.writeStringField("name", name);
    json.writeNumberField("value", value);
    if (!explanations.isEmpty()) {
      json.writeArrayFieldStart("explanations");
      for (final Explanation explanation : explanations) {
        Scores.toJson(json, explanation);
      }
      json.writeEndArray();
    }
    json.writeEndObject();
  }

  /**
   * Creates a JSON representation of the suggestion.
   * @param jCas The JCas
   * @return The JSON representation
   */
  public static String toJson(final Suggestion suggestion) {
    try {
      final StringWriter stringWriter = new StringWriter();
      final JsonGenerator json = JSON_FACTORY.createGenerator(stringWriter);
      Scores.toJson(json, suggestion);
      json.flush();
      return stringWriter.toString();
    } catch (final IOException e) {
      // should be impossible for StringWriter
      throw new IllegalStateException(e);
    }
  }

  /**
   * Writes the suggestion as an JSON object.
   * @param json The generator, for example created using {@link #JSON_FACTORY}
   * @param suggestion The suggestion
   * @throws IOException If the JSON could not be written
   */
  public static void toJson(
      final JsonGenerator json, final Suggestion suggestion)
  throws IOException {
    final String text = suggestion.getText();
    final List<Score> scores =
        FSArrays.getNonNull(suggestion.getScores());

    json.writeStartObject();
    json.writeStringField("text", text);
    if (!scores.isEmpty()) {
      json.writeArrayFieldStart("scores");
      for (final Score score : scores) {
        Scores.toJson(json, score);
      }
      json.writeEndArray();
    }
    json.writeEndObject();
  }

  /**
   * Creates a JSON representation of the explanation.
   * @param jCas The JCas
   * @return The JSON representation
   */
  public static String toJson(final Explanation explanation) {
    try {
      final StringWriter stringWriter = new StringWriter();
      final JsonGenerator json = JSON_FACTORY.createGenerator(stringWriter);
      Scores.toJson(json, explanation);
      json.flush();
      return stringWriter.toString();
    } catch (final IOException e) {
      // should be impossible for StringWriter
      throw new IllegalStateException(e);
    }
  }

  /**
   * Writes the explanation as an JSON object.
   * @param json The generator, for example created using {@link #JSON_FACTORY}
   * @param explanation The explanation
   * @throws IOException If the JSON could not be written
   */
  public static void toJson(
      final JsonGenerator json, final Explanation explanation)
  throws IOException {
    final String key = explanation.getKey();
    final String value = explanation.getValue();
    final Annotation reference = explanation.getReference();
    
    json.writeStartObject();
    json.writeStringField("key", key);
    if (value != null) { json.writeStringField("value", value); }
    if (reference != null) {
      json.writeNumberField("referenceBegin", reference.getBegin());
      json.writeNumberField("referenceEnd", reference.getEnd());
    }
    json.writeEndObject();
  }

}
