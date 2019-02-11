
package org.titou10.jtb.variable.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for variableKind.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="variableKind"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="DATE"/&gt;
 *     &lt;enumeration value="INT"/&gt;
 *     &lt;enumeration value="LIST"/&gt;
 *     &lt;enumeration value="STRING"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "variableKind")
@XmlEnum
public enum VariableKind {

    DATE,
    INT,
    LIST,
    STRING;

    public String value() {
        return name();
    }

    public static VariableKind fromValue(String v) {
        return valueOf(v);
    }

}
