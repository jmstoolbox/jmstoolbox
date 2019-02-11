
package org.titou10.jtb.variable.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for variableDateTimeKind.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="variableDateTimeKind"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="STANDARD"/&gt;
 *     &lt;enumeration value="RANGE"/&gt;
 *     &lt;enumeration value="OFFSET"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
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
