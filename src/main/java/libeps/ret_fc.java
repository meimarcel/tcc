/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.1
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package libeps;

public class ret_fc {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected ret_fc(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ret_fc obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  @SuppressWarnings("deprecation")
  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        libepsJNI.delete_ret_fc(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setConv(int value) {
    libepsJNI.ret_fc_conv_set(swigCPtr, this, value);
  }

  public int getConv() {
    return libepsJNI.ret_fc_conv_get(swigCPtr, this);
  }

  public void setNIt(int value) {
    libepsJNI.ret_fc_nIt_set(swigCPtr, this, value);
  }

  public int getNIt() {
    return libepsJNI.ret_fc_nIt_get(swigCPtr, this);
  }

  public void setKg(double value) {
    libepsJNI.ret_fc_Kg_set(swigCPtr, this, value);
  }

  public double getKg() {
    return libepsJNI.ret_fc_Kg_get(swigCPtr, this);
  }

  public ret_fc() {
    this(libepsJNI.new_ret_fc(), true);
  }

}
