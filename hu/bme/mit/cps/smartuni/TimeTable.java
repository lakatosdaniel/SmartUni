

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

public class TimeTable   implements Copyable, Serializable{

    public long TimeStamp = (long)0;
    public int SourceID = (int)0;
    public boolean Lecture = (boolean)false;

    public TimeTable() {

    }
    public TimeTable (TimeTable other) {

        this();
        copy_from(other);
    }

    public static Object create() {

        TimeTable self;
        self = new  TimeTable();
        self.clear();
        return self;

    }

    public void clear() {

        TimeStamp = (long)0;
        SourceID = (int)0;
        Lecture = (boolean)false;
    }

    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }        

        if(getClass() != o.getClass()) {
            return false;
        }

        TimeTable otherObj = (TimeTable)o;

        if(TimeStamp != otherObj.TimeStamp) {
            return false;
        }
        if(SourceID != otherObj.SourceID) {
            return false;
        }
        if(Lecture != otherObj.Lecture) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int __result = 0;
        __result += (int)TimeStamp;
        __result += (int)SourceID;
        __result += (Lecture == true)?1:0;
        return __result;
    }

    /**
    * This is the implementation of the <code>Copyable</code> interface.
    * This method will perform a deep copy of <code>src</code>
    * This method could be placed into <code>TimeTableTypeSupport</code>
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

        TimeTable typedSrc = (TimeTable) src;
        TimeTable typedDst = this;

        typedDst.TimeStamp = typedSrc.TimeStamp;
        typedDst.SourceID = typedSrc.SourceID;
        typedDst.Lecture = typedSrc.Lecture;

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
        strBuffer.append("SourceID: ").append(SourceID).append("\n");  
        CdrHelper.printIndent(strBuffer, indent+1);        
        strBuffer.append("Lecture: ").append(Lecture).append("\n");  

        return strBuffer.toString();
    }

}
