//
// Ce fichier a été généré par Java Architecture for XML Binding (JAXB) Reference Implementation, v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Les modifications apportées à ce fichier seront perdues lors de la recompilation du schéma source. 
// Généré sur : 2017.04.12 le 09:31:50 AM EDT 
//


package org.titou10.jtb.visualizer.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour visualizerMessageType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="visualizerMessageType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="BYTES"/>
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
    TEXT;

    public String value() {
        return name();
    }

    public static VisualizerMessageType fromValue(String v) {
        return valueOf(v);
    }

}
