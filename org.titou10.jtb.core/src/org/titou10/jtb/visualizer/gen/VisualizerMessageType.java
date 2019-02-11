
package org.titou10.jtb.visualizer.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for visualizerMessageType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="visualizerMessageType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="BYTES"/&gt;
 *     &lt;enumeration value="MAP"/&gt;
 *     &lt;enumeration value="TEXT"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "visualizerMessageType")
@XmlEnum
public enum VisualizerMessageType {

    BYTES,
    MAP,
    TEXT;

    public String value() {
        return name();
    }

    public static VisualizerMessageType fromValue(String v) {
        return valueOf(v);
    }

}
