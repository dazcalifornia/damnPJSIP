/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.0
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class VideoWindow {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected VideoWindow(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(VideoWindow obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(VideoWindow obj) {
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
        pjsua2JNI.delete_VideoWindow(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public VideoWindow(int win_id) {
    this(pjsua2JNI.new_VideoWindow(win_id), true);
  }

  public VideoWindowInfo getInfo() throws Exception {
    return new VideoWindowInfo(pjsua2JNI.VideoWindow_getInfo(swigCPtr, this), true);
  }

  public VideoMedia getVideoMedia() throws Exception {
    return new VideoMedia(pjsua2JNI.VideoWindow_getVideoMedia(swigCPtr, this), true);
  }

  public void Show(boolean show) throws Exception {
    pjsua2JNI.VideoWindow_Show(swigCPtr, this, show);
  }

  public void setPos(MediaCoordinate pos) throws Exception {
    pjsua2JNI.VideoWindow_setPos(swigCPtr, this, MediaCoordinate.getCPtr(pos), pos);
  }

  public void setSize(MediaSize size) throws Exception {
    pjsua2JNI.VideoWindow_setSize(swigCPtr, this, MediaSize.getCPtr(size), size);
  }

  public void rotate(int angle) throws Exception {
    pjsua2JNI.VideoWindow_rotate(swigCPtr, this, angle);
  }

  public void setWindow(VideoWindowHandle win) throws Exception {
    pjsua2JNI.VideoWindow_setWindow(swigCPtr, this, VideoWindowHandle.getCPtr(win), win);
  }

  public void setFullScreen(boolean enabled) throws Exception {
    pjsua2JNI.VideoWindow_setFullScreen(swigCPtr, this, enabled);
  }

  public void setFullScreen2(int mode) throws Exception {
    pjsua2JNI.VideoWindow_setFullScreen2(swigCPtr, this, mode);
  }

}
