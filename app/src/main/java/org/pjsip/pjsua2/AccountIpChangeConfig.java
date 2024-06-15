/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.0
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class AccountIpChangeConfig {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected AccountIpChangeConfig(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(AccountIpChangeConfig obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(AccountIpChangeConfig obj) {
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
        pjsua2JNI.delete_AccountIpChangeConfig(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setShutdownTp(boolean value) {
    pjsua2JNI.AccountIpChangeConfig_shutdownTp_set(swigCPtr, this, value);
  }

  public boolean getShutdownTp() {
    return pjsua2JNI.AccountIpChangeConfig_shutdownTp_get(swigCPtr, this);
  }

  public void setHangupCalls(boolean value) {
    pjsua2JNI.AccountIpChangeConfig_hangupCalls_set(swigCPtr, this, value);
  }

  public boolean getHangupCalls() {
    return pjsua2JNI.AccountIpChangeConfig_hangupCalls_get(swigCPtr, this);
  }

  public void setReinviteFlags(long value) {
    pjsua2JNI.AccountIpChangeConfig_reinviteFlags_set(swigCPtr, this, value);
  }

  public long getReinviteFlags() {
    return pjsua2JNI.AccountIpChangeConfig_reinviteFlags_get(swigCPtr, this);
  }

  public void setReinvUseUpdate(long value) {
    pjsua2JNI.AccountIpChangeConfig_reinvUseUpdate_set(swigCPtr, this, value);
  }

  public long getReinvUseUpdate() {
    return pjsua2JNI.AccountIpChangeConfig_reinvUseUpdate_get(swigCPtr, this);
  }

  public void readObject(ContainerNode node) throws Exception {
    pjsua2JNI.AccountIpChangeConfig_readObject(swigCPtr, this, ContainerNode.getCPtr(node), node);
  }

  public void writeObject(ContainerNode node) throws Exception {
    pjsua2JNI.AccountIpChangeConfig_writeObject(swigCPtr, this, ContainerNode.getCPtr(node), node);
  }

  public AccountIpChangeConfig() {
    this(pjsua2JNI.new_AccountIpChangeConfig(), true);
  }

}
