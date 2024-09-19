
package org.titou10.jtb.cs.gen;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Classe Java pour userPropertyOrigin.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * <pre>{@code
 * <simpleType name="userPropertyOrigin">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="USER_PROPERTY"/>
 *     <enumeration value="MAP_KEY"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "userPropertyOrigin")
@XmlEnum
public enum UserPropertyOrigin {

    USER_PROPERTY,
    MAP_KEY;

    public String value() {
        return name();
    }

    public static UserPropertyOrigin fromValue(String v) {
        return valueOf(v);
    }

}
