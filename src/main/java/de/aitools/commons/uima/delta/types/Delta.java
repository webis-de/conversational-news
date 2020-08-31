

   
/* Apache UIMA v3 - First created by JCasGen Wed Aug 19 14:16:31 CEST 2020 */

package de.aitools.commons.uima.delta.types;
 

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;

import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;


import de.aitools.commons.uima.supertype.Unit;


/** Indicates a unit of text that has been modified.
 * Updated by JCasGen Thu Aug 27 15:07:42 CEST 2020
 * XML source: /Users/Hanna/Uni/08_Semester/BachelorArbeit/EclipseWorkspace/ConversationalNewsRepo/listenability-tools/src/main/resources/de/webis/writing/types/WritingAssistanceTypeSystem.xml
 * @generated */
public class Delta extends Unit {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static String _TypeName = "de.aitools.commons.uima.delta.types.Delta";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Delta.class);
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
   
  public final static String _FeatName_old = "old";


  /* Feature Adjusted Offsets */
  private final static CallSite _FC_old = TypeSystemImpl.createCallSite(Delta.class, "old");
  private final static MethodHandle _FH_old = _FC_old.dynamicInvoker();

   
  /** Never called.  Disable default constructor
   * @generated */
  @Deprecated
  @SuppressWarnings ("deprecation")
  protected Delta() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public Delta(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Delta(JCas jcas) {
    super(jcas);
    readObject();   
  } 


  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Delta(JCas jcas, int begin, int end) {
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
  //* Feature: old

  /** getter for old - gets The text that has been previously at the place of this unit.
   * @generated
   * @return value of the feature 
   */
  public String getOld() { 
    return _getStringValueNc(wrapGetIntCatchException(_FH_old));
  }
    
  /** setter for old - sets The text that has been previously at the place of this unit. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setOld(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_old), v);
  }    
    
  }

    