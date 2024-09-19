
package org.titou10.jtb.visualizer.gen;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Classe Java pour visualizerKind.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * <pre>{@code
 * <simpleType name="visualizerKind">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="BUILTIN"/>
 *     <enumeration value="INLINE_SCRIPT"/>
 *     <enumeration value="EXTERNAL_COMMAND"/>
 *     <enumeration value="EXTERNAL_SCRIPT"/>
 *     <enumeration value="OS_EXTENSION"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
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
