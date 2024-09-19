
package org.titou10.jtb.cs.gen;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Classe Java pour columnKind.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * <pre>{@code
 * <simpleType name="columnKind">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="SYSTEM_HEADER"/>
 *     <enumeration value="USER_PROPERTY"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "columnKind")
@XmlEnum
public enum ColumnKind {

    SYSTEM_HEADER,
    USER_PROPERTY;

    public String value() {
        return name();
    }

    public static ColumnKind fromValue(String v) {
        return valueOf(v);
    }

}
