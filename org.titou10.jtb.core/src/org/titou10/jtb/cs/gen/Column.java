
package org.titou10.jtb.cs.gen;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour column complex type.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * 
 * <pre>{@code
 * <complexType name="column">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <choice>
 *           <element name="userProperty" type="{}userProperty"/>
 *           <element name="systemHeaderName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         </choice>
 *       </sequence>
 *       <attribute name="columnKind" type="{}columnKind" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
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
