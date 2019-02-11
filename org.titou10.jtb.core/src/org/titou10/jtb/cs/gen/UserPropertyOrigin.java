
package org.titou10.jtb.cs.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for userPropertyOrigin.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="userPropertyOrigin"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="USER_PROPERTY"/&gt;
 *     &lt;enumeration value="MAP_KEY"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
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
