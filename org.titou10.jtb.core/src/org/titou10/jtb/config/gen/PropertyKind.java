//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2015.06.08 à 04:09:59 PM EDT 
//


package org.titou10.jtb.config.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour propertyKind.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="propertyKind">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="STRING"/>
 *     &lt;enumeration value="BOOLEAN"/>
 *     &lt;enumeration value="LONG"/>
 *     &lt;enumeration value="INT"/>
 *     &lt;enumeration value="SHORT"/>
 *     &lt;enumeration value="FLOAT"/>
 *     &lt;enumeration value="DOUBLE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "propertyKind")
@XmlEnum
public enum PropertyKind {

    STRING,
    BOOLEAN,
    LONG,
    INT,
    SHORT,
    FLOAT,
    DOUBLE;

    public String value() {
        return name();
    }

    public static PropertyKind fromValue(String v) {
        return valueOf(v);
    }

}
