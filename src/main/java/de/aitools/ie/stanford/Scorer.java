package de.aitools.ie.stanford;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.aitools.commons.uima.core.Token;
import de.aitools.commons.uima.pipeline.AnalysisEngineComponent;
import de.webis.writing.Scores;


/**
 * @author johanna.sacher@uni-weimar.de
 */

public class Scorer extends AnalysisEngineComponent{
    
    // -------------------------------------------------------------------------
    // CONSTANTS
    // -------------------------------------------------------------------------
    private final String NORM_WORD = "normalizationWord";
    private final String FREQUENCIES_DATA = "frequencyDataPath";
    
    // -------------------------------------------------------------------------
    // MEMBERS
    // -------------------------------------------------------------------------
    private Double defaultNorm = 1.0;
    private String normalizationWord;
    private HashMap<String, Double> frequencies = new HashMap<String, Double>();
    
    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------
    private List<Double> readInFrequencies(String filename) {
        BufferedReader br;
        List<Double> frequs = new ArrayList<Double>();
        try {
            br = new BufferedReader(new FileReader(filename));
            String line;
            double total = 0;
          
             while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                try {
                    //this is file specific - adjust if other data is used (or adjust the data itself)
                    total = Double.parseDouble(values[1]);
                    
                    //alternativ ohne die if Bedingung?
                    if(!frequs.contains(total)) {
                        frequs.add(total);
                    }
                    
                }catch (NumberFormatException e) {
                    System.out.println("numberStr " + e + " is not a number");
                }
                frequencies.put(values[0], total);
            }
        } catch (FileNotFoundException e) {
            System.out.println("csv file not found");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("could not read line in frequencies-csv");
            e.printStackTrace();
        }
        return frequs;
        //printFrequencies();
    }
    
    @SuppressWarnings("unused")
    private void printFrequencies() {   
        for(String word : frequencies.keySet()) {
            System.out.println("word: " + word + ", total: " + frequencies.get(word));
        }
        System.out.println("Total Number of Entries: " + frequencies.size());
    }

    // -------------------------------------------------------------------------
    // CONFIGURATION
    // -------------------------------------------------------------------------
    
    @Override
    public void configure(Properties properties) {
        
        normalizationWord = properties.getProperty(NORM_WORD);
        List<Double> frequencies = readInFrequencies(properties.getProperty(FREQUENCIES_DATA));
        
        determineMiddleFrequency(frequencies);
        normalizeScores();
    }
    
    private void determineMiddleFrequency(List<Double> frequencies) {
        Collections.sort(frequencies);
        defaultNorm = frequencies.get(frequencies.size()/2);
    }
    
    private void normalizeScores() {
        //TODO The scores are currently displayed in the scientific notation,
        //they need formating later IF we want to use/display them
        
        Double normFrequ = frequencies.get(normalizationWord);
        if(normFrequ.equals(null)) {
            normFrequ = defaultNorm;
        }
        
        //normalize all scores by the set frequency
        for(String word : frequencies.keySet()) {
            frequencies.put(word, frequencies.get(word)/normFrequ);
        }
        //printFrequencies();
    }
    
    // -------------------------------------------------------------------------
    // FUNCTIONALITY
    // -------------------------------------------------------------------------
    
    @Override
    public void accept(JCas jCas) {
        FSIterator<Annotation> tokenIter = 
        jCas.getAnnotationIndex(Token.type).iterator();
        
        while (tokenIter.hasNext()){
            Token token = (Token) tokenIter.next();
            this.scoreToken(jCas, token);
        }		
    }
    
    /**
     * Scores the given token with a frequency
     * 
     * @param jCas The JCas Object that contains the token
     * @param token Token that is supposed to be scored
     */
    private void scoreToken(JCas jCas, Token token) {
        
        //String word =   token.getCoveredText();
    	String lemma = token.getLemma();
        double score = 0;
        
        if(frequencies.get(lemma) != null) {
            score = frequencies.get(lemma);
        }
        
        //Do not add a score for tokens that would get a score above or equal to 1
        if(score < 1) {
            Scores.add(token, lemma, score);
        }
        //System.out.println("lemma: " + word + ", Score: " + score);
    }

}
