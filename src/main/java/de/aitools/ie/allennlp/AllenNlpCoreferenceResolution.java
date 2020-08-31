package de.aitools.ie.allennlp;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.aitools.commons.uima.core.Coreference;
import de.aitools.commons.uima.core.Token;
import de.aitools.commons.uima.pipeline.AnalysisEngineComponent;

/**
 * <p>Analysis engine that adds {@link Coreference}-annotations, by using 
 * the coreference resolution model provided by 
 * <a href="https://demo.allennlp.org/coreference-resolution">AllenNlp</a></p>
 * 
 * <p>The text to be analyzed must already contain {@link Token}-annotations.
 * The tokenized text is then send to a server, specified by
 * {@link #PROPERTY_ALLENNLP_SERVICE_URL}, via a POST-request, containing the
 * tokenized text as JSON. The server then responds with the indices of the
 * identified coreferences, which are then added to the respective JCas.</p>
 * 
 * @author lukas.peter.trautner@uni-weimar.de
 * 
 * @see <a href="https://git.webis.de/code-research/conversational-search/allennlp-server">
 * allennlp-server on Gitlab</a>
 *
 */
public class AllenNlpCoreferenceResolution extends AnalysisEngineComponent {

  // -------------------------------------------------------------------------
  // LOGGING
  // -------------------------------------------------------------------------
  
  private static final Logger LOG =
      Logger.getLogger(AllenNlpCoreferenceResolution.class.getName());
  
  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------
  
  /**
   * Property for the location of the AllenNLP server.
   */
  private static final String PROPERTY_ALLENNLP_SERVICE_URL = "service.url";
  
  // -------------------------------------------------------------------------
  // MEMBERS
  // -------------------------------------------------------------------------
  
    private String serviceUrl;
  
  // -------------------------------------------------------------------------
  // GETTERS
  // -------------------------------------------------------------------------
    
    /**
     * Gets the location of the AllenNlp server.
     * 
     * @return the location
     * 
     * @see {@link #setServiceUrl(String)}
     * @see {@link #configure(Properties)}
     */
    public String getServiceUrl() {
      return this.serviceUrl;
    }
    
  // -------------------------------------------------------------------------
  // SETTERS
  // -------------------------------------------------------------------------
    
    /**
     * Sets the location of the AllenNlp Server.
     * 
     * @param serviceUrl the location
     * 
     * @see {@link #getServiceUrl()}
     * @see {@link #configure(Properties)}
     */
    public void setServiceUrl(final String serviceUrl) {
      this.serviceUrl = serviceUrl;
    }
    
  // -------------------------------------------------------------------------
  // CONFIGURATION
  // -------------------------------------------------------------------------
  
  @Override
  public void configure(Properties properties) {
    this.setServiceUrl(properties.getProperty(
        PROPERTY_ALLENNLP_SERVICE_URL, null));
  }
  
  // -------------------------------------------------------------------------
  // FUNCTIONALITY
  // -------------------------------------------------------------------------

  @Override
  public void accept(JCas jCas) {
    try {
      final List<Token> tokens = this.getTokens(jCas);
      final AllenNlpTags content = this.queryAllenNlp(
           PROPERTY_ALLENNLP_SERVICE_URL, tokens);
      this.addAnnotations(jCas, tokens, content.getClusters());
    } catch (Exception e) {
      LOG.severe("Failed to annotate coreferences. " + e.toString());
    }
  }
  
  /**
   * Adds all of the {@link Coreference} annotations to a {@link JCas},
   * by calling {@link #addAnnotation(JCas, Token, Token, int)}.
   * 
   * @param jCas      the JCas to which the annotations are added
   * @param tokens    the tokens of the document being processed
   * @param clusters  the coreference clusters
   * 
   * @see {@link #addAnnotation(JCas, Token, Token, int)}
   */
  private void addAnnotations(final JCas jCas,
      final List<Token> tokens, final int[][][] clusters) {
    for (int clusterId = 0; clusterId < clusters.length; clusterId++) {
      for (int i = 0; i < clusters[clusterId].length; i++) {
        final Token startToken = tokens.get(clusters[clusterId][i][0]);
        final Token endToken = tokens.get(clusters[clusterId][i][1]);
        this.addAnnotation(jCas, startToken, endToken, clusterId);
      }
    }
  }
  
  /**
   * Adds one {@link Coreference} annotations to a {@link JCas}.
   * 
   * @param jCas        the JCas to which the annotation is added
   * @param startToken  the first token of a coreference
   * @param endToken    the last token of a coreference
   * @param clusterId   the id of the cluster the coreference belongs to
   * 
   * @see {@link #addAnnotations(JCas, List, int[][][])} 
   */
  private void addAnnotation(final JCas jCas,
      final Token startToken,
      final Token endToken,
      final int clusterId) {
    final Coreference coref = new Coreference(jCas);
    coref.setBegin(startToken.getBegin());
    coref.setEnd(endToken.getEnd());
    coref.setEntity("");
    coref.setClusterId(clusterId);
    coref.addToIndexes();
  }
  
  /**
   * Returns the tokenized document a list of {@link Token}s.
   * 
   * @param jCas the JCas
   * @return the tokenized document
   */
  private List<Token> getTokens(final JCas jCas) {
    final List<Token> tokens = new ArrayList<Token>();
    final FSIterator<Annotation> tokenIter = 
        jCas.getAnnotationIndex(Token.type).iterator();
    while (tokenIter.hasNext()){
      tokens.add((Token) tokenIter.next());
    }
    return tokens;
  }
  /**
   * Queries the AllenNlp server, specified by its url, with the tokenized
   * document and returns its output as a {@link AllenNlpTags}-object.
   * 
   * @param serviceUrl the url of the AllenNLP server
   * @param tokens the tokens of a document
   * @return the output of AllenNLP 
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   * @throws InterruptedException
   * 
   * @see {@link AllenNlpTags}
   * @see {@link #requestBodyToJson(List, ObjectMapper)}
   * @see {@link #makeRequest(String)}
   */
  private AllenNlpTags queryAllenNlp(final String serviceUrl,
      final List<Token> tokens) 
          throws JsonParseException,
          JsonMappingException,
          IOException,
          InterruptedException {
    final List<String> tokenizedDocument =
        tokens.stream().map(Token::getCoveredText)
        .collect(Collectors.toList());
    final ObjectMapper mapper = new ObjectMapper();
    final String requestBody =
        this.requestBodyToJson(tokenizedDocument, mapper);
    HttpResponse<String> response = this.makeRequest(requestBody);
    return mapper.readValue(response.body(), AllenNlpTags.class);
  }
  
/**
 * Transforms the tokenized document into a JSON-String, such that the
 * AllenNLP server is able to parse the request correctly.
 * 
 * @param tokenizedDocument the tokenized document
 * @param mapper the {@link ObjectMapper} 
 * @return the JSON-String
 * @throws JsonProcessingException
 * 
 * @see {@link #queryAllenNlp(String, List)}
 * @see {@link #makeRequest(String)}
 */
  private String requestBodyToJson(final List<String> tokenizedDocument,
      final ObjectMapper mapper)
      throws JsonProcessingException {
    final Map<String, Object> values =
        new HashMap<String, Object>();
    values.put("model", "coref");
    values.put("input", tokenizedDocument);
    return mapper.writeValueAsString(values);
  }
  
  /**
   * Sends a HTTP-request containing the tokenized document in the body
   * to the AllenNLP server.
   * 
   * @param requestBody the body of the HTTP-request.
   * @return the response
   * @throws IOException
   * @throws InterruptedException
   * 
   * @see {@link #queryAllenNlp(String, List)}
   * @see {@link #requestBodyToJson(List, ObjectMapper)}
   */
  private HttpResponse<String> makeRequest(final String requestBody)
      throws IOException, InterruptedException {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(this.getServiceUrl()))
        .POST(HttpRequest.BodyPublishers.ofByteArray(
            requestBody.getBytes(StandardCharsets.US_ASCII)))
        .setHeader("Content-Type", "application/json")
        .setHeader("Accept", "application/json")
        .build();
    return client.send(request,
        HttpResponse.BodyHandlers.ofString());
  }
  
  // -------------------------------------------------------------------------
  // HELPERS
  // -------------------------------------------------------------------------
  
  /**
   * Utility class to allow for parsing of the JSON-responses of AllenNLP,
   * using {@link ObjectMapper}.
   * 
   * @author lukas.peter.trautner@uni-weimar.de
   *
   */
  private static class AllenNlpTags {
    
    @JsonIgnore
    public String[] document;
    
    public int[][][] clusters;
    
    @JsonIgnore
    public int[][] antecedentIndices;
    
    @JsonIgnore
    public int[] predictedAntecedents;
    
    @JsonIgnore
    public int[][] topSpans;
    
    @JsonProperty("document")
    public void setDocument(final String[] document) {
      this.document = document;
    }
    
    @JsonProperty("clusters")
    public void setClusters(final int[][][] clusters) {
      this.clusters = clusters;
    }
    
    @JsonProperty("antecedent_indices")
    public void setAntecedentIndices(final int[][] antecedentIndices) {
      this.antecedentIndices = antecedentIndices;
    }
    
    @JsonProperty("predicted_antecedents")
    public void setPredictedAntecedents(final int[] predictedAntecedents) {
      this.predictedAntecedents = predictedAntecedents;
    }
    
    @JsonProperty("top_spans")
    public void setTopSpans(final int[][] topSpans) {
      this.topSpans = topSpans;
    }
    
    public int[][][] getClusters() {
      return this.clusters;
    }
  }

}
