
package org.titou10.jtb.variable.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour variableKind.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="variableKind">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="DATE"/>
 *     &lt;enumeration value="INT"/>
 *     &lt;enumeration value="LIST"/>
 *     &lt;enumeration value="STRING"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
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
