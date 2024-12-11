
package org.titou10.jtb.variable.gen;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for variableDateTimeKind</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="variableDateTimeKind">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="STANDARD"/>
 *     <enumeration value="RANGE"/>
 *     <enumeration value="OFFSET"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "variableDateTimeKind")
@XmlEnum
public enum VariableDateTimeKind {

    STANDARD,
    RANGE,
    OFFSET;

    public String value() {
        return name();
    }

    public static VariableDateTimeKind fromValue(String v) {
        return valueOf(v);
    }

}
