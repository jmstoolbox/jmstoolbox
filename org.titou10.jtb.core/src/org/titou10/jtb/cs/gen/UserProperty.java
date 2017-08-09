//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2017.08.08 à 09:58:00 AM EDT 
//


package org.titou10.jtb.cs.gen;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour userProperty complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="userProperty">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="userPropertyName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="displayName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="displayWidth" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="type" type="{}userPropertyType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "userProperty", propOrder = {
    "userPropertyName",
    "displayName",
    "displayWidth",
    "type"
})
public class UserProperty {

    @XmlElement(required = true)
    protected String userPropertyName;
    @XmlElement(required = true)
    protected String displayName;
    protected int displayWidth;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected UserPropertyType type;

    /**
     * Obtient la valeur de la propriété userPropertyName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserPropertyName() {
        return userPropertyName;
    }

    /**
     * Définit la valeur de la propriété userPropertyName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserPropertyName(String value) {
        this.userPropertyName = value;
    }

    /**
     * Obtient la valeur de la propriété displayName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Définit la valeur de la propriété displayName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayName(String value) {
        this.displayName = value;
    }

    /**
     * Obtient la valeur de la propriété displayWidth.
     * 
     */
    public int getDisplayWidth() {
        return displayWidth;
    }

    /**
     * Définit la valeur de la propriété displayWidth.
     * 
     */
    public void setDisplayWidth(int value) {
        this.displayWidth = value;
    }

    /**
     * Obtient la valeur de la propriété type.
     * 
     * @return
     *     possible object is
     *     {@link UserPropertyType }
     *     
     */
    public UserPropertyType getType() {
        return type;
    }

    /**
     * Définit la valeur de la propriété type.
     * 
     * @param value
     *     allowed object is
     *     {@link UserPropertyType }
     *     
     */
    public void setType(UserPropertyType value) {
        this.type = value;
    }

}
