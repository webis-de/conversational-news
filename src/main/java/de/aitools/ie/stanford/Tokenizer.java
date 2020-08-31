package de.aitools.ie.stanford;

//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
import java.util.Properties;
import java.util.List;

import org.apache.uima.jcas.JCas;

import de.aitools.commons.uima.pipeline.AnalysisEngineComponent;
import de.aitools.commons.uima.core.Token;
import de.aitools.commons.uima.core.Document;
import de.aitools.commons.uima.core.Sentence;

import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.pipeline.*;

/**
 * TODO
 * 
 * @author johanna.sacher@uni-weimar.de
 *
 */
public class Tokenizer extends AnalysisEngineComponent {

    // -------------------------------------------------------------------------
    // CONSTANTS
    // -------------------------------------------------------------------------
    
    // -------------------------------------------------------------------------
    // MEMBERS
    // -------------------------------------------------------------------------
    private StanfordCoreNLP pipeline;
    // -------------------------------------------------------------------------
    // CONSTRUCTORS
    // -------------------------------------------------------------------------
    
    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------
    
    // -------------------------------------------------------------------------
    // CONFIGURATION
    // -------------------------------------------------------------------------
    
    @Override
    public void configure(final Properties properties) {
        
        //TODO get the annotation types from properties!
        
        //creates a StanfordCoreNLP object, with POS tagging, lemmatization
        pipeline = createStanfordPipeline("annotators", "tokenize, ssplit, pos, lemma");
    }
    
    // -------------------------------------------------------------------------
    // FUNCTIONALITY
    // -------------------------------------------------------------------------
    
    @Override
    public void accept(final JCas jCas) {

        final String text = jCas.getDocumentText();
        
        // add document annotation, which is later used during
        // the feature computation
        final Document document = new Document(jCas, 0, text.length());
        document.addToIndexes();
          
        // create an empty Annotation just with the given text
        Annotation doc = new Annotation(text);
        
        // run all Annotators on this text
        pipeline.annotate(doc);
        
        List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
        
        for(CoreMap sentence: sentences) {
        	
            //create a UIMA Sentence from this information
             createSentence(jCas, sentence);
            
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
            
                //create a UIMA Token from the stanford annotations
                createToken(jCas, token);
            }
        }
    }
      
    private static StanfordCoreNLP createStanfordPipeline(String annotator, String annotators) {
        //creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties props = new Properties();
        props.setProperty(annotator, annotators);
        return new StanfordCoreNLP(props);
    }
      
    private void createToken(JCas jCas, CoreLabel token) {
    	  
        Integer beginIndex = token.beginPosition();
        Integer endIndex = token.endPosition();
          
        final Token resultToken = new Token(jCas, beginIndex, endIndex);
          
        resultToken.setLemma(token.get(LemmaAnnotation.class));
        resultToken.setStem(token.get(StemAnnotation.class));
        resultToken.setPos(token.get(PartOfSpeechAnnotation.class));
        resultToken.setChunk(token.get(ChunkAnnotation.class));
        	      
        //which Morph Annotator?
        //resultToken.setMorph(token.get(Morp??));
          
        //Parent needs a token, not a string
        //resultToken.setParent(token.get(ParentAnnotation.class));
          
        //DepententsAnnotation returns List, but we need Sting
        //resultToken.setDepLabel((token.get(DependentsAnnotation.class));
              
        resultToken.addToIndexes();
    }
    
    private void createSentence(JCas jCas, CoreMap sentence) {
    
        //get the necessary information from the CoreMap that contains the sentence annotations
        int begin = sentence.get(CharacterOffsetBeginAnnotation.class);
        int end = sentence.get(CharacterOffsetEndAnnotation.class);
        //	int index = sentence.get(SentenceIndexAnnotation.class);
        
        //	System.out.println("Sentence: " + sentence.toShorterString());
        //	System.out.println("From " + begin + " to " + end + " with index " + index);
        
        Sentence sentenceObject = new Sentence(jCas, begin, end);
        sentenceObject.addToIndexes();
    	
    }

}
