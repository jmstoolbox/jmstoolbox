
package org.titou10.jtb.script.gen;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Classe Java pour stepKind.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * <pre>{@code
 * <simpleType name="stepKind">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="PAUSE"/>
 *     <enumeration value="REGULAR"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "stepKind")
@XmlEnum
public enum StepKind {

    PAUSE,
    REGULAR;

    public String value() {
        return name();
    }

    public static StepKind fromValue(String v) {
        return valueOf(v);
    }

}
