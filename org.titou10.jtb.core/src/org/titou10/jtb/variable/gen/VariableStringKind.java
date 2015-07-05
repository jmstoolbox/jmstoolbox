
package org.titou10.jtb.variable.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour variableStringKind.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="variableStringKind">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ALPHABETIC"/>
 *     &lt;enumeration value="ALPHANUMERIC"/>
 *     &lt;enumeration value="NUMERIC"/>
 *     &lt;enumeration value="CUSTOM"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
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
