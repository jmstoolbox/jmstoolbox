
package org.titou10.jtb.variable.gen;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Classe Java pour variableDateTimeKind.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * <pre>{@code
 * <simpleType name="variableDateTimeKind">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="STANDARD"/>
 *     <enumeration value="RANGE"/>
 *     <enumeration value="OFFSET"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "variableDateTimeKind")
@XmlEnum
public enum VariableDateTimeKind {

    STANDARD,
    RANGE,
    OFFSET;

    public String value() {
        return name();
    }

    public static VariableDateTimeKind fromValue(String v) {
        return valueOf(v);
    }

}
