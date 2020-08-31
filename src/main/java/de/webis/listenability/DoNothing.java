package de.webis.listenability;

import java.util.Properties;

import org.apache.uima.jcas.JCas;

import de.aitools.commons.uima.pipeline.AnalysisEngineComponent;

@Deprecated
public class DoNothing
extends AnalysisEngineComponent {

  @Override
  public void configure(final Properties properties) {}

  @Override
  public void accept(final JCas jCas) {}

}
