

/*
WARNING: THIS FILE IS AUTO-GENERATED. DO NOT MODIFY.

This file was generated from .idl using "rtiddsgen".
The rtiddsgen tool is part of the RTI Connext distribution.
For more information, type 'rtiddsgen -help' at a command shell
or consult the RTI Connext manual.
*/

package hu.bme.mit.cps.smartuni;

import com.rti.dds.infrastructure.*;
import com.rti.dds.infrastructure.Copyable;
import java.io.Serializable;
import com.rti.dds.cdr.CdrHelper;

public class WindowState   implements Copyable, Serializable{

    public long TimeStamp = (long)0;
    public int SensorID = (int)0;
    public boolean IsOpen = (boolean)false;

    public WindowState() {

    }
    public WindowState (WindowState other) {

        this();
        copy_from(other);
    }

    public static Object create() {

        WindowState self;
        self = new  WindowState();
        self.clear();
        return self;

    }

    public void clear() {

        TimeStamp = (long)0;
        SensorID = (int)0;
        IsOpen = (boolean)false;
    }

    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }        

        if(getClass() != o.getClass()) {
            return false;
        }

        WindowState otherObj = (WindowState)o;

        if(TimeStamp != otherObj.TimeStamp) {
            return false;
        }
        if(SensorID != otherObj.SensorID) {
            return false;
        }
        if(IsOpen != otherObj.IsOpen) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int __result = 0;
        __result += (int)TimeStamp;
        __result += (int)SensorID;
        __result += (IsOpen == true)?1:0;
        return __result;
    }

    /**
    * This is the implementation of the <code>Copyable</code> interface.
    * This method will perform a deep copy of <code>src</code>
    * This method could be placed into <code>WindowStateTypeSupport</code>
    * rather than here by using the <code>-noCopyable</code> option
    * to rtiddsgen.
    * 
    * @param src The Object which contains the data to be copied.
    * @return Returns <code>this</code>.
    * @exception NullPointerException If <code>src</code> is null.
    * @exception ClassCastException If <code>src</code> is not the 
    * same type as <code>this</code>.
    * @see com.rti.dds.infrastructure.Copyable#copy_from(java.lang.Object)
    */
    public Object copy_from(Object src) {

        WindowState typedSrc = (WindowState) src;
        WindowState typedDst = this;

        typedDst.TimeStamp = typedSrc.TimeStamp;
        typedDst.SensorID = typedSrc.SensorID;
        typedDst.IsOpen = typedSrc.IsOpen;

        return this;
    }

    public String toString(){
        return toString("", 0);
    }

    public String toString(String desc, int indent) {
        StringBuffer strBuffer = new StringBuffer();        

        if (desc != null) {
            CdrHelper.printIndent(strBuffer, indent);
            strBuffer.append(desc).append(":\n");
        }

        CdrHelper.printIndent(strBuffer, indent+1);        
        strBuffer.append("TimeStamp: ").append(TimeStamp).append("\n");  
        CdrHelper.printIndent(strBuffer, indent+1);        
        strBuffer.append("SensorID: ").append(SensorID).append("\n");  
        CdrHelper.printIndent(strBuffer, indent+1);        
        strBuffer.append("IsOpen: ").append(IsOpen).append("\n");  

        return strBuffer.toString();
    }

}
