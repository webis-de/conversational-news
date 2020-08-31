

   
/* Apache UIMA v3 - First created by JCasGen Thu Jul 16 18:02:14 CEST 2020 */

package de.webis.writing.types;
 


import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

import de.aitools.commons.uima.supertype.Unit;


import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;


/** Information on the discourse marker type, if it is a phrase and the whole discourse marker
 * Updated by JCasGen Thu Aug 27 15:07:42 CEST 2020
 * XML source: /Users/Hanna/Uni/08_Semester/BachelorArbeit/EclipseWorkspace/ConversationalNewsRepo/listenability-tools/src/main/resources/de/webis/writing/types/WritingAssistanceTypeSystem.xml
 * @generated */
public class DiscourseMarker extends Unit {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static String _TypeName = "de.webis.writing.types.DiscourseMarker";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(DiscourseMarker.class);
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
   
  public final static String _FeatName_isPhrase = "isPhrase";
  public final static String _FeatName_discourseMarker = "discourseMarker";


  public final static String _FeatName_markerType = "markerType";


  /* Feature Adjusted Offsets */
  private final static CallSite _FC_isPhrase = TypeSystemImpl.createCallSite(DiscourseMarker.class, "isPhrase");
  private final static MethodHandle _FH_isPhrase = _FC_isPhrase.dynamicInvoker();
  private final static CallSite _FC_discourseMarker = TypeSystemImpl.createCallSite(DiscourseMarker.class, "discourseMarker");
  private final static MethodHandle _FH_discourseMarker = _FC_discourseMarker.dynamicInvoker();

   
  private final static CallSite _FC_markerType = TypeSystemImpl.createCallSite(DiscourseMarker.class, "markerType");
  private final static MethodHandle _FH_markerType = _FC_markerType.dynamicInvoker();

   
  /* *******************
   *   Feature Offsets *
   * *******************/ 

  
  /* Feature Adjusted Offsets */

   
  /** Never called.  Disable default constructor
   * @generated */
  @Deprecated
  @SuppressWarnings ("deprecation")
  protected DiscourseMarker() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public DiscourseMarker(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public DiscourseMarker(JCas jcas) {
    super(jcas);
    readObject();   
  } 


  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public DiscourseMarker(JCas jcas, int begin, int end) {
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
  //* Feature: isPhrase

  /** getter for isPhrase - gets true if it is a discourse marker consisting of more than two words.
   * @generated
   * @return value of the feature 
   */
  public boolean getIsPhrase() { 
    return _getBooleanValueNc(wrapGetIntCatchException(_FH_isPhrase));
  }
    
  /** setter for isPhrase - sets true if it is a discourse marker consisting of more than two words. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setIsPhrase(boolean v) {
    _setBooleanValueNfc(wrapGetIntCatchException(_FH_isPhrase), v);
  }    
    
   
    
  //*--------------*
  //* Feature: discourseMarker

  /** getter for discourseMarker - gets The discourse marker as string.
   * @generated
   * @return value of the feature 
   */
  public String getDiscourseMarker() { 
    return _getStringValueNc(wrapGetIntCatchException(_FH_discourseMarker));
  }
    
  /** setter for discourseMarker - sets The discourse marker as string. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setDiscourseMarker(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_discourseMarker), v);
  }    
    
   
    
  //*--------------*
  //* Feature: markerType

  /** getter for markerType - gets The type of the discourse marker
   * @generated
   * @return value of the feature 
   */
  public String getMarkerType() { 
    return _getStringValueNc(wrapGetIntCatchException(_FH_markerType));
  }
    
  /** setter for markerType - sets The type of the discourse marker 
   * @generated
   * @param v value to set into the feature 
   */
  public void setMarkerType(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_markerType), v);
  }    
    
  }

    