//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2017.08.09 à 07:47:37 AM EDT 
//


package org.titou10.jtb.cs.gen;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour columnKind.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="columnKind">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SYSTEM_HEADER"/>
 *     &lt;enumeration value="USER_PROPERTY"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "columnKind")
@XmlEnum
public enum ColumnKind {

    SYSTEM_HEADER,
    USER_PROPERTY;

    public String value() {
        return name();
    }

    public static ColumnKind fromValue(String v) {
        return valueOf(v);
    }

}
