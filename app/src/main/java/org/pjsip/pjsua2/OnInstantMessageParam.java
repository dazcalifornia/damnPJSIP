/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.0
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class OnInstantMessageParam {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected OnInstantMessageParam(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(OnInstantMessageParam obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(OnInstantMessageParam obj) {
    long ptr = 0;
    if (obj != null) {
      if (!obj.swigCMemOwn)
        throw new RuntimeException("Cannot release ownership as memory is not owned");
      ptr = obj.swigCPtr;
      obj.swigCMemOwn = false;
      obj.delete();
    }
    return ptr;
  }

  @SuppressWarnings("deprecation")
  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        pjsua2JNI.delete_OnInstantMessageParam(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setFromUri(String value) {
    pjsua2JNI.OnInstantMessageParam_fromUri_set(swigCPtr, this, value);
  }

  public String getFromUri() {
    return pjsua2JNI.OnInstantMessageParam_fromUri_get(swigCPtr, this);
  }

  public void setToUri(String value) {
    pjsua2JNI.OnInstantMessageParam_toUri_set(swigCPtr, this, value);
  }

  public String getToUri() {
    return pjsua2JNI.OnInstantMessageParam_toUri_get(swigCPtr, this);
  }

  public void setContactUri(String value) {
    pjsua2JNI.OnInstantMessageParam_contactUri_set(swigCPtr, this, value);
  }

  public String getContactUri() {
    return pjsua2JNI.OnInstantMessageParam_contactUri_get(swigCPtr, this);
  }

  public void setContentType(String value) {
    pjsua2JNI.OnInstantMessageParam_contentType_set(swigCPtr, this, value);
  }

  public String getContentType() {
    return pjsua2JNI.OnInstantMessageParam_contentType_get(swigCPtr, this);
  }

  public void setMsgBody(String value) {
    pjsua2JNI.OnInstantMessageParam_msgBody_set(swigCPtr, this, value);
  }

  public String getMsgBody() {
    return pjsua2JNI.OnInstantMessageParam_msgBody_get(swigCPtr, this);
  }

  public void setRdata(SipRxData value) {
    pjsua2JNI.OnInstantMessageParam_rdata_set(swigCPtr, this, SipRxData.getCPtr(value), value);
  }

  public SipRxData getRdata() {
    long cPtr = pjsua2JNI.OnInstantMessageParam_rdata_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SipRxData(cPtr, false);
  }

  public OnInstantMessageParam() {
    this(pjsua2JNI.new_OnInstantMessageParam(), true);
  }

}
