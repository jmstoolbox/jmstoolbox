
package org.titou10.jtb.visualizer.gen;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Classe Java pour visualizerMessageType.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * <pre>{@code
 * <simpleType name="visualizerMessageType">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="BYTES"/>
 *     <enumeration value="MAP"/>
 *     <enumeration value="TEXT"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
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
