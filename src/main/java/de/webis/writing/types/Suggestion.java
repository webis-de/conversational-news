

   
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
import org.apache.uima.jcas.cas.TOP;


/** A suggestion to replace the original unit with.
 * Updated by JCasGen Thu Aug 27 15:07:42 CEST 2020
 * XML source: /Users/Hanna/Uni/08_Semester/BachelorArbeit/EclipseWorkspace/ConversationalNewsRepo/listenability-tools/src/main/resources/de/webis/writing/types/WritingAssistanceTypeSystem.xml
 * @generated */
public class Suggestion extends TOP {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static String _TypeName = "de.webis.writing.types.Suggestion";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Suggestion.class);
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
   
  public final static String _FeatName_text = "text";
  public final static String _FeatName_scores = "scores";


  /* Feature Adjusted Offsets */
  private final static CallSite _FC_text = TypeSystemImpl.createCallSite(Suggestion.class, "text");
  private final static MethodHandle _FH_text = _FC_text.dynamicInvoker();
  private final static CallSite _FC_scores = TypeSystemImpl.createCallSite(Suggestion.class, "scores");
  private final static MethodHandle _FH_scores = _FC_scores.dynamicInvoker();

   
  /** Never called.  Disable default constructor
   * @generated */
  @Deprecated
  @SuppressWarnings ("deprecation")
  protected Suggestion() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public Suggestion(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Suggestion(JCas jcas) {
    super(jcas);
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
  //* Feature: text

  /** getter for text - gets The text of the suggestion.
   * @generated
   * @return value of the feature 
   */
  public String getText() { 
    return _getStringValueNc(wrapGetIntCatchException(_FH_text));
  }
    
  /** setter for text - sets The text of the suggestion. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setText(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_text), v);
  }    
    
   
    
  //*--------------*
  //* Feature: scores

  /** getter for scores - gets The scores (of type de.webis.writing.types.Score) of this suggestion.
   * @generated
   * @return value of the feature 
   */
  @SuppressWarnings("unchecked")
  public FSArray<Score> getScores() { 
    return (FSArray<Score>)(_getFeatureValueNc(wrapGetIntCatchException(_FH_scores)));
  }
    
  /** setter for scores - sets The scores (of type de.webis.writing.types.Score) of this suggestion. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setScores(FSArray<Score> v) {
    _setFeatureValueNcWj(wrapGetIntCatchException(_FH_scores), v);
  }    
    
    
  /** indexed getter for scores - gets an indexed value - The scores (of type de.webis.writing.types.Score) of this suggestion.
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  @SuppressWarnings("unchecked")
  public Score getScores(int i) {
     return (Score)(((FSArray<Score>)(_getFeatureValueNc(wrapGetIntCatchException(_FH_scores)))).get(i));
  } 

  /** indexed setter for scores - sets an indexed value - The scores (of type de.webis.writing.types.Score) of this suggestion.
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  @SuppressWarnings("unchecked")
    public void setScores(int i, Score v) {
    ((FSArray<Score>)(_getFeatureValueNc(wrapGetIntCatchException(_FH_scores)))).set(i, v);
  }  
  }

    