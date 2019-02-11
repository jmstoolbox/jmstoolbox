
package org.titou10.jtb.variable.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for variableDateTimeOffsetTU.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="variableDateTimeOffsetTU"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="YEARS"/&gt;
 *     &lt;enumeration value="MONTHS"/&gt;
 *     &lt;enumeration value="DAYS"/&gt;
 *     &lt;enumeration value="HOURS"/&gt;
 *     &lt;enumeration value="MINUTES"/&gt;
 *     &lt;enumeration value="SECONDS"/&gt;
 *     &lt;enumeration value="MILLISECONDS"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "variableDateTimeOffsetTU")
@XmlEnum
public enum VariableDateTimeOffsetTU {

    YEARS,
    MONTHS,
    DAYS,
    HOURS,
    MINUTES,
    SECONDS,
    MILLISECONDS;

    public String value() {
        return name();
    }

    public static VariableDateTimeOffsetTU fromValue(String v) {
        return valueOf(v);
    }

}
