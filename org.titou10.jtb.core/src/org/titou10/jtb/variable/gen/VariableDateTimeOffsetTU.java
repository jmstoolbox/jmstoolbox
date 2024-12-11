
package org.titou10.jtb.variable.gen;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for variableDateTimeOffsetTU</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="variableDateTimeOffsetTU">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="YEARS"/>
 *     <enumeration value="MONTHS"/>
 *     <enumeration value="DAYS"/>
 *     <enumeration value="HOURS"/>
 *     <enumeration value="MINUTES"/>
 *     <enumeration value="SECONDS"/>
 *     <enumeration value="MILLISECONDS"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
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
