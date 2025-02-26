/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.0
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class Call {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected Call(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(Call obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(Call obj) {
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
        pjsua2JNI.delete_Call(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  protected void swigDirectorDisconnect() {
    swigCMemOwn = false;
    delete();
  }

  public void swigReleaseOwnership() {
    swigCMemOwn = false;
    pjsua2JNI.Call_change_ownership(this, swigCPtr, false);
  }

  public void swigTakeOwnership() {
    swigCMemOwn = true;
    pjsua2JNI.Call_change_ownership(this, swigCPtr, true);
  }

  public Call(Account acc, int call_id) {
    this(pjsua2JNI.new_Call__SWIG_0(Account.getCPtr(acc), acc, call_id), true);
    pjsua2JNI.Call_director_connect(this, swigCPtr, true, true);
  }

  public Call(Account acc) {
    this(pjsua2JNI.new_Call__SWIG_1(Account.getCPtr(acc), acc), true);
    pjsua2JNI.Call_director_connect(this, swigCPtr, true, true);
  }

  public CallInfo getInfo() throws Exception {
    return new CallInfo(pjsua2JNI.Call_getInfo(swigCPtr, this), true);
  }

  public boolean isActive() {
    return pjsua2JNI.Call_isActive(swigCPtr, this);
  }

  public int getId() {
    return pjsua2JNI.Call_getId(swigCPtr, this);
  }

  public static Call lookup(int call_id) {
    long cPtr = pjsua2JNI.Call_lookup(call_id);
    return (cPtr == 0) ? null : new Call(cPtr, false);
  }

  public boolean hasMedia() {
    return pjsua2JNI.Call_hasMedia(swigCPtr, this);
  }

  public Media getMedia(long med_idx) {
    long cPtr = pjsua2JNI.Call_getMedia(swigCPtr, this, med_idx);
    return (cPtr == 0) ? null : new Media(cPtr, false);
  }

  public AudioMedia getAudioMedia(int med_idx) throws Exception {
    return new AudioMedia(pjsua2JNI.Call_getAudioMedia(swigCPtr, this, med_idx), true);
  }

  public VideoMedia getEncodingVideoMedia(int med_idx) throws Exception {
    return new VideoMedia(pjsua2JNI.Call_getEncodingVideoMedia(swigCPtr, this, med_idx), true);
  }

  public VideoMedia getDecodingVideoMedia(int med_idx) throws Exception {
    return new VideoMedia(pjsua2JNI.Call_getDecodingVideoMedia(swigCPtr, this, med_idx), true);
  }

  public int remoteHasCap(int htype, String hname, String token) {
    return pjsua2JNI.Call_remoteHasCap(swigCPtr, this, htype, hname, token);
  }

  public void setUserData(SWIGTYPE_p_void user_data) {
    pjsua2JNI.Call_setUserData(swigCPtr, this, SWIGTYPE_p_void.getCPtr(user_data));
  }

  public SWIGTYPE_p_void getUserData() {
    long cPtr = pjsua2JNI.Call_getUserData(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public int getRemNatType() throws Exception {
    return pjsua2JNI.Call_getRemNatType(swigCPtr, this);
  }

  public void makeCall(String dst_uri, CallOpParam prm) throws Exception {
    pjsua2JNI.Call_makeCall(swigCPtr, this, dst_uri, CallOpParam.getCPtr(prm), prm);
  }

  public void answer(CallOpParam prm) throws Exception {
    pjsua2JNI.Call_answer(swigCPtr, this, CallOpParam.getCPtr(prm), prm);
  }

  public void hangup(CallOpParam prm) throws Exception {
    pjsua2JNI.Call_hangup(swigCPtr, this, CallOpParam.getCPtr(prm), prm);
  }

  public void setHold(CallOpParam prm) throws Exception {
    pjsua2JNI.Call_setHold(swigCPtr, this, CallOpParam.getCPtr(prm), prm);
  }

  public void reinvite(CallOpParam prm) throws Exception {
    pjsua2JNI.Call_reinvite(swigCPtr, this, CallOpParam.getCPtr(prm), prm);
  }

  public void update(CallOpParam prm) throws Exception {
    pjsua2JNI.Call_update(swigCPtr, this, CallOpParam.getCPtr(prm), prm);
  }

  public void xfer(String dest, CallOpParam prm) throws Exception {
    pjsua2JNI.Call_xfer(swigCPtr, this, dest, CallOpParam.getCPtr(prm), prm);
  }

  public void xferReplaces(Call dest_call, CallOpParam prm) throws Exception {
    pjsua2JNI.Call_xferReplaces(swigCPtr, this, Call.getCPtr(dest_call), dest_call, CallOpParam.getCPtr(prm), prm);
  }

  public void processRedirect(int cmd) throws Exception {
    pjsua2JNI.Call_processRedirect(swigCPtr, this, cmd);
  }

  public void dialDtmf(String digits) throws Exception {
    pjsua2JNI.Call_dialDtmf(swigCPtr, this, digits);
  }

  public void sendDtmf(CallSendDtmfParam param) throws Exception {
    pjsua2JNI.Call_sendDtmf(swigCPtr, this, CallSendDtmfParam.getCPtr(param), param);
  }

  public void sendInstantMessage(SendInstantMessageParam prm) throws Exception {
    pjsua2JNI.Call_sendInstantMessage(swigCPtr, this, SendInstantMessageParam.getCPtr(prm), prm);
  }

  public void sendTypingIndication(SendTypingIndicationParam prm) throws Exception {
    pjsua2JNI.Call_sendTypingIndication(swigCPtr, this, SendTypingIndicationParam.getCPtr(prm), prm);
  }

  public void sendRequest(CallSendRequestParam prm) throws Exception {
    pjsua2JNI.Call_sendRequest(swigCPtr, this, CallSendRequestParam.getCPtr(prm), prm);
  }

  public String dump(boolean with_media, String indent) throws Exception {
    return pjsua2JNI.Call_dump(swigCPtr, this, with_media, indent);
  }

  public int vidGetStreamIdx() {
    return pjsua2JNI.Call_vidGetStreamIdx(swigCPtr, this);
  }

  public boolean vidStreamIsRunning(int med_idx, int dir) {
    return pjsua2JNI.Call_vidStreamIsRunning(swigCPtr, this, med_idx, dir);
  }

  public void vidSetStream(int op, CallVidSetStreamParam param) throws Exception {
    pjsua2JNI.Call_vidSetStream(swigCPtr, this, op, CallVidSetStreamParam.getCPtr(param), param);
  }

  public void vidStreamModifyCodecParam(int med_idx, VidCodecParam param) throws Exception {
    pjsua2JNI.Call_vidStreamModifyCodecParam(swigCPtr, this, med_idx, VidCodecParam.getCPtr(param), param);
  }

  public void audStreamModifyCodecParam(int med_idx, CodecParam param) throws Exception {
    pjsua2JNI.Call_audStreamModifyCodecParam(swigCPtr, this, med_idx, CodecParam.getCPtr(param), param);
  }

  public StreamInfo getStreamInfo(long med_idx) throws Exception {
    return new StreamInfo(pjsua2JNI.Call_getStreamInfo(swigCPtr, this, med_idx), true);
  }

  public StreamStat getStreamStat(long med_idx) throws Exception {
    return new StreamStat(pjsua2JNI.Call_getStreamStat(swigCPtr, this, med_idx), true);
  }

  public MediaTransportInfo getMedTransportInfo(long med_idx) throws Exception {
    return new MediaTransportInfo(pjsua2JNI.Call_getMedTransportInfo(swigCPtr, this, med_idx), true);
  }

  public void processMediaUpdate(OnCallMediaStateParam prm) {
    pjsua2JNI.Call_processMediaUpdate(swigCPtr, this, OnCallMediaStateParam.getCPtr(prm), prm);
  }

  public void processStateChange(OnCallStateParam prm) {
    pjsua2JNI.Call_processStateChange(swigCPtr, this, OnCallStateParam.getCPtr(prm), prm);
  }

  public void onCallState(OnCallStateParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallState(swigCPtr, this, OnCallStateParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallStateSwigExplicitCall(swigCPtr, this, OnCallStateParam.getCPtr(prm), prm);
  }

  public void onCallTsxState(OnCallTsxStateParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallTsxState(swigCPtr, this, OnCallTsxStateParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallTsxStateSwigExplicitCall(swigCPtr, this, OnCallTsxStateParam.getCPtr(prm), prm);
  }

  public void onCallMediaState(OnCallMediaStateParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallMediaState(swigCPtr, this, OnCallMediaStateParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallMediaStateSwigExplicitCall(swigCPtr, this, OnCallMediaStateParam.getCPtr(prm), prm);
  }

  public void onCallSdpCreated(OnCallSdpCreatedParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallSdpCreated(swigCPtr, this, OnCallSdpCreatedParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallSdpCreatedSwigExplicitCall(swigCPtr, this, OnCallSdpCreatedParam.getCPtr(prm), prm);
  }

  public void onStreamPreCreate(OnStreamPreCreateParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onStreamPreCreate(swigCPtr, this, OnStreamPreCreateParam.getCPtr(prm), prm); else pjsua2JNI.Call_onStreamPreCreateSwigExplicitCall(swigCPtr, this, OnStreamPreCreateParam.getCPtr(prm), prm);
  }

  public void onStreamCreated(OnStreamCreatedParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onStreamCreated(swigCPtr, this, OnStreamCreatedParam.getCPtr(prm), prm); else pjsua2JNI.Call_onStreamCreatedSwigExplicitCall(swigCPtr, this, OnStreamCreatedParam.getCPtr(prm), prm);
  }

  public void onStreamDestroyed(OnStreamDestroyedParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onStreamDestroyed(swigCPtr, this, OnStreamDestroyedParam.getCPtr(prm), prm); else pjsua2JNI.Call_onStreamDestroyedSwigExplicitCall(swigCPtr, this, OnStreamDestroyedParam.getCPtr(prm), prm);
  }

  public void onDtmfDigit(OnDtmfDigitParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onDtmfDigit(swigCPtr, this, OnDtmfDigitParam.getCPtr(prm), prm); else pjsua2JNI.Call_onDtmfDigitSwigExplicitCall(swigCPtr, this, OnDtmfDigitParam.getCPtr(prm), prm);
  }

  public void onDtmfEvent(OnDtmfEventParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onDtmfEvent(swigCPtr, this, OnDtmfEventParam.getCPtr(prm), prm); else pjsua2JNI.Call_onDtmfEventSwigExplicitCall(swigCPtr, this, OnDtmfEventParam.getCPtr(prm), prm);
  }

  public void onCallTransferRequest(OnCallTransferRequestParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallTransferRequest(swigCPtr, this, OnCallTransferRequestParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallTransferRequestSwigExplicitCall(swigCPtr, this, OnCallTransferRequestParam.getCPtr(prm), prm);
  }

  public void onCallTransferStatus(OnCallTransferStatusParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallTransferStatus(swigCPtr, this, OnCallTransferStatusParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallTransferStatusSwigExplicitCall(swigCPtr, this, OnCallTransferStatusParam.getCPtr(prm), prm);
  }

  public void onCallReplaceRequest(OnCallReplaceRequestParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallReplaceRequest(swigCPtr, this, OnCallReplaceRequestParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallReplaceRequestSwigExplicitCall(swigCPtr, this, OnCallReplaceRequestParam.getCPtr(prm), prm);
  }

  public void onCallReplaced(OnCallReplacedParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallReplaced(swigCPtr, this, OnCallReplacedParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallReplacedSwigExplicitCall(swigCPtr, this, OnCallReplacedParam.getCPtr(prm), prm);
  }

  public void onCallRxOffer(OnCallRxOfferParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallRxOffer(swigCPtr, this, OnCallRxOfferParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallRxOfferSwigExplicitCall(swigCPtr, this, OnCallRxOfferParam.getCPtr(prm), prm);
  }

  public void onCallRxReinvite(OnCallRxReinviteParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallRxReinvite(swigCPtr, this, OnCallRxReinviteParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallRxReinviteSwigExplicitCall(swigCPtr, this, OnCallRxReinviteParam.getCPtr(prm), prm);
  }

  public void onCallTxOffer(OnCallTxOfferParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallTxOffer(swigCPtr, this, OnCallTxOfferParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallTxOfferSwigExplicitCall(swigCPtr, this, OnCallTxOfferParam.getCPtr(prm), prm);
  }

  public void onInstantMessage(OnInstantMessageParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onInstantMessage(swigCPtr, this, OnInstantMessageParam.getCPtr(prm), prm); else pjsua2JNI.Call_onInstantMessageSwigExplicitCall(swigCPtr, this, OnInstantMessageParam.getCPtr(prm), prm);
  }

  public void onInstantMessageStatus(OnInstantMessageStatusParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onInstantMessageStatus(swigCPtr, this, OnInstantMessageStatusParam.getCPtr(prm), prm); else pjsua2JNI.Call_onInstantMessageStatusSwigExplicitCall(swigCPtr, this, OnInstantMessageStatusParam.getCPtr(prm), prm);
  }

  public void onTypingIndication(OnTypingIndicationParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onTypingIndication(swigCPtr, this, OnTypingIndicationParam.getCPtr(prm), prm); else pjsua2JNI.Call_onTypingIndicationSwigExplicitCall(swigCPtr, this, OnTypingIndicationParam.getCPtr(prm), prm);
  }

  public int onCallRedirected(OnCallRedirectedParam prm) {
    return (getClass() == Call.class) ? pjsua2JNI.Call_onCallRedirected(swigCPtr, this, OnCallRedirectedParam.getCPtr(prm), prm) : pjsua2JNI.Call_onCallRedirectedSwigExplicitCall(swigCPtr, this, OnCallRedirectedParam.getCPtr(prm), prm);
  }

  public void onCallMediaTransportState(OnCallMediaTransportStateParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallMediaTransportState(swigCPtr, this, OnCallMediaTransportStateParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallMediaTransportStateSwigExplicitCall(swigCPtr, this, OnCallMediaTransportStateParam.getCPtr(prm), prm);
  }

  public void onCallMediaEvent(OnCallMediaEventParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCallMediaEvent(swigCPtr, this, OnCallMediaEventParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCallMediaEventSwigExplicitCall(swigCPtr, this, OnCallMediaEventParam.getCPtr(prm), prm);
  }

  public void onCreateMediaTransport(OnCreateMediaTransportParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCreateMediaTransport(swigCPtr, this, OnCreateMediaTransportParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCreateMediaTransportSwigExplicitCall(swigCPtr, this, OnCreateMediaTransportParam.getCPtr(prm), prm);
  }

  public void onCreateMediaTransportSrtp(OnCreateMediaTransportSrtpParam prm) {
    if (getClass() == Call.class) pjsua2JNI.Call_onCreateMediaTransportSrtp(swigCPtr, this, OnCreateMediaTransportSrtpParam.getCPtr(prm), prm); else pjsua2JNI.Call_onCreateMediaTransportSrtpSwigExplicitCall(swigCPtr, this, OnCreateMediaTransportSrtpParam.getCPtr(prm), prm);
  }

}
