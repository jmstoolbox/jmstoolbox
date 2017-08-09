//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2017.08.09 à 07:47:37 AM EDT 
//


package org.titou10.jtb.cs.gen;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour column complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="column">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="userProperty" type="{}userProperty"/>
 *           &lt;element name="systemHeaderName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="columnKind" type="{}columnKind" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "column", propOrder = {
    "userProperty",
    "systemHeaderName"
})
public class Column {

    protected UserProperty userProperty;
    protected String systemHeaderName;
    @XmlAttribute(name = "columnKind")
    protected ColumnKind columnKind;

    /**
     * Obtient la valeur de la propriété userProperty.
     * 
     * @return
     *     possible object is
     *     {@link UserProperty }
     *     
     */
    public UserProperty getUserProperty() {
        return userProperty;
    }

    /**
     * Définit la valeur de la propriété userProperty.
     * 
     * @param value
     *     allowed object is
     *     {@link UserProperty }
     *     
     */
    public void setUserProperty(UserProperty value) {
        this.userProperty = value;
    }

    /**
     * Obtient la valeur de la propriété systemHeaderName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSystemHeaderName() {
        return systemHeaderName;
    }

    /**
     * Définit la valeur de la propriété systemHeaderName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSystemHeaderName(String value) {
        this.systemHeaderName = value;
    }

    /**
     * Obtient la valeur de la propriété columnKind.
     * 
     * @return
     *     possible object is
     *     {@link ColumnKind }
     *     
     */
    public ColumnKind getColumnKind() {
        return columnKind;
    }

    /**
     * Définit la valeur de la propriété columnKind.
     * 
     * @param value
     *     allowed object is
     *     {@link ColumnKind }
     *     
     */
    public void setColumnKind(ColumnKind value) {
        this.columnKind = value;
    }

}
