package de.aitools.commons.uima.delta;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.aitools.commons.uima.pipeline.AnalysisEngineComponent;

public abstract class DeltaAnalysisEngineComponent
extends AnalysisEngineComponent {

  @Override
  public void accept(final JCas jCas) {
    // TODO Auto-generated method stub
    
  }

  public abstract boolean isLocalToSentence();

  public abstract boolean isLocalToParagraph();

  /**
   * 
   * @param annotation
   * @param inParagraph
   * @param inSentence
   * @return
   */
  public abstract boolean keep(final Annotation annotation,
      final boolean inParagraph, final boolean inSentence);

}


/*
 * {
 *   "xmi": "...",
 *   "deltas" : [
 *     {
 *       "begin": ...,
 *       "end": ...,
 *       "new": "..."
 *     }, ...
 *   ],
 *   "state" : ... // arbitrary JSON that is again returned with the response 
 * }
 */