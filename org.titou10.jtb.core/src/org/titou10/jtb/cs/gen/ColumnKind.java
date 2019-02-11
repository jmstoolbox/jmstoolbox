
package org.titou10.jtb.cs.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for columnKind.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="columnKind"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="SYSTEM_HEADER"/&gt;
 *     &lt;enumeration value="USER_PROPERTY"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "columnKind")
@XmlEnum
public enum ColumnKind {

    SYSTEM_HEADER,
    USER_PROPERTY;

    public String value() {
        return name();
    }

    public static ColumnKind fromValue(String v) {
        return valueOf(v);
    }

}
