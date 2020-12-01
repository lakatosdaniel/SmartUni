

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

public class PredictedTemperature   implements Copyable, Serializable{

    public long TimeStamp = (long)0;
    public hu.bme.mit.cps.smartuni.PredictionKind Prediction = (hu.bme.mit.cps.smartuni.PredictionKind)hu.bme.mit.cps.smartuni.PredictionKind.valueOf(0);

    public PredictedTemperature() {

    }
    public PredictedTemperature (PredictedTemperature other) {

        this();
        copy_from(other);
    }

    public static Object create() {

        PredictedTemperature self;
        self = new  PredictedTemperature();
        self.clear();
        return self;

    }

    public void clear() {

        TimeStamp = (long)0;
        Prediction = hu.bme.mit.cps.smartuni.PredictionKind.valueOf(0);
    }

    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }        

        if(getClass() != o.getClass()) {
            return false;
        }

        PredictedTemperature otherObj = (PredictedTemperature)o;

        if(TimeStamp != otherObj.TimeStamp) {
            return false;
        }
        if(!Prediction.equals(otherObj.Prediction)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int __result = 0;
        __result += (int)TimeStamp;
        __result += Prediction.hashCode(); 
        return __result;
    }

    /**
    * This is the implementation of the <code>Copyable</code> interface.
    * This method will perform a deep copy of <code>src</code>
    * This method could be placed into <code>PredictedTemperatureTypeSupport</code>
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

        PredictedTemperature typedSrc = (PredictedTemperature) src;
        PredictedTemperature typedDst = this;

        typedDst.TimeStamp = typedSrc.TimeStamp;
        typedDst.Prediction = (hu.bme.mit.cps.smartuni.PredictionKind) typedDst.Prediction.copy_from(typedSrc.Prediction);

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
        strBuffer.append(Prediction.toString("Prediction ", indent+1));

        return strBuffer.toString();
    }

}
