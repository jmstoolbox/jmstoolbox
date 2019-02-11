
package org.titou10.jtb.visualizer.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for visualizerKind.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="visualizerKind"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="BUILTIN"/&gt;
 *     &lt;enumeration value="INLINE_SCRIPT"/&gt;
 *     &lt;enumeration value="EXTERNAL_COMMAND"/&gt;
 *     &lt;enumeration value="EXTERNAL_SCRIPT"/&gt;
 *     &lt;enumeration value="OS_EXTENSION"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "visualizerKind")
@XmlEnum
public enum VisualizerKind {

    BUILTIN,
    INLINE_SCRIPT,
    EXTERNAL_COMMAND,
    EXTERNAL_SCRIPT,
    OS_EXTENSION;

    public String value() {
        return name();
    }

    public static VisualizerKind fromValue(String v) {
        return valueOf(v);
    }

}
