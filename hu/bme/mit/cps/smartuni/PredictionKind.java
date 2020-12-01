

/*
WARNING: THIS FILE IS AUTO-GENERATED. DO NOT MODIFY.

This file was generated from .idl using "rtiddsgen".
The rtiddsgen tool is part of the RTI Connext distribution.
For more information, type 'rtiddsgen -help' at a command shell
or consult the RTI Connext manual.
*/

package hu.bme.mit.cps.smartuni;

import com.rti.dds.util.Enum;
import com.rti.dds.cdr.CdrHelper;
import java.util.Arrays;
import java.io.ObjectStreamException;

public class PredictionKind  extends Enum {

    public static final PredictionKind STAGNATE = new PredictionKind("STAGNATE", 0);
    public static final int _STAGNATE = 0;
    public static final PredictionKind INCREASE = new PredictionKind("INCREASE", 1);
    public static final int _INCREASE = 1;
    public static final PredictionKind DECREASE = new PredictionKind("DECREASE", 2);
    public static final int _DECREASE = 2;
    public static PredictionKind valueOf(int ordinal) {
        switch(ordinal) {

            case 0: return PredictionKind.STAGNATE;
            case 1: return PredictionKind.INCREASE;
            case 2: return PredictionKind.DECREASE;

        }
        return null;
    }

    public static PredictionKind from_int(int __value) {
        return valueOf(__value);
    }

    public static int[] getOrdinals() {
        int i = 0;
        int[] values = new int[3];

        values[i] = STAGNATE.ordinal();
        i++;
        values[i] = INCREASE.ordinal();
        i++;
        values[i] = DECREASE.ordinal();
        i++;

        return values;
    }

    public int value() {
        return super.ordinal();
    }

    /**
    * Create a default instance
    */  
    public static PredictionKind create() {

        return valueOf(0);
    }

    /**
    * Print Method
    */     
    public String toString(String desc, int indent) {
        StringBuffer strBuffer = new StringBuffer();

        CdrHelper.printIndent(strBuffer, indent);

        if (desc != null) {
            strBuffer.append(desc).append(": ");
        }

        strBuffer.append(this);
        strBuffer.append("\n");              
        return strBuffer.toString();
    }

    private Object readResolve() throws ObjectStreamException {
        return valueOf(ordinal());
    }

    private PredictionKind(String name, int ordinal) {
        super(name, ordinal);
    }
}

