
package org.titou10.jtb.cs.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for userPropertyType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="userPropertyType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="LONG_TO_TS"/&gt;
 *     &lt;enumeration value="LONG_TO_DATE"/&gt;
 *     &lt;enumeration value="STRING"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "userPropertyType")
@XmlEnum
public enum UserPropertyType {

    LONG_TO_TS,
    LONG_TO_DATE,
    STRING;

    public String value() {
        return name();
    }

    public static UserPropertyType fromValue(String v) {
        return valueOf(v);
    }

}
