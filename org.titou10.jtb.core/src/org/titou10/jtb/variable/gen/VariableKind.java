
package org.titou10.jtb.variable.gen;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Classe Java pour variableKind.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * <pre>{@code
 * <simpleType name="variableKind">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="DATE"/>
 *     <enumeration value="INT"/>
 *     <enumeration value="LIST"/>
 *     <enumeration value="STRING"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
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
