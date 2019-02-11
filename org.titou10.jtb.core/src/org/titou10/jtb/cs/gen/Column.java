
package org.titou10.jtb.cs.gen;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for column complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="column"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="userProperty" type="{}userProperty"/&gt;
 *           &lt;element name="systemHeaderName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="columnKind" type="{}columnKind" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
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
     * Gets the value of the userProperty property.
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
     * Sets the value of the userProperty property.
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
     * Gets the value of the systemHeaderName property.
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
     * Sets the value of the systemHeaderName property.
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
     * Gets the value of the columnKind property.
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
     * Sets the value of the columnKind property.
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
