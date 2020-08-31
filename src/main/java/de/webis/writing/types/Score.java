

   
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


/** A score assigned to some text by some measure.
 * Updated by JCasGen Thu Aug 27 15:07:42 CEST 2020
 * XML source: /Users/Hanna/Uni/08_Semester/BachelorArbeit/EclipseWorkspace/ConversationalNewsRepo/listenability-tools/src/main/resources/de/webis/writing/types/WritingAssistanceTypeSystem.xml
 * @generated */
public class Score extends TOP {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static String _TypeName = "de.webis.writing.types.Score";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Score.class);
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
   
  public final static String _FeatName_name = "name";
  public final static String _FeatName_value = "value";
  public final static String _FeatName_explanations = "explanations";


  /* Feature Adjusted Offsets */
  private final static CallSite _FC_name = TypeSystemImpl.createCallSite(Score.class, "name");
  private final static MethodHandle _FH_name = _FC_name.dynamicInvoker();
  private final static CallSite _FC_value = TypeSystemImpl.createCallSite(Score.class, "value");
  private final static MethodHandle _FH_value = _FC_value.dynamicInvoker();
  private final static CallSite _FC_explanations = TypeSystemImpl.createCallSite(Score.class, "explanations");
  private final static MethodHandle _FH_explanations = _FC_explanations.dynamicInvoker();

   
  /** Never called.  Disable default constructor
   * @generated */
  @Deprecated
  @SuppressWarnings ("deprecation")
  protected Score() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public Score(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Score(JCas jcas) {
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
  //* Feature: name

  /** getter for name - gets The name of the measure that calculated this score for the text.
   * @generated
   * @return value of the feature 
   */
  public String getName() { 
    return _getStringValueNc(wrapGetIntCatchException(_FH_name));
  }
    
  /** setter for name - sets The name of the measure that calculated this score for the text. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setName(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_name), v);
  }    
    
   
    
  //*--------------*
  //* Feature: value

  /** getter for value - gets The value that the measure calculated for the text. The value should usually be between 0 (= worst possible) and 1 (= everything is fine). The score is used to hint at problems, so values above 1 are usually avoided.
   * @generated
   * @return value of the feature 
   */
  public double getValue() { 
    return _getDoubleValueNc(wrapGetIntCatchException(_FH_value));
  }
    
  /** setter for value - sets The value that the measure calculated for the text. The value should usually be between 0 (= worst possible) and 1 (= everything is fine). The score is used to hint at problems, so values above 1 are usually avoided. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setValue(double v) {
    _setDoubleValueNfc(wrapGetIntCatchException(_FH_value), v);
  }    
    
   
    
  //*--------------*
  //* Feature: explanations

  /** getter for explanations - gets The explanations for this score.
   * @generated
   * @return value of the feature 
   */
  @SuppressWarnings("unchecked")
  public FSArray<Explanation> getExplanations() { 
    return (FSArray<Explanation>)(_getFeatureValueNc(wrapGetIntCatchException(_FH_explanations)));
  }
    
  /** setter for explanations - sets The explanations for this score. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setExplanations(FSArray<Explanation> v) {
    _setFeatureValueNcWj(wrapGetIntCatchException(_FH_explanations), v);
  }    
    
    
  /** indexed getter for explanations - gets an indexed value - The explanations for this score.
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  @SuppressWarnings("unchecked")
  public Explanation getExplanations(int i) {
     return (Explanation)(((FSArray<Explanation>)(_getFeatureValueNc(wrapGetIntCatchException(_FH_explanations)))).get(i));
  } 

  /** indexed setter for explanations - sets an indexed value - The explanations for this score.
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  @SuppressWarnings("unchecked")
    public void setExplanations(int i, Explanation v) {
    ((FSArray<Explanation>)(_getFeatureValueNc(wrapGetIntCatchException(_FH_explanations)))).set(i, v);
  }  
  }

    