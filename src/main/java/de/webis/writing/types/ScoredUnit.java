

   
/* Apache UIMA v3 - First created by JCasGen Tue Apr 28 17:07:59 CEST 2020 */

package de.webis.writing.types;
 

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;

import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;


import org.apache.uima.jcas.cas.FSArray;
import de.aitools.commons.uima.supertype.Unit;
import org.apache.uima.jcas.tcas.Annotation;


/** One or more scores have been assigned to this unit by one or more measures. Suggestions may exist of how the unit could be replaced.
 * Updated by JCasGen Thu Aug 27 15:07:42 CEST 2020
 * XML source: /Users/Hanna/Uni/08_Semester/BachelorArbeit/EclipseWorkspace/ConversationalNewsRepo/listenability-tools/src/main/resources/de/webis/writing/types/WritingAssistanceTypeSystem.xml
 * @generated */
public class ScoredUnit extends Annotation {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static String _TypeName = "de.webis.writing.types.ScoredUnit";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(ScoredUnit.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
 
  /* *******************
   *   Feature Offsets *
   * *******************/ 
   
  public final static String _FeatName_scores = "scores";
  public final static String _FeatName_suggestions = "suggestions";
  public final static String _FeatName_unit = "unit";


  /* Feature Adjusted Offsets */
  private final static CallSite _FC_scores = TypeSystemImpl.createCallSite(ScoredUnit.class, "scores");
  private final static MethodHandle _FH_scores = _FC_scores.dynamicInvoker();
  private final static CallSite _FC_suggestions = TypeSystemImpl.createCallSite(ScoredUnit.class, "suggestions");
  private final static MethodHandle _FH_suggestions = _FC_suggestions.dynamicInvoker();
  private final static CallSite _FC_unit = TypeSystemImpl.createCallSite(ScoredUnit.class, "unit");
  private final static MethodHandle _FH_unit = _FC_unit.dynamicInvoker();

   
  /** Never called.  Disable default constructor
   * @generated */
  @Deprecated
  @SuppressWarnings ("deprecation")
  protected ScoredUnit() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public ScoredUnit(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public ScoredUnit(JCas jcas) {
    super(jcas);
    readObject();   
  } 


  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public ScoredUnit(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: scores

  /** getter for scores - gets The assigned scores  (of type de.webis.writing.types.Score).
   * @generated
   * @return value of the feature 
   */
  @SuppressWarnings("unchecked")
  public FSArray<Score> getScores() { 
    return (FSArray<Score>)(_getFeatureValueNc(wrapGetIntCatchException(_FH_scores)));
  }
    
  /** setter for scores - sets The assigned scores  (of type de.webis.writing.types.Score). 
   * @generated
   * @param v value to set into the feature 
   */
  public void setScores(FSArray<Score> v) {
    _setFeatureValueNcWj(wrapGetIntCatchException(_FH_scores), v);
  }    
    
    
  /** indexed getter for scores - gets an indexed value - The assigned scores  (of type de.webis.writing.types.Score).
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  @SuppressWarnings("unchecked")
  public Score getScores(int i) {
     return (Score)(((FSArray<Score>)(_getFeatureValueNc(wrapGetIntCatchException(_FH_scores)))).get(i));
  } 

  /** indexed setter for scores - sets an indexed value - The assigned scores  (of type de.webis.writing.types.Score).
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  @SuppressWarnings("unchecked")
    public void setScores(int i, Score v) {
    ((FSArray<Score>)(_getFeatureValueNc(wrapGetIntCatchException(_FH_scores)))).set(i, v);
  }  
   
    
  //*--------------*
  //* Feature: suggestions

  /** getter for suggestions - gets The suggestions (of type de.webis.writing.types.Suggestion), which could be synonyms (for word or phrase units) or paraphrases (for phrase or longer units).
   * @generated
   * @return value of the feature 
   */
  @SuppressWarnings("unchecked")
  public FSArray<Suggestion> getSuggestions() { 
    return (FSArray<Suggestion>)(_getFeatureValueNc(wrapGetIntCatchException(_FH_suggestions)));
  }
    
  /** setter for suggestions - sets The suggestions (of type de.webis.writing.types.Suggestion), which could be synonyms (for word or phrase units) or paraphrases (for phrase or longer units). 
   * @generated
   * @param v value to set into the feature 
   */
  public void setSuggestions(FSArray<Suggestion> v) {
    _setFeatureValueNcWj(wrapGetIntCatchException(_FH_suggestions), v);
  }    
    
    
  /** indexed getter for suggestions - gets an indexed value - The suggestions (of type de.webis.writing.types.Suggestion), which could be synonyms (for word or phrase units) or paraphrases (for phrase or longer units).
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  @SuppressWarnings("unchecked")
  public Suggestion getSuggestions(int i) {
     return (Suggestion)(((FSArray<Suggestion>)(_getFeatureValueNc(wrapGetIntCatchException(_FH_suggestions)))).get(i));
  } 

  /** indexed setter for suggestions - sets an indexed value - The suggestions (of type de.webis.writing.types.Suggestion), which could be synonyms (for word or phrase units) or paraphrases (for phrase or longer units).
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  @SuppressWarnings("unchecked")
    public void setSuggestions(int i, Suggestion v) {
    ((FSArray<Suggestion>)(_getFeatureValueNc(wrapGetIntCatchException(_FH_suggestions)))).set(i, v);
  }  
   
    
  //*--------------*
  //* Feature: unit

  /** getter for unit - gets The Unit that this ScoredUnit refers to.
   * @generated
   * @return value of the feature 
   */
  public Unit getUnit() { 
    return (Unit)(_getFeatureValueNc(wrapGetIntCatchException(_FH_unit)));
  }
    
  /** setter for unit - sets The Unit that this ScoredUnit refers to. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setUnit(Unit v) {
    _setFeatureValueNcWj(wrapGetIntCatchException(_FH_unit), v);
  }    
    
  }

    