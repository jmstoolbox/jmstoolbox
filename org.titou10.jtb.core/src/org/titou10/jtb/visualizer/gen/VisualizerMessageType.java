//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.04.14 at 09:03:39 AM EDT 
//


package org.titou10.jtb.visualizer.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for visualizerMessageType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="visualizerMessageType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="BYTES"/>
 *     &lt;enumeration value="MAP"/>
 *     &lt;enumeration value="MESSAGE"/>
 *     &lt;enumeration value="TEXT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "visualizerMessageType")
@XmlEnum
public enum VisualizerMessageType {

    BYTES,
    MAP,
    MESSAGE,
    TEXT;

    public String value() {
        return name();
    }

    public static VisualizerMessageType fromValue(String v) {
        return valueOf(v);
    }

}
