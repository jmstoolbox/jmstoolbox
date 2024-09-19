
package org.titou10.jtb.config.gen;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for propertyKind</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="propertyKind">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="STRING"/>
 *     <enumeration value="BOOLEAN"/>
 *     <enumeration value="LONG"/>
 *     <enumeration value="INT"/>
 *     <enumeration value="SHORT"/>
 *     <enumeration value="FLOAT"/>
 *     <enumeration value="DOUBLE"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
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
