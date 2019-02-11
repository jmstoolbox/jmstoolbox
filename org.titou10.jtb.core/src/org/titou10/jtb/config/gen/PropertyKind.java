
package org.titou10.jtb.config.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for propertyKind.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="propertyKind"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="STRING"/&gt;
 *     &lt;enumeration value="BOOLEAN"/&gt;
 *     &lt;enumeration value="LONG"/&gt;
 *     &lt;enumeration value="INT"/&gt;
 *     &lt;enumeration value="SHORT"/&gt;
 *     &lt;enumeration value="FLOAT"/&gt;
 *     &lt;enumeration value="DOUBLE"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "propertyKind")
@XmlEnum
public enum PropertyKind {

    STRING,
    BOOLEAN,
    LONG,
    INT,
    SHORT,
    FLOAT,
    DOUBLE;

    public String value() {
        return name();
    }

    public static PropertyKind fromValue(String v) {
        return valueOf(v);
    }

}
