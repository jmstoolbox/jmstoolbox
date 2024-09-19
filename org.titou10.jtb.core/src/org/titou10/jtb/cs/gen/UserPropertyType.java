
package org.titou10.jtb.cs.gen;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Classe Java pour userPropertyType.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * <pre>{@code
 * <simpleType name="userPropertyType">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="LONG_TO_TS"/>
 *     <enumeration value="LONG_TO_DATE"/>
 *     <enumeration value="STRING"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "userPropertyType")
@XmlEnum
public enum UserPropertyType {

    LONG_TO_TS,
    LONG_TO_DATE,
    STRING;

    public String value() {
        return name();
    }

    public static UserPropertyType fromValue(String v) {
        return valueOf(v);
    }

}
