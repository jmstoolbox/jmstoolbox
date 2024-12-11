
package org.titou10.jtb.variable.gen;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for variableStringKind</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="variableStringKind">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="ALPHABETIC"/>
 *     <enumeration value="ALPHANUMERIC"/>
 *     <enumeration value="NUMERIC"/>
 *     <enumeration value="UUID"/>
 *     <enumeration value="CUSTOM"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "variableStringKind")
@XmlEnum
public enum VariableStringKind {

    ALPHABETIC,
    ALPHANUMERIC,
    NUMERIC,
    UUID,
    CUSTOM;

    public String value() {
        return name();
    }

    public static VariableStringKind fromValue(String v) {
        return valueOf(v);
    }

}
