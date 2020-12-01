

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

public class ActionKind  extends Enum {

    public static final ActionKind HEAT = new ActionKind("HEAT", 0);
    public static final int _HEAT = 0;
    public static final ActionKind COOL = new ActionKind("COOL", 1);
    public static final int _COOL = 1;
    public static final ActionKind STOP = new ActionKind("STOP", 2);
    public static final int _STOP = 2;
    public static ActionKind valueOf(int ordinal) {
        switch(ordinal) {

            case 0: return ActionKind.HEAT;
            case 1: return ActionKind.COOL;
            case 2: return ActionKind.STOP;

        }
        return null;
    }

    public static ActionKind from_int(int __value) {
        return valueOf(__value);
    }

    public static int[] getOrdinals() {
        int i = 0;
        int[] values = new int[3];

        values[i] = HEAT.ordinal();
        i++;
        values[i] = COOL.ordinal();
        i++;
        values[i] = STOP.ordinal();
        i++;

        return values;
    }

    public int value() {
        return super.ordinal();
    }

    /**
    * Create a default instance
    */  
    public static ActionKind create() {

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

    private ActionKind(String name, int ordinal) {
        super(name, ordinal);
    }
}

