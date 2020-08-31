

   
/* Apache UIMA v3 - First created by JCasGen Tue Apr 28 17:07:59 CEST 2020 */

package de.webis.writing.types;
 

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;

import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;


import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;


/** An explanation for some value or annotation. Explanations are not human-readable text but rather contain the different values (and/or references to other annotations) that are necessary to compose a human-readable explanation text in the client. An explanation is thus just a value (and/or reference) with a name (called key). Explanations can use a value to specify why or how the value and/or the reference to another annotation is an explanation.
 * Updated by JCasGen Thu Aug 27 15:07:42 CEST 2020
 * XML source: /Users/Hanna/Uni/08_Semester/BachelorArbeit/EclipseWorkspace/ConversationalNewsRepo/listenability-tools/src/main/resources/de/webis/writing/types/WritingAssistanceTypeSystem.xml
 * @generated */
public class Explanation extends TOP {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static String _TypeName = "de.webis.writing.types.Explanation";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Explanation.class);
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
   
  public final static String _FeatName_key = "key";
  public final static String _FeatName_value = "value";
  public final static String _FeatName_reference = "reference";


  /* Feature Adjusted Offsets */
  private final static CallSite _FC_key = TypeSystemImpl.createCallSite(Explanation.class, "key");
  private final static MethodHandle _FH_key = _FC_key.dynamicInvoker();
  private final static CallSite _FC_value = TypeSystemImpl.createCallSite(Explanation.class, "value");
  private final static MethodHandle _FH_value = _FC_value.dynamicInvoker();
  private final static CallSite _FC_reference = TypeSystemImpl.createCallSite(Explanation.class, "reference");
  private final static MethodHandle _FH_reference = _FC_reference.dynamicInvoker();

   
  /** Never called.  Disable default constructor
   * @generated */
  @Deprecated
  @SuppressWarnings ("deprecation")
  protected Explanation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public Explanation(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Explanation(JCas jcas) {
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
  //* Feature: key

  /** getter for key - gets A name for the value and/or reference of this explanation. This name will be used to interprete the value (and/or reference) and differentiate between values (and/or references) when the human-readable explanation text is composed.
   * @generated
   * @return value of the feature 
   */
  public String getKey() { 
    return _getStringValueNc(wrapGetIntCatchException(_FH_key));
  }
    
  /** setter for key - sets A name for the value and/or reference of this explanation. This name will be used to interprete the value (and/or reference) and differentiate between values (and/or references) when the human-readable explanation text is composed. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setKey(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_key), v);
  }    
    
   
    
  //*--------------*
  //* Feature: value

  /** getter for value - gets An optional value for this explanation.
   * @generated
   * @return value of the feature 
   */
  public String getValue() { 
    return _getStringValueNc(wrapGetIntCatchException(_FH_value));
  }
    
  /** setter for value - sets An optional value for this explanation. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setValue(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_value), v);
  }    
    
   
    
  //*--------------*
  //* Feature: reference

  /** getter for reference - gets The optional annotation that is referenced by this explanation
   * @generated
   * @return value of the feature 
   */
  public Annotation getReference() { 
    return (Annotation)(_getFeatureValueNc(wrapGetIntCatchException(_FH_reference)));
  }
    
  /** setter for reference - sets The optional annotation that is referenced by this explanation 
   * @generated
   * @param v value to set into the feature 
   */
  public void setReference(Annotation v) {
    _setFeatureValueNcWj(wrapGetIntCatchException(_FH_reference), v);
  }    
    
  }

    