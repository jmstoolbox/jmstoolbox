
package org.titou10.jtb.cs.gen;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for userProperty complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="userProperty"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="userPropertyName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="displayName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="displayWidth" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="type" type="{}userPropertyType"/&gt;
 *         &lt;element name="origin" type="{}userPropertyOrigin"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
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
     * Gets the value of the userPropertyName property.
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
     * Sets the value of the userPropertyName property.
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
     * Gets the value of the displayName property.
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
     * Sets the value of the displayName property.
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
     * Gets the value of the displayWidth property.
     * 
     */
    public int getDisplayWidth() {
        return displayWidth;
    }

    /**
     * Sets the value of the displayWidth property.
     * 
     */
    public void setDisplayWidth(int value) {
        this.displayWidth = value;
    }

    /**
     * Gets the value of the type property.
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
     * Sets the value of the type property.
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
     * Gets the value of the origin property.
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
     * Sets the value of the origin property.
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
