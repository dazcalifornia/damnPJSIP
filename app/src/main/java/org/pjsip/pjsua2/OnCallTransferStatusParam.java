/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.0
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class OnCallTransferStatusParam {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected OnCallTransferStatusParam(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(OnCallTransferStatusParam obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(OnCallTransferStatusParam obj) {
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
        pjsua2JNI.delete_OnCallTransferStatusParam(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setStatusCode(int value) {
    pjsua2JNI.OnCallTransferStatusParam_statusCode_set(swigCPtr, this, value);
  }

  public int getStatusCode() {
    return pjsua2JNI.OnCallTransferStatusParam_statusCode_get(swigCPtr, this);
  }

  public void setReason(String value) {
    pjsua2JNI.OnCallTransferStatusParam_reason_set(swigCPtr, this, value);
  }

  public String getReason() {
    return pjsua2JNI.OnCallTransferStatusParam_reason_get(swigCPtr, this);
  }

  public void setFinalNotify(boolean value) {
    pjsua2JNI.OnCallTransferStatusParam_finalNotify_set(swigCPtr, this, value);
  }

  public boolean getFinalNotify() {
    return pjsua2JNI.OnCallTransferStatusParam_finalNotify_get(swigCPtr, this);
  }

  public void setCont(boolean value) {
    pjsua2JNI.OnCallTransferStatusParam_cont_set(swigCPtr, this, value);
  }

  public boolean getCont() {
    return pjsua2JNI.OnCallTransferStatusParam_cont_get(swigCPtr, this);
  }

  public OnCallTransferStatusParam() {
    this(pjsua2JNI.new_OnCallTransferStatusParam(), true);
  }

}
