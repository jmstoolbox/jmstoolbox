
package org.titou10.jtb.variable.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for variableStringKind.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="variableStringKind"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="ALPHABETIC"/&gt;
 *     &lt;enumeration value="ALPHANUMERIC"/&gt;
 *     &lt;enumeration value="NUMERIC"/&gt;
 *     &lt;enumeration value="CUSTOM"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "variableStringKind")
@XmlEnum
public enum VariableStringKind {

    ALPHABETIC,
    ALPHANUMERIC,
    NUMERIC,
    CUSTOM;

    public String value() {
        return name();
    }

    public static VariableStringKind fromValue(String v) {
        return valueOf(v);
    }

}
