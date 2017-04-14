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
 * <p>
 * Classe Java pour visualizerSourceKind.
 * 
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu dans cette classe.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="visualizerSourceKind">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="INTERNAL"/>
 *     &lt;enumeration value="OS_EXTERNAL"/>
 *     &lt;enumeration value="SCRIPT_INTERNAL"/>
 *     &lt;enumeration value="SCRIPT_EXTERNAL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "visualizerSourceKind")
@XmlEnum
public enum VisualizerSourceKind {

                                  INTERNAL,
                                  OS_EXTERNAL,
                                  SCRIPT_INTERNAL,
                                  SCRIPT_EXTERNAL;

   public String value() {
      return name();
   }

   public static VisualizerSourceKind fromValue(String v) {
      return valueOf(v);
   }

}
