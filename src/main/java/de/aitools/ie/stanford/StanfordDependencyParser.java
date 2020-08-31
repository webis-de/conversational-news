package de.aitools.ie.stanford;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.aitools.commons.uima.core.Sentence;
import de.aitools.commons.uima.core.Token;
import de.aitools.commons.uima.pipeline.AnalysisEngineComponent;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.ling.CoreAnnotations.ChunkAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.StemAnnotation;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.trees.TypedDependency;

/**
 * Wrapper of the stanford dependency parser, that creates a dependency
 * parse tree.
 * <p>
 * This analysis engine requires {@link Token} and {@link Sentence}
 * annotations. 
 * </p><p>
 * The output of the analysis are the <code>parent</code> and
 * <code>depLabel</code> features of the {@link Token}s.
 * </p><p>
 * This class is based on the wrapper for the stanford parser in
 * the <a href="https://git.webis.de/code-lib/aitools/aitools4-ie-uima/-/blob/
master/src/main/java/de/aitools/ie/uima/analysis/parsing/
StanfordParser.java">aitools-ie-uima</a>-project.
 * </pre>
 * <p>
 * @author lukas.peter.trautner@uni-weimar.de
 *
 */
public class StanfordDependencyParser extends AnalysisEngineComponent {
  
  // -------------------------------------------------------------------------
  // CONSTANTS
  // -------------------------------------------------------------------------
  
  /**
   * Configuration option that specifies the location of the model that is
   * used by the dependency parser.
   */
  private static final String PROPERTY_MODEL = "model";
  
  /**
   * Default value for {@link #PROPERTY_MODEL}.
   */
  private static final String DEFAULT_MODEL = DependencyParser.DEFAULT_MODEL;
  
  // -------------------------------------------------------------------------
  // MEMBERS
  // -------------------------------------------------------------------------
  
  /**
   * The dependency parser.
   */
  private DependencyParser parser;
  
  // -------------------------------------------------------------------------
  // CONSTRUCTORS
  // -------------------------------------------------------------------------
  
  /**
   * Creates a new {@link StanfordDependencyParser} analysis engine component.
   */
  public StanfordDependencyParser() {
    this.parser = null;
  }
  
  // -------------------------------------------------------------------------
  // GETTERS
  // -------------------------------------------------------------------------
  
  /**
   * Gets the internal dependency parser.
   * 
   * @return The dependency parser
   * 
   * @see #setParser(DependencyParser)
   */
  public DependencyParser getParser() {
    return this.parser;
  }
  
  // -------------------------------------------------------------------------
  // GETTERS
  // -------------------------------------------------------------------------
  
  /**
   * Sets the internal dependency parser.
   * 
   * @param parser The dependency parser
   * 
   * @see #getParser()
   */
  private void setParser(final DependencyParser parser) {
    this.parser = parser;
  }
  
  // -------------------------------------------------------------------------
  // CONFIGURATION
  // -------------------------------------------------------------------------

  @Override
  public void configure(final Properties properties) {
    final String model =
        properties.getProperty(PROPERTY_MODEL, DEFAULT_MODEL);
    this.setParser(DependencyParser.loadFromModelFile(model));
  }
  
  // -------------------------------------------------------------------------
  // FUNCTIONALITY
  // -------------------------------------------------------------------------

  @Override
  public void accept(final JCas jCas) {
    final FSIterator<Annotation> iterator =
        jCas.getAnnotationIndex(Sentence.type).iterator();
    while (iterator.hasNext()) {
      final Sentence sentence = (Sentence) iterator.next();
      final List<Token> tokens =
          JCasUtil.selectCovered(Token.class, sentence);
      final List<CoreLabel> coreLabels = this.tokensToCoreLabel(tokens);
      final Collection<TypedDependency> dependencies =
          this.getParser().predict(coreLabels).typedDependenciesCollapsed();
      this.addDependencies(tokens, dependencies);
    }
  }
  
  /**
   * Creates {@link CoreLabel}s from {@link Token}s.
   * 
   * @param tokens The tokens
   * @return The CoreLabels
   */
  private List<CoreLabel> tokensToCoreLabel(final List<Token> tokens) {
    final CoreLabelTokenFactory factory = new CoreLabelTokenFactory();
    final List<CoreLabel> coreLabels = new ArrayList<CoreLabel>();
    for (final Token token : tokens) {
      final String text = token.getCoveredText();
      final int begin = token.getBegin();
      final int length = token.getEnd() - begin;
      final CoreLabel coreLabel = factory.makeToken(text, begin, length);
      coreLabel.set(LemmaAnnotation.class, token.getLemma());
      coreLabel.set(StemAnnotation.class, token.getStem());
      coreLabel.set(PartOfSpeechAnnotation.class, token.getPos());
      coreLabel.set(ChunkAnnotation.class, token.getChunk());
      coreLabels.add(coreLabel);
    }
    return coreLabels;
  }
  
  /**
   * Adds the specified dependencies to the tokens of a sentence.
   * <p>
   * Dependencies that have "ROOT" as its governor are not added.
   * </p>
   * @param tokens The tokens from which the complete parse tree was created
   * @param dependencies The dependencies created by the {@link #parser}
   */

  private void addDependencies(
      final List<Token> tokens,
      final Collection<TypedDependency> dependencies) {
    for (final TypedDependency dependency : dependencies) {
      this.addDependency(tokens, dependency);
    }
  }
  
  /**
   * Adds the specified dependency to the tokens of a sentence.
   * <p>
   * If the dependency has "ROOT" as its governor it is not added.
   * </p>
   * @param tokens The tokens from which the complete parse tree was created
   * @param dependency A dependency created by the {@link #parser}
   */

  private void addDependency(
      final List<Token> tokens,
      final TypedDependency dependency) {
    if (dependency.gov().index() == 0)
      return;
    final Token dependent = tokens.get(dependency.dep().index() - 1);
    final Token governor = tokens.get(dependency.gov().index() - 1);
    dependent.setParent(governor);
    dependent.setDepLabel(dependency.reln().getShortName());
  }

}