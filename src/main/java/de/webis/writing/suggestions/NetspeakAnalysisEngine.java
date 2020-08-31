package de.webis.writing.suggestions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

import de.aitools.commons.datastructures.Cache;
import de.aitools.commons.uima.core.Sentence;
import de.aitools.commons.uima.core.Token;
import de.webis.writing.Scores;
import de.webis.writing.suggestions.predicates.UnitInContextPredicate;
import de.webis.writing.types.Score;
import de.webis.writing.types.ScoredUnit;
import de.webis.writing.types.Suggestion;


/**
 * An analysis engine that scores unit based on their relative frequency
 * compared to their Netspeak synonyms.
 * 
 * TODO: documentation
 *
 * @author johannes.kiesel@uni-weimar.de
 */
public class NetspeakAnalysisEngine
extends AbstractUnitInContextAnalysisEngine<Sentence, Token> {
  
  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------

  /**
   * The UTF-8 charset.
   */
  protected static final Charset UTF8 = Charset.forName("UTF-8");
  
  /**
   * Property to specify the Netspeak API url.
   */
  public static final String PROPERTY_API_BASE_URL =
      "apiBaseUrl";
  
  /**
   * Default for {@link #PROPERTY_API_BASE_URL}
   */
  protected static final String DEFAULT_API_BASE_URL =
      "https://api.netspeak.org/netspeak4/search?";
  
  /**
   * Property to specify the number of tokens before the token in question to
   * send along with the request.
   */
  public static final String PROPERTY_PREVIOUS_CONTEXT_SIZE =
      "previousContextSize";

  /**
   * Default value for {@link #PROPERTY_PREVIOUS_CONTEXT_SIZE}.
   */
  protected static final int DEFAULT_PREVIOUS_CONTEXT_SIZE = 2;
  
  /**
   * Property to specify the number of tokens after the token in question to
   * send along with the request.
   */
  public static final String PROPERTY_FOLLOWING_CONTEXT_SIZE =
      "followingContextSize";

  /**
   * Default value for {@link #PROPERTY_FOLLOWING_CONTEXT_SIZE}.
   */
  protected static final int DEFAULT_FOLLOWING_CONTEXT_SIZE = 2;
  
  /**
   * Property to specify the maximum number of results to get from the API.
   */
  public static final String PROPERTY_MAX_RESULTS =
      "maxApiResults";

  /**
   * Default value for {@link #PROPERTY_MAX_RESULTS}.
   */
  protected static final int DEFAULT_MAX_RESULTS = 10;
  
  // -------------------------------------------------------------------------
  // MEMBERS
  // -------------------------------------------------------------------------

  private final Cache<Request, Result> netspeakCache;

  private String apiBaseUrl;

  private int previousContextSize;

  private int followingContextSize;
  
  private int maxResults;
  
  // -------------------------------------------------------------------------
  // CONSTRUCTORS
  // -------------------------------------------------------------------------

  /**
   * Creates a new analysis engine with default settings.
   */
  public NetspeakAnalysisEngine() {
    super(Sentence.class, Token.class);
    this.netspeakCache = new Cache<>(new NetspeakRetriever());
    this.setApiBaseUrl(DEFAULT_API_BASE_URL);
    this.setPreviousContextSize(DEFAULT_PREVIOUS_CONTEXT_SIZE);
    this.setFollowingContextSize(DEFAULT_FOLLOWING_CONTEXT_SIZE);
    this.setMaxResults(DEFAULT_MAX_RESULTS);
  }
  
  // -------------------------------------------------------------------------
  // GETTERS
  // -------------------------------------------------------------------------

  /**
   * Gets the Netspeak API base URL.
   * @return The base URL (up to and including '?')
   */
  public String getApiBaseUrl() {
    return this.apiBaseUrl;
  }

  /**
   * Gets the number of tokens before the token in question to consider when
   * getting synonyms.
   * @return The number of tokens
   */
  public int getPreviousContextSize() {
    return this.previousContextSize;
  }

  /**
   * Gets the number of tokens after the token in question to consider when
   * getting synonyms.
   * @return The number of tokens
   */
  public int getFollowingContextSize() {
    return this.followingContextSize;
  }

  /**
   * Gets the maximum number of results to get from the API.
   * @return The maximum number
   */
  public int getMaxResults() {
    return this.maxResults;
  }

  /**
   * Gets the threshold that determines whether a score is assigned to a token.
   * <p>
   * The frequency of the token divided by the frequency of its most-frequent
   * synonym (= the score of the token) must be lower or equal to this threshold
   * for it to be added as a score.
   * </p>
   * @return The threshold
   */
  public double getMaximumScore() {
    return 0.9; // TODO: parameter
  }

  /**
   * Gets the string that is used as the name for the scores that this analysis
   * engine assigns.
   * @return The name
   */
  public String getScoreName() {
    return "NetspeakSynonyms"; // TODO: parameter
  }

  /**
   * Gets the string that is used as the name for the explanation of a
   * suggestion that holds the URL that was used to request Netspeak.
   * @return The name
   */
  public String getRequestUrlName() {
    return "Source"; // TODO: parameter
  }

  /**
   * Gets the string that is used as the name for the explanation of a
   * suggestion that holds the URL to the corresponding Netspeak page.
   * @return The name
   */
  public String getNetspeakUrlName() {
    return "NetspeakUrl"; // TODO: parameter
  }

  /**
   * Gets the cache for Netspeak results.
   * @return The cache
   */
  protected Cache<Request, Result> getNetspeakCache() {
    return this.netspeakCache;
  }
  
  // -------------------------------------------------------------------------
  // SETTERS
  // -------------------------------------------------------------------------

  /**
   * Gets the Netspeak API base URL.
   * @param apiBaseUrl The base URL (up to and including '?')
   */
  public void setApiBaseUrl(final String apiBaseUrl) {
    this.apiBaseUrl = Objects.requireNonNull(apiBaseUrl);
    this.getNetspeakCache().clear();
  }

  /**
   * Sets the number of tokens before the token in question to consider when
   * getting synonyms.
   * @param previousContextSize The number of tokens
   */
  public void setPreviousContextSize(final int previousContextSize) {
    this.previousContextSize = previousContextSize;
  }

  /**
   * Sets the number of tokens after the token in question to consider when
   * getting synonyms.
   * @param followingContextSize The number of tokens
   */
  public void setFollowingContextSize(final int followingContextSize) {
    this.followingContextSize = followingContextSize;
  }

  /**
   * Sets the maximum number of results to get from the API.
   * @return The maximum number
   */
  public void setMaxResults(final int maxResults) {
    this.maxResults = maxResults;
    this.getNetspeakCache().clear();
  }
  
  // -------------------------------------------------------------------------
  // CONFIGURATION
  // -------------------------------------------------------------------------

  @Override
  public void configure(final Properties properties) {
    super.configure(properties);
    if (properties.containsKey(PROPERTY_API_BASE_URL)) {
      this.setApiBaseUrl(properties.getProperty(PROPERTY_API_BASE_URL));
    }
    if (properties.containsKey(PROPERTY_PREVIOUS_CONTEXT_SIZE)) {
      this.setPreviousContextSize(Integer.parseInt(
          properties.getProperty(PROPERTY_PREVIOUS_CONTEXT_SIZE)));
    }
    if (properties.containsKey(PROPERTY_FOLLOWING_CONTEXT_SIZE)) {
      this.setFollowingContextSize(Integer.parseInt(
          properties.getProperty(PROPERTY_FOLLOWING_CONTEXT_SIZE)));
    }
    if (properties.containsKey(PROPERTY_MAX_RESULTS)) {
      this.setMaxResults(Integer.parseInt(
          properties.getProperty(PROPERTY_MAX_RESULTS)));
    }
  }
  
  // -------------------------------------------------------------------------
  // FUNCTIONALITY
  // -------------------------------------------------------------------------


  @Override
  protected void processUnitsInContext(
      final Sentence context, final List<Token> contextUnits) {
    final List<Future<Result>> futureResults =
        this.queryForUnitsInContext(context, contextUnits);
    final double maximumScore = this.getMaximumScore();
    final String scoreName = this.getScoreName();
    final String requestUrlName = this.getRequestUrlName();
    final String netspeakUrlName = this.getNetspeakUrlName();

    try {
      for (int r = 0; r < futureResults.size(); ++r) {
        final Future<Result> futureResult = futureResults.get(r);
        if (futureResult != null) {
          final Result result = futureResult.get();
          final List<Synonym> synonyms = result.getSynonyms();
          if (synonyms.isEmpty()) { continue; }

          final double originalFrequency = result.getOriginalFrequency();
          final double highestFrequency = synonyms.get(0).getFrequency();
          final double originalScore = originalFrequency / highestFrequency;
          if (originalScore >= maximumScore) { continue; }
          final ScoredUnit token = Scores.getUnitFor(contextUnits.get(r));
          Scores.add(token, this.getScoreName(), originalScore);
          

          for (final Synonym synonym : result.getSynonyms()) {
            final double frequency = synonym.getFrequency();
            if (frequency < originalFrequency) { break; }
            final Suggestion suggestion =
                Scores.suggest(token, synonym.getText());
            final Score score =
                Scores.add(suggestion, scoreName, frequency / highestFrequency);
            Scores.explain(score, requestUrlName, result.getRequestUrl());
            Scores.explain(score, netspeakUrlName,
                result.getRequestUrl().replaceAll(
                    "^.*query=",
                    "https://netspeak.org/#q=")); // TODO: parameter
          }
        }
      }
    } catch (final InterruptedException | ExecutionException e) {
      throw new IllegalStateException(e);
    } 
  }

  @Override
  protected void processUnitInContext(
      final Sentence context,
      final List<Token> contextUnits, final int contextUnitsIndex) {
    throw new UnsupportedOperationException();
  }
  
  // -------------------------------------------------------------------------
  // HELPERS
  // -------------------------------------------------------------------------

  /**
   * Queries the Netspeak API for all units in the context.
   * @param context The context of the unit
   * @param contextUnits All units in the context
   */
  protected List<Future<Result>> queryForUnitsInContext(
      final Sentence context, final List<Token> contextUnits) {
    final UnitInContextPredicate<? super Sentence, ? super Token> unitInContextPredicate =
        this.getUnitInContextPredicate();

    final List<Future<Result>> results = new ArrayList<>(contextUnits.size());
    for (int u = 0; u < contextUnits.size(); ++u) {
      if (unitInContextPredicate == null
          || unitInContextPredicate.test(context, contextUnits, u)) {
        results.add(this.queryForUnitInContext(context, contextUnits, u));
      } else {
        results.add(null);
      }
    }
    return Collections.unmodifiableList(results);
  }

  /**
   * Queries the Netspeak API for a unit in one context.
   * @param context The context of the unit
   * @param contextUnits All units in the context
   * @param contextUnitsIndex The index of the unit for which synonyms should be
   * requested
   * @return The result of the query or <code>null</code> if the unit in
   * question is empty after cleaning
   */
  protected Future<Result> queryForUnitInContext(
      final Sentence context,
      final List<Token> contextUnits, final int contextUnitsIndex) {
    final Request request = this.makeRequest(contextUnits, contextUnitsIndex);
    if (request == null) {
      return null;
    } else {
      return this.getNetspeakCache().getFuture(request);
    }
  }

  /**
   * Creates the request to query the Netspeak API for a unit in one context.
   * @param contextUnits All units in the context
   * @param contextUnitsIndex The index of the unit for which synonyms should be
   * requested
   * @return The request or <code>null</code> if the token in question is empty
   * after cleaning
   */
  protected Request makeRequest(
      final List<Token> contextUnits, final int contextUnitsIndex) {
    String before = "";
    final ListIterator<Token> beforeIterator =
        contextUnits.listIterator(contextUnitsIndex);
    for (int remaining = this.getPreviousContextSize();
        remaining > 0 && beforeIterator.hasPrevious();
        --remaining) {
      before = beforeIterator.previous().getCoveredText() + " " + before;
    }

    final String original =
        contextUnits.get(contextUnitsIndex).getCoveredText();

    String after = "";
    final ListIterator<Token> afterIterator =
        contextUnits.listIterator(contextUnitsIndex + 1);
    for (int remaining = this.getFollowingContextSize();
        remaining > 0 && afterIterator.hasNext();
        --remaining) {
      after = after + " " + afterIterator.next().getCoveredText();
    }

    try {
      return new Request(before, original, after);
    } catch (final IllegalArgumentException e) {
      return null; // empty original
    }
  }
  
  // -------------------------------------------------------------------------
  // DATA CLASSES
  // -------------------------------------------------------------------------

  protected static class Request {

    private final String before;

    private final String original;

    private final String after;

    public Request(
        final String before, final String original, final String after) {
      if (before == null || before.isBlank()) {
        this.before = "".intern();
      } else {
        final String beforeCleaned = this.cleanRequestText(before);
        if (beforeCleaned.isEmpty()) {
          this.before = "".intern();
        } else {
          this.before = (beforeCleaned + " ").intern();
        }
      }

      this.original = this.cleanRequestText(original).intern();
      if (this.original.isBlank()) {
        throw new IllegalArgumentException("Empty original");
      }

      if (after == null || after.isBlank()) {
        this.after = "".intern();
      } else {
        final String afterCleaned = this.cleanRequestText(after);
        if (afterCleaned.isEmpty()) {
          this.after = "".intern();
        } else {
          this.after = (" " + afterCleaned).intern();
        }
      }
    }

    public String getBefore() {
      return this.before;
    }

    public String getOriginal() {
      return this.original;
    }

    public String getAfter() {
      return this.after;
    }

    @Override
    public int hashCode() {
      return this.getBefore().hashCode()
          + 31 * this.getOriginal().hashCode()
          + 31 * 31 * this.getAfter().hashCode();
    }

    @Override
    public boolean equals(final Object object) {
      if (object == null) { return false; }
      if (object instanceof Request) {
        final Request other = (Request) object;
        // == possible due to use of 'intern' method in constructor
        return this.getBefore() == other.getBefore()
            && this.getOriginal() == other.getOriginal()
            && this.getAfter() == other.getAfter();
      } else {
        return false;
      }
    }

    protected String cleanRequestText(final String coveredText) {
      return coveredText.toLowerCase().replaceAll("[^a-z0-9' -]", "").strip();
    }

  }

  protected static class Synonym {

    private final String text;

    private final long frequency;

    public Synonym(final String text, final long frequency) {
      this.text = Objects.requireNonNull(text);
      this.frequency = frequency;
    }

    public String getText() {
      return this.text;
    }

    public long getFrequency() {
      return this.frequency;
    }

    @Override
    public String toString() {
      return this.getFrequency() + ":" + this.getText();
    }

  }

  protected static class Result {

    private final List<Synonym> synonyms;

    private final long originalFrequency;

    private final String requestUrl;

    public Result(
        final Collection<Synonym> synonyms,
        final long originalFrequency,
        final String requestUrl) {
      this.synonyms = List.copyOf(synonyms);
      this.originalFrequency = originalFrequency;
      this.requestUrl = Objects.requireNonNull(requestUrl);
    }

    public List<Synonym> getSynonyms() {
      return this.synonyms;
    }

    public long getOriginalFrequency() {
      return this.originalFrequency;
    }

    public String getRequestUrl() {
      return this.requestUrl;
    }

    @Override
    public String toString() {
      return this.getOriginalFrequency() + "->" + this.getSynonyms();
    }
    
  }
  
  // -------------------------------------------------------------------------
  // HELPER CLASSES
  // -------------------------------------------------------------------------

  protected class NetspeakRetriever
  implements Function<Request, Result> {

    @Override
    public Result apply(final Request request) {
      final String before = request.getBefore();
      final String original = request.getOriginal();
      final String after = request.getAfter();

      final URL requestUrl = this.getRequestUrl(request);

      final List<Synonym> synonyms = new ArrayList<>();
      long originalFrequency = 1;
      try {
        final URLConnection connection = requestUrl.openConnection();
        try (final Scanner response =
            new Scanner(connection.getInputStream(), UTF8)) {
          response.useDelimiter("[\\n\\t]");
          while (response.hasNext()) {
            response.next();
            final long frequency = response.nextLong();
            final String phrase = response.next();
            final String suggestion = phrase
                .substring(
                    before.length(),
                    phrase.length() - after.length())
                .strip();
            if (suggestion.equals(original)) {
              originalFrequency = frequency;
            } else {
              synonyms.add(new Synonym(suggestion, frequency));
            }
          }

          if (response.ioException() != null) {
            throw response.ioException();
          }
        }
      } catch (final IOException e) {
        throw new UncheckedIOException(e);
      }

      return new Result(synonyms, originalFrequency, requestUrl.toString());
    }

    protected String getApiBaseUrl() {
      return NetspeakAnalysisEngine.this.getApiBaseUrl();
    }

    protected int getMaxResults() {
      return NetspeakAnalysisEngine.this.getMaxResults();
    }

    protected URL getRequestUrl(final Request request) {
      final String query = URLEncoder.encode(
          request.getBefore()
          + "#" + request.getOriginal()
          + request.getAfter(), UTF8);
      try {
        return new URL(this.getApiBaseUrl()
            + "topk=" + this.getMaxResults()
            + "&query=" + query);
      } catch (final MalformedURLException e) {
        // not possible due to encode unless API base URL is malformed
        throw new IllegalStateException(e);
      }
    }
    
  }

}
