
package org.titou10.jtb.cs.gen;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour userProperty complex type.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * 
 * <pre>{@code
 * <complexType name="userProperty">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="userPropertyName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         <element name="displayName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="displayWidth" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         <element name="type" type="{}userPropertyType"/>
 *         <element name="origin" type="{}userPropertyOrigin"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "userProperty", propOrder = {
    "userPropertyName",
    "displayName",
    "displayWidth",
    "type",
    "origin"
})
public class UserProperty {

    @XmlElement(required = true)
    protected String userPropertyName;
    protected String displayName;
    protected int displayWidth;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected UserPropertyType type;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected UserPropertyOrigin origin;

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

    /**
     * Obtient la valeur de la propriété origin.
     * 
     * @return
     *     possible object is
     *     {@link UserPropertyOrigin }
     *     
     */
    public UserPropertyOrigin getOrigin() {
        return origin;
    }

    /**
     * Définit la valeur de la propriété origin.
     * 
     * @param value
     *     allowed object is
     *     {@link UserPropertyOrigin }
     *     
     */
    public void setOrigin(UserPropertyOrigin value) {
        this.origin = value;
    }

}
